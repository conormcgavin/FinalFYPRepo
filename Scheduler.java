package finalBosss;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.ParetoMaximizer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;


public class Scheduler {
	Model model;
	
	IntVar[][] timetable;
	
	IntVar[] levels;
	
	ArrayList<IntVar[]> preferences;
	IntVar[] totalPreferencesPerPerson;
	IntVar totalOverallPreferences;
	
	ArrayList<Employee> employees;
	
	Solution finalSolution;
	
	IntVar diff;
	IntVar sumDists;

	
	// Optional based on optimisation function
	IntVar finalScore;
	IntVar[] exScores;
	IntVar[] scoreDiffs;
	IntVar maxScoreDiff;
	IntVar totalScoreDifference;
	IntVar totalOverallScore; 
	IntVar[] preferenceScoreAssignment;
	IntVar minPrefs;
	IntVar[] minAdjusted;
	int[][] preferenceScores;
	IntVar avgDists;
	int amountOptimalSolutions;
	
	
	
	
	public Scheduler(ArrayList<Employee> employees) {
		Model model = new Model("FYP");
		
		this.model = model;
		this.employees = employees;
		
		this.timetable = model.intVarMatrix("Timetable", employees.size(), Constants.DAYS_PER_WEEK, 0, 1);
		
		this.levels = model.intVarArray("levels", employees.size(), 0, 2);
		
		this.preferences = new ArrayList<IntVar[]>();
		for (int i=0; i<employees.size(); i++) {
			preferences.add(model.intVarArray("PreferenceAssignments", Constants.PREFERENCES_PER_PERSON, 0, 1));
			levels[i] = model.intVar(employees.get(i).skillLevel);
		}
		
		
		
		this.totalPreferencesPerPerson = model.intVarArray("PrefsPerPerson", employees.size(), 0, 100000);
		this.totalOverallPreferences = model.intVar("TotalOverallScore", 0, 100000);
		
		this.addHardConstraints();
		this.addDataCollectionConstraints();
		
		if (Constants.MODE == "creditBank") {
			this.preferenceScores = new int[employees.size()][Constants.PREFERENCES_PER_PERSON];
		}
	
	}
	
	private void addHardConstraints() {
		// the total of the entire matrix should add up to the sum of workers_hours_per_week
		int sum_workers_needed = 0;
	    for (int value : Constants.WORKERS_NEEDED_PER_DAY) {
	        sum_workers_needed += value;
	    }
		model.sum(ArrayUtils.flatten(timetable), "=", sum_workers_needed).post();
		
		// every column should have a sum of exactly the sum of workers needed in that day
		for (int i=0; i<Constants.DAYS_PER_WEEK; i++) {
			model.sum(ArrayUtils.getColumn(timetable, i), "=", Constants.WORKERS_NEEDED_PER_DAY[i]).post();
		}
		
		// workers must work between their working day limits every week
		for (int i=0; i<employees.size(); i++) {
			model.sum(timetable[i], "<=", employees.get(i).maxDaysPerWeek).post();
			model.sum(timetable[i], ">=", employees.get(i).minDaysPerWeek).post();
		}
	}
	
	public void addPreference(int person, Preference p) {
		if (p.modelAsHard == false) {
			
			model.ifOnlyIf(
					model.arithm(timetable[person][p.day], "=", 0), 
					model.arithm(preferences.get(person)[p.order-1], "=", 1)
			);
			
		} else {
			model.arithm(timetable[person][p.day], "=", 0).post();
			model.arithm(preferences.get(person)[p.order-1], "=", 1).post();
		}
		if (Constants.MODE == "creditBank") {
			this.preferenceScores[person][p.order-1] = p.score;
		}
	}
	
	public void addDataCollectionConstraints() {
		IntVar max = model.intVar(0, 1000000);
		model.max(max, totalPreferencesPerPerson).post();
		IntVar min = model.intVar(0, 1000000);
		model.min(min, totalPreferencesPerPerson).post();
		
		this.diff = model.intVar(0, 1000000);
		model.arithm(max, "-", min, "=", diff).post();
		
		IntVar x = model.intVar(0,1000000);
		model.arithm(totalOverallPreferences, "*", model.intVar(100), "=", x).post();;
		
		IntVar[] y = model.intVarArray(employees.size(), 0,1000000);
		for (int i = 0; i < employees.size(); i++) {
			model.arithm(totalPreferencesPerPerson[i], "*", model.intVar(100), "=", y[i]).post();;
		}
		
		IntVar avg = model.intVar(0,1000000);
		model.arithm(x, "/", model.intVar(employees.size()), "=", avg).post();;
		IntVar[] dists = model.intVarArray(employees.size(), 0, 1000000);
		for (int i = 0; i < employees.size(); i++) {
			model.distance(avg, y[i], "=", dists[i]).post();;
		}
		this.sumDists = model.intVar(0,100000);
		this.avgDists = model.intVar(0,100000);
		model.sum(dists, "=", sumDists).post();
		model.arithm(sumDists, "/", model.intVar(employees.size()), "=", avgDists).post();;
	}	
	
	public void optimise() {
		
		for (int i = 0; i < employees.size(); i++) {
			model.sum(preferences.get(i), "=", totalPreferencesPerPerson[i]).post();
		}
		
		model.sum(totalPreferencesPerPerson, "=", totalOverallPreferences).post();
	
		if (Constants.MODE == "minPrefs") {
			this.optimiseMinPrefs();
		} else if (Constants.MODE == "expectedPrefs") {
			this.optimiseExpectedPrefs();
		} else if (Constants.MODE == "creditBank") {
			this.optimiseCreditBank();
		}
	}
	
	public void optimiseCreditBank() {
		
		this.preferenceScoreAssignment = model.intVarArray("PreferenceScores", employees.size(), 0 , 100000);
		this.totalOverallScore = model.intVar("TotalOverallScore", 0, 100000);
		
		for (int i=0; i<employees.size(); i++) {
			model.scalar(preferences.get(i), preferenceScores[i], "=", preferenceScoreAssignment[i]).post();
		}
		
		model.sum(preferenceScoreAssignment, "=", totalOverallScore).post();
		
		model.setObjective(Model.MAXIMIZE, totalOverallScore);
		
	}

	public void optimiseExpectedPrefs() {
		
		this.exScores = model.intVarArray("exScores", employees.size(), 0, 100000);
		
		int sum = 0;
		int[] banks = new int[this.employees.size()];
		float[] bank_percentages = new float[this.employees.size()];
		IntVar[] bank_percentagesInt = model.intVarArray("BankPercentages", employees.size(), 0, 100);
		
		for (int i = 0; i < this.employees.size(); i++) {
			banks[i] = employees.get(i).bank;
			sum += employees.get(i).bank;
		}
		for (int i = 0; i < this.employees.size(); i++) {
			bank_percentages[i] = (float) (employees.get(i).bank*100 / sum);
		}
		
		bank_percentagesInt = model.intVarArray("BankPercentages", employees.size(), 0, 100);
		for (int i=0; i<employees.size(); i++) {
			bank_percentagesInt[i] = model.intVar((int)bank_percentages[i]);
		}
		
		for (int i=0; i<employees.size(); i++) {
			model.arithm(totalOverallPreferences, "*", bank_percentagesInt[i], "=", exScores[i]).post();
		}
		
		
		
		this.scoreDiffs = model.intVarArray("scoreDiffs", employees.size(), 0, 100000);
		this.totalScoreDifference = model.intVar("TotalScoreDiff", 0, 100000);
		this.finalScore = model.intVar("FinalScore", 0, 1000000);
		
		IntVar totalOverallPreferencesBy100 = model.intVar(0, 100000000);
		totalOverallPreferencesBy100 = model.intScaleView(totalOverallPreferences, 100);
		
		
		IntVar[] totalPreferencesPerPersonBy100 = model.intVarArray(this.employees.size(), 0,10000000);
		for (int i = 0; i < employees.size(); i++) {
			totalPreferencesPerPersonBy100[i] = model.intScaleView(totalPreferencesPerPerson[i], 100);
		}
		
		for (int i=0; i<employees.size(); i++) {
			model.distance(totalPreferencesPerPersonBy100[i], exScores[i], "=", scoreDiffs[i]).post();
		}
		model.sum(scoreDiffs, "=", totalScoreDifference).post();
		
		maxScoreDiff = model.intVar(0, 100000000);
		model.max(maxScoreDiff, scoreDiffs).post();
		/*
		IntVar[] diffs = model.intVarArray(employees.size(), 0, 10000000);
		
		IntVar maxScoreDiff = model.intVar(0, 10000000);
		for (int i=0; i<employees.size(); i++) {
			// have a list pointing to the intvars for every leftover variable
			// get the min diff from this
			// multiply it by 10^i
			// add it to list of diffs
			
			// get argmin and remove it from the list? is that possible
			// 
			IntVar maxDist = model.max(maxScoreDiff, scoreDiffs).post();
		}
		// get sum of this list as finalscore thing
		*/
		
		model.arithm(finalScore, "=", totalOverallPreferencesBy100, "-", maxScoreDiff).post();
		
		model.setObjective(Model.MAXIMIZE, finalScore);
		
	}

	public void optimiseMinPrefs() {

		this.minPrefs = model.intVar(0, 1000000);
		this.minAdjusted = model.intVarArray(employees.size(), 0, 1000000);

		for (int i = 0; i < employees.size(); i++) {
			model.arithm(minAdjusted[i], "=", totalPreferencesPerPerson[i], "+", employees.get(i).bank).post();
			;
		}

		model.min(minPrefs, minAdjusted).post();

		/*
		model.arithm(finalScore, "=", totalOverallPreferences, "+", minPrefs).post();

		model.setObjective(Model.MAXIMIZE, finalScore);
		*/
	}


	public Solution solve(boolean printSolutions) {
		if (printSolutions) {
			System.out.println("Solving...");
		}
		Solver solver = model.getSolver();
		finalSolution = new Solution(model);
		
		if (Constants.MODE == "maxPrefs") {
			
			ParetoMaximizer po = new ParetoMaximizer(new IntVar[]{totalOverallPreferences,model.intMinusView(diff)});
			solver.plugMonitor(po);
			
			while(solver.solve()) {
			}
			
			List<Solution> paretoFront = po.getParetoFront();
			int maxPrefs = 0;
			
			if (paretoFront.size() > 0) {
				for (Solution solution : paretoFront) {
					if (solution.getIntVal(totalOverallPreferences) > maxPrefs) {
						maxPrefs = solution.getIntVal(totalOverallPreferences);
						finalSolution = solution;
					} else if (solution.getIntVal(totalOverallPreferences) == maxPrefs) {
						if(solution.getIntVal(diff) < finalSolution.getIntVal(diff) ) {
							finalSolution = solution;
						}
					}
				}
				
				
				for(Solution s:paretoFront){
			        System.out.println("a = "+s.getIntVal(totalOverallPreferences)+" and b = "+s.getIntVal(diff));
				}
				
				int optimalSolutions = 0;
				for (Solution solution : paretoFront) {
					if (solution.getIntVal(totalOverallPreferences) == maxPrefs) {
						optimalSolutions +=1;
					}
				}
				FileWriter fw;
				try {
					fw = new FileWriter("C:\\Users\\cmcga\\optimal.txt", true);
					fw.write(String.format("%d\n", optimalSolutions));
					fw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		} else if (Constants.MODE == "minPrefs") {
			
			ParetoMaximizer po = new ParetoMaximizer(new IntVar[]{minPrefs,totalOverallPreferences});
			solver.plugMonitor(po);
			
			while(solver.solve()) {
			}
			
			List<Solution> paretoFront = po.getParetoFront();
			if (paretoFront.size() > 0) {
				int min =0;
				for (Solution solution : paretoFront) {
					if (solution.getIntVal(minPrefs) > min) {
						min = solution.getIntVal(minPrefs);
						finalSolution = solution;
					} else if (solution.getIntVal(minPrefs) == min) {
						if(solution.getIntVal(totalOverallPreferences) > finalSolution.getIntVal(totalOverallPreferences)) {
							finalSolution = solution;
						}
					}
				}
			}
			
			for(Solution s:paretoFront){
		        System.out.println("a = "+s.getIntVal(totalOverallPreferences)+" and b = "+s.getIntVal(minPrefs));
			}
			
		} else {
			while (solver.solve()) {
				finalSolution.record();
			}	
		}
		if (printSolutions) {
			printSolution(finalSolution);
			printPreferenceAssignments(finalSolution);
			printExtraInfo(finalSolution);
			
		}
		return finalSolution;
	}
	

	public void printSolution(Solution s) {
		System.out.println("Printing Optimal Solution...");
		System.out.println("-----------------------------------------------------------------------------");
		String row_sol;
		for (int i = 0; i < employees.size(); i++) {
			row_sol = "Worker " + i + ":\t";
			for (int j = 0; j < Constants.DAYS_PER_WEEK; j++) {
				row_sol += s.getIntVal(timetable[i][j]) + "\t";
			}
			System.out.println(row_sol);			
		}	
	}
	
	public void printPreferenceAssignments(Solution s) {
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("Printing preference assignments...");
		System.out.println("-----------------------------------------------------------------------------");

		for (int i = 0; i < employees.size(); i++) {
			System.out.println("Worker " + i + ": ");
			for (int j = 0; j < Constants.PREFERENCES_PER_PERSON; j++) {
				System.out.print(s.getIntVal(preferences.get(i)[j]) + "\t");
			}
			System.out.println();
		}
		System.out.println("-----------------------------------------------------------------------------");
	}

	public void printExtraInfo(Solution s) {
		System.out.println("Printing extra info...");
		System.out.println("-----------------------------------------------------------------------------");

		System.out.println("Total preferences granted: " + s.getIntVal(totalOverallPreferences));
		System.out.println("Average preferences per employee: " + (float)s.getIntVal(totalOverallPreferences) / (float)employees.size());
		System.out.println("Difference from max preferences to min prefs: " + s.getIntVal(diff));
		System.out.println("Average distance from average preferences each: " + (s.getIntVal(avgDists)));
		System.out.println();
		
		if (Constants.MODE == "minPrefs") {
			System.out.println("Minimum preferences granted for balance of 0: " + s.getIntVal(minPrefs));
		
		} else if (Constants.MODE == "expectedPrefs") {
			System.out.print("Expected Preferences: ");
			for (int j = 0; j < employees.size(); j++) {
				System.out.print((float)(s.getIntVal(exScores[j])) / 100.0 + "\t");
			}
			System.out.println();
			System.out.print("\nActual Preferences: ");
			for (int j = 0; j < employees.size(); j++) {
				System.out.print(s.getIntVal(totalPreferencesPerPerson[j]) + "\t");
			}
			System.out.println();
			System.out.println("Final Score: " + s.getIntVal(finalScore));
			
		} else if (Constants.MODE == "creditBank") {
			System.out.println("Score per employee: ");
			for (int j = 0; j < employees.size(); j++) {
				System.out.print(s.getIntVal(preferenceScoreAssignment[j]) + "\t");
			}
			System.out.println();
			System.out.println("Total overall score: " + totalOverallScore.getValue());
		}
		
		System.out.println("-----------------------------------------------------------------------------\n");
	}
}
