package finalBosss;

import java.util.ArrayList;
import java.util.Random;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;

public class Controller {
	ArrayList<Employee> employees; // list of employees to be scheduled
	int week; // current week
	Scheduler scheduler; // scheduler for producing timetables each week
	
	ArrayList<Solution> finalSolutionHistory; // history of solutions returned by scheduler each week
	ArrayList<Float> averagePercentageConflictPrefsGrantedHistory; // history of the average percentage of all employees non-free preferences that were granted over weeks
	
	
	public Controller() {
		this.employees = new ArrayList<Employee>();
		this.averagePercentageConflictPrefsGrantedHistory = new ArrayList<Float>();
		this.week = 0;
		this.finalSolutionHistory = new ArrayList<Solution>();
	}
	
	// adds an employee to the system
	public void addEmployee(Employee e) {
		System.out.printf("Adding Employee %s...\n", e.name);
		this.employees.add(e);
	}
	
	// removes an employee from the system [UNTESTED]
	public void removeEmployee(Employee e) {
		System.out.printf("Removing Employee %s\n...", e.name);
		this.employees.remove(e);
	}
	
	// creates a new scheduler with the current employees list
	public Scheduler initialiseScheduler() {
		System.out.println("Initialising Scheduler...");
		this.scheduler = new Scheduler(this.employees);
		return scheduler;
		
	}
	
	// adds current employee preferences to the scheduler's model
	// set print to true to enable system outputs
	public void addPreferencesToModel(Scheduler scheduler, boolean print) {
		if (print) {
			System.out.println("Adding employee requests and preferences to model...");
		}
		if (Constants.MODE == "creditBank" ||Constants.MODE == "expectedPrefs") {
			allocateFreePreferences(); // allocate free preferences if using a credit bank based model
		}
		for (int i = 0; i<employees.size(); i++) {
			Employee e = employees.get(i);
			ArrayList<Preference> prefs = e.preferences;
			for (Preference pref : prefs) {
				// if (!preRunPreference(pref)) {
				// 	EXIT
				// } 
				scheduler.addPreference(i, pref);
			}
		}
	}
	
	// pre-runs the scheduler when preferences are added so as to assert it is technically possible for the preference to be granted.
	// not needed in this project as all preferences are possible to be awarded to employees.
	// [UNTESTED]
	public boolean preRunPreference(Preference p) {
			Scheduler testScheduler = initialiseScheduler();
			p.modelAsHard = true;
			testScheduler.addPreference(0, p);
			Solver testSolver = testScheduler.model.getSolver();
			if(!testSolver.solve()) {
				System.out.println("Preference doesn't obey business constraints and hence can not be selected.");
				return false;
			} 
			p.modelAsHard = false;
			return true;
	}
	
	
	// flags free preferences of employees and models them as hard constraints in the system
	public boolean allocateFreePreferences() {
		// gets the capacity of preferences (how many employees could maximally be awarded the preference)
		int[] capacities = new int[Constants.DAYS_PER_WEEK];
		for (int i = 0; i < capacities.length; i++) {
			capacities[i] = employees.size() - Constants.WORKERS_NEEDED_PER_DAY[i];
		}
		
		// gets the demand of preferences (how many employees want the preference)
		int[] demand = new int[Constants.DAYS_PER_WEEK];
		for (Employee employee : employees) {
			for (Preference pref : employee.preferences) {
				demand[pref.day] +=1;
			}
		}
		
		// if demand < capacity, model the preference as hard and update data on free prefs
		for (Employee employee : employees) {
			for (Preference pref : employee.preferences) {
				if (demand[pref.day] <= capacities[pref.day]) {
					pref.modelAsHard = true;
					pref.free = true;
					employee.freePrefs +=1;
				}
			}
		}
		
		return false;
	}
	
	// updates the credit banks for employees in Model C and D, and balance for employees in Model B.
	public int[] updateBanks(boolean performAdjustment) {
		int bankAdjustment = 0;
		int[] adjustments = new int[employees.size()];
		
		if(performAdjustment) {
			System.out.println("Updating Banks...");
		}
		if (Constants.MODE == "expectedPrefs" || Constants.MODE == "creditBank" ) {
			
			// gets the total conflicting preferences over all employees, as well as the total amount that were granted over all employees.
			int totalConflictPrefs = 0;
			int totalConflictPrefsGranted = 0;
			for (int i=0; i<employees.size(); i++) {
				totalConflictPrefs += Constants.PREFERENCES_PER_PERSON - employees.get(i).freePrefs;
				for (int j=0; j<Constants.PREFERENCES_PER_PERSON; j++) {
					if (employees.get(i).preferences.get(j).free == false && 
							scheduler.finalSolution.getIntVal(scheduler.preferences.get(i)[j]) == 1) {
						totalConflictPrefsGranted += 1;
					}
				}
			}
			
			float averagePercentageConflictPrefsGranted = 0;
			int employeesWithConflictingPrefs = 0;
			
			// for each employee, gets how many conflicting prefs they were granted
			for (int i=0; i<employees.size(); i++) {
				int employeeConflictPrefs = ((Constants.PREFERENCES_PER_PERSON - employees.get(i).freePrefs));
				int employeeConflictPrefsGranted =0;
				
				for (int j=0; j<Constants.PREFERENCES_PER_PERSON; j++) {
					if (employees.get(i).preferences.get(j).free == false && 
							scheduler.finalSolution.getIntVal(scheduler.preferences.get(i)[j]) == 1) {
						employeeConflictPrefsGranted += 1;
					}
				}
				
				// gets the share of conflict prefs for the employee - their proportion of the total granted, based on their share of the total amount of conflicting preferences.
				// calculates their bank adjustment from this.
				float expectedShareOfConflictPrefs = (float) totalConflictPrefsGranted * 
						((float) employeeConflictPrefs / (float) totalConflictPrefs);
				bankAdjustment = (int) ((expectedShareOfConflictPrefs - employeeConflictPrefsGranted) * 100);
				
			
				// gets the percentage of the employees conflicting preferecnes they were awarded this week
				float percentageConflictPrefsGranted = 0;
				if (employeeConflictPrefs > 0) {
					percentageConflictPrefsGranted = ((float)employeeConflictPrefsGranted / (float)employeeConflictPrefs) * 100;
					employeesWithConflictingPrefs += 1;
					averagePercentageConflictPrefsGranted += percentageConflictPrefsGranted;
					if (performAdjustment) {
						employees.get(i).conflictPercentageGrantedHistory.add(percentageConflictPrefsGranted);
					} 
				}
				if (performAdjustment) {
					employees.get(i).bank += bankAdjustment;
				}
				
				adjustments[i] = bankAdjustment;
				
			 }
			
			// gets the average percentage of employee conflicting preferences awarded this week
			averagePercentageConflictPrefsGranted = averagePercentageConflictPrefsGranted / (float)employeesWithConflictingPrefs;
			if (performAdjustment) {
				this.averagePercentageConflictPrefsGrantedHistory.add(averagePercentageConflictPrefsGranted);
			}
				
				
		} else if (Constants.MODE == "minPrefs") {
			// updates balance based on how far above or below the employee was of the minPrefs value
			for (int i=0; i<employees.size(); i++) {
				bankAdjustment = scheduler.finalSolution.getIntVal(scheduler.totalPreferencesPerPerson[i]) -
						scheduler.finalSolution.getIntVal(scheduler.minPrefs);
				employees.get(i).bank += bankAdjustment;
			}
			
		}
		
		return adjustments;
		 
	}
	
	public void recordHistoryOfCurrentWeek() {
		System.out.println("Recording History...");
		
		for (int employee = 0; employee < employees.size(); employee ++) {
			Employee e = employees.get(employee);
			
			for (int i=0; i<e.preferences.size(); i++) {
				Preference pref = e.preferences.get(i);
				if (scheduler.finalSolution.getIntVal(scheduler.preferences.get(employee)[i]) == 1) {
					pref.granted = true;
					e.preferencesReceived[i] += 1;
				}
				Preference prefCopy = Preference.copy(pref);
				e.preferenceHistory.add(prefCopy);
			}
			
			if (Constants.MODE == "expectedPrefs" || Constants.MODE == "creditBank" || Constants.MODE == "minPrefs") {
				e.bankHistory.add(e.bank);
			}
		}
		
		this.finalSolutionHistory.add(scheduler.finalSolution);
	}
	
	

	public Solution runWeek() {
		System.out.println("********************************************************************************");
		System.out.printf("-----------------------------RUNNING WEEK %d--------------------------------\n", week);	
		addPreferencesToModel(this.scheduler, true);
		scheduler.optimise();
		Solution solution = scheduler.solve(true);
		return solution;
	}
	
	public void completeWeek() {
		updateBanks(true);
		recordHistoryOfCurrentWeek();
	}
	
	public void newWeek() {
		System.out.println("Starting preparation for a new week...");
		week += 1;
		for (Employee e : employees) {
			e.freePrefHistory.add(e.freePrefs);
			e.freePrefs = 0;
			ArrayList<Preference> prefs = e.preferences;
			for (Preference p : prefs) {
				p.week = week;
				p.granted = false;
				p.free = false;
				p.modelAsHard = false;
			}
		}
		scheduler = initialiseScheduler();
		System.out.println("********************************************************************************\n");
	}
	
	// randomly generated preferences for all employees in system... 
	// weekendHeavy set to true means twice as many preferences will be asked for on fri and sat.
	public void randomGen(boolean weekendHeavy) {
		for (Employee employee : employees) {
			Random rand = new Random();
			int intRandom;
			int[] dayList = new int[Constants.PREFERENCES_PER_PERSON];
			for (int i=0; i<Constants.PREFERENCES_PER_PERSON;) {
				if (!weekendHeavy) {
					intRandom = rand.nextInt(Constants.DAYS_PER_WEEK);
				} else {
					intRandom = rand.nextInt(Constants.DAYS_PER_WEEK + 2); // not very robust code
					if (intRandom >= Constants.DAYS_PER_WEEK) {
						intRandom -= 3; // fri/sat (5/6)
					}
				}
				boolean doneAlready = false;
				for (int j=0; j<dayList.length; j++) {
					if (dayList[j] == intRandom) {
						doneAlready = true;
					}
				}
				if (doneAlready == false) {
					dayList[i] = intRandom;
					i+=1;
				}
			}
			for (int j=0; j<dayList.length; j++) {
				employee.addPreference(week, dayList[j], j+1);
			}
		}
		
	}
	
	// sample simulation of many weeks running of solving.
	// can change number of weeks to run for, how often preferences randomly change, if weekend heavy preferences, if stats are tracked
	public void sampleRun(int numWeeks, int periodForPrefChange, boolean weekendHeavy, boolean trackStats, int iteration) throws IOException {
		if (trackStats) {
			createFoldersAndHeaders();
		}
		
		
		randomGen(weekendHeavy);
		initialiseScheduler();
		
		for (int i = 0; i < numWeeks; i++) {
			runWeek();
			completeWeek();
			
			
			if (trackStats) {
				Solution f = scheduler.finalSolution;
				FileWriter fw = new FileWriter(Constants.DATA_FILE,true);
				fw.write(
				Constants.MODE + ", " 
				+ iteration  + ", " 
				+ employees.size()  + ", " 
				+ numWeeks + ", "
				+ periodForPrefChange  + ", " 
				+ weekendHeavy  + ", " 
				+ i + ", " 
				+ f.getIntVal(scheduler.totalOverallPreferences) + ", "
				+ f.getIntVal(scheduler.avgDists)  + ", " 
				+ f.getIntVal(scheduler.diff) + ", "
				);
				
				for (int j=0 ;j<employees.size(); j++) {
					fw.write(f.getIntVal(scheduler.totalPreferencesPerPerson[j]) + ", ");
				}
				
				for (int h=employees.size();h<6; h++) {
					fw.write("0, ");
				}
				
				for (int j=0 ;j<employees.size(); j++) {
					fw.write(employees.get(j).bank + ", ");
				}

				for (int h=employees.size();h<6; h++) {
					fw.write("0, ");
				}
				
				fw.write("\n");
				fw.close();
			}
		
			newWeek();
			System.out.println();
			System.out.println();
			
			
			if (i % periodForPrefChange == 0) {
				randomGen(weekendHeavy);
			}
			
		}
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("----------------------------------END OF RUN-------------------------------------");
		System.out.println("---------------------------------------------------------------------------------");

		for (int h=0;h<employees.size(); h++) {
			employees.get(h).printStats();
		}
		
		System.out.println();
		System.out.println();
	}

	// creates folders for storing data for analysis
	public void createFoldersAndHeaders() throws IOException {
		File theDir = new File(Constants.DATA_FILE);
		if (!theDir.exists()){
		    theDir.createNewFile();
		    FileWriter fw = new FileWriter(Constants.DATA_FILE,true);
		    
			fw.write("mode, iter, numEmployees, weeksRunFor, weekPrefsChange, weekendHeavy, week, total_prefs, distance, maxMinDiff, ");
			for (int i=0 ;i<6; i++) {
				fw.write(String.format("prefs%d, ", i));
			}
			
			for (int i=0 ;i<6; i++) {
				fw.write(String.format("bank%d, ", i));
			}
			fw.close();
		}
	}
	

	// replay a solution with preference p modelled as a hard constraint to show differences in solutions.
	public Solution replay(int employee, Preference p) {
		System.out.println("********************************************************************************");
		System.out.printf("-----------------------------RE-RUNNING WEEK %d--------------------------------\n", week);	
		
		Employee e = employees.get(employee);
		System.out.printf("Re-running current week for purpose of the explanation of preference for employee %s...\n", e.name);
		
		// rerun
		// model all granted preferences as hard so they remain that way
		for (int j = 0; j < Constants.PREFERENCES_PER_PERSON; j ++) {
			if (scheduler.finalSolution.getIntVal(scheduler.preferences.get(employee)[j])== 1) {
				e.preferences.get(j).modelAsHard = true;
			}
		}
		p.modelAsHard = true;
		
		Scheduler rerunScheduler = new Scheduler(this.employees);
		Solution replayedSolution = new Solution(rerunScheduler.model);
		
		
		addPreferencesToModel(rerunScheduler, true);
		rerunScheduler.optimise();
		replayedSolution = rerunScheduler.solve(true);
		
		
		int originalDiff = scheduler.finalSolution.getIntVal(scheduler.maxScoreDiff);
		int replayedDiff = rerunScheduler.finalSolution.getIntVal(rerunScheduler.maxScoreDiff);
		
		if (replayedDiff > originalDiff) {
			System.out.println("The system aims to return a solution as close as possible to employees expected number of preferences. \n"
					+ "your preference being granted would result in a lower score.\n");
		}
		
		int originalPrefOutput = scheduler.finalSolution.getIntVal(scheduler.totalOverallPreferences);
		int replayedPrefOutput = replayedSolution.getIntVal(rerunScheduler.totalOverallPreferences);
		
		if (replayedPrefOutput > originalPrefOutput) {
			System.out.println("As can be seen, when your preference is granted, the overall amount of preferences that are distributed to all employees"
					+ "\nis lowered. In order to be as fair as possible to all employees, we aim to maximise the amount of preferences that we grant\n"
					+ "while finding good timetable solutions.\n");
		}
		
		if ((replayedDiff == originalDiff) && (replayedPrefOutput == originalPrefOutput)) {
			System.out.println("As can be seen, the solutions here are equal, meaning that there was a solution that granted your preferences without\n"
					+ "disadvantaging others. Cases like this are decided randomly. Because you are on the worse end, you may receive a boost in future weeks.\n\n");
		}
		
		int[] adjustments = updateBanks(false);
		if (adjustments[employees.indexOf(e)] > 0) {
			System.out.println("The system detects you have been granted less conflicting preferences than average this week.\n"
					+ "You have been given a boost which will result in increased chances of receiving preferences in weeks to come.");
		} else if (adjustments[employees.indexOf(e)] < 0) {
			System.out.println("Despite not being granted this preference, you have been granted more conflicting preferences than average this week.\n"
					+ "The system aims to equally distribute preferences amongst employees.");
		} else if (adjustments[employees.indexOf(e)] == 0) {
			System.out.println("You have received the average amount of preferences expected for you this week.");
		}	

		
		// What if given solution is closer to expected? 
			// would have to implement correct version of expectedPRefs
		
		System.out.println();
		System.out.println("********************************************************************************");
		System.out.println("\n\n");
		
		return replayedSolution;
	}
	
	// explains an employees current expected preferences value for current week.
	public void explainExPrefsValue(Employee e) {
		float avgEveryone = 0;
		float avgEmployee = 0;
		
		int numWeeksConflictingPrefs = 0;
		if (e.conflictPercentageGrantedHistory.size() != 0) {
			for (int i=0; i<e.conflictPercentageGrantedHistory.size(); i++) {
				avgEmployee += e.conflictPercentageGrantedHistory.get(i);
				numWeeksConflictingPrefs += 1;
			}
		}
		
		for (int i=0; i<week; i++) {
			avgEveryone += this.averagePercentageConflictPrefsGrantedHistory.get(i);
		}
	
		
		avgEveryone = avgEveryone / (week);
		avgEmployee = avgEmployee / numWeeksConflictingPrefs;
		
		
		System.out.printf("Over the last %d weeks, you have been granted %.2f percent of your conflicting preferences per week on average.\n", (int) week, avgEmployee);
		System.out.printf("The average percentage of conflicting preferences being granted over all employees in this time is %.2f percent.\n\n", avgEveryone);
		
		
		String bank_level = "";
		float totalBank = 0;
		for (Employee employee : employees) {
			totalBank += employee.bank;
		}
		float avgBank = totalBank / employees.size();
		float highBankBoundary = (float) (1.25 * avgBank);
		float lowBankBoundary = (float) (0.75 * avgBank);
		
		if (e.bank > avgBank) {
			if (e.bank >= highBankBoundary) {
				bank_level = "HIGH";
			} else {
				bank_level = "ABOVE AVERAGE";
			}
			System.out.printf("As a result of this, your bank level is %s compared to other employees.\n", bank_level);
		} else if (e.bank < avgBank) {
			if (e.bank <= lowBankBoundary) {
				bank_level = "LOW";
			} else {
				bank_level = "BELOW AVERAGE";
			}
			System.out.printf("As a result of this, your current bank level has decreased and is now %s.\n", bank_level);
		} else if (e.bank == avgBank) {
			bank_level = "AVERAGE";
			System.out.printf("As a result of this, your current bank level is %s.\n\n", bank_level);
		}
		
		System.out.printf("As a result, your chances of being granted a conflicting preference in coming weeks is %s.\n", bank_level);
		System.out.printf("Those employees with higher bank balances are given priority.\n");
		System.out.println("\n\n\n");
	}
	
	// explains why a preference wasn't granted by analysis of other employees who were granted the preference.
	public void explainPreferenceNotGranted(int employeeIndex, Preference p) {
		Employee e = employees.get(employeeIndex);
		float expected = (float) scheduler.finalSolution.getIntVal(scheduler.exScores[employeeIndex]) / 100;
		float actual = scheduler.finalSolution.getIntVal(scheduler.totalPreferencesPerPerson[employees.indexOf(e)]);
		
		System.out.printf("Your expected preferences value this week was %.2f.\n", expected);
		System.out.printf("Your actual granted number of preferences was %.2f.\n", actual);
		
		if (expected < actual) {
			System.out.printf("Your actual granted number of preferences was greater than expected.\n", actual);
			System.out.printf("While you weren't granted this preference, you still exceeded the number of your preferences \n"
					+ "that you were expected to be granted. This explains why the preference in question was not granted.\n\n");
		} else {
			System.out.printf("You were granted less preferences than was expected.\n\n", actual);
			int[] bankAdjustments = updateBanks(false);
			float oldbank = (float)e.bank;
			float newbank = (float) bankAdjustments[employeeIndex] + e.bank;
			
			ArrayList<Integer> conflictingEmployeeBanks = new ArrayList<Integer>();
			ArrayList<Integer> conflictingEmployeePreferences = new ArrayList<Integer>();
			
			for (int i = 0; i < employees.size(); i++) {
				ArrayList<Preference> prefs = employees.get(i).preferences;
				for (int j=0; j<Constants.PREFERENCES_PER_PERSON; j++) {
					if (prefs.get(j).day == p.day) {
						if (scheduler.finalSolution.getIntVal(scheduler.timetable[i][p.day]) == 0) {
							conflictingEmployeeBanks.add(employees.get(i).bank);
							conflictingEmployeePreferences.add(scheduler.finalSolution.getIntVal(scheduler.totalPreferencesPerPerson[i]));
						}
					}
				}
			}
			
			int amountEmployeesGreaterBank = 0;
			int amountEmployeesSameBank = 0;
			for (Integer othersBank : conflictingEmployeeBanks) {
				if (othersBank > e.bank) {
					amountEmployeesGreaterBank += 1; 
				} else if (othersBank == e.bank) {
					amountEmployeesSameBank += 1; 
				} 
			}
			
			int amountEmployeesLessSamePrefsThanYou = 0;
			for (Integer othersPrefs : conflictingEmployeePreferences) {
				if (othersPrefs < actual) {
					amountEmployeesLessSamePrefsThanYou += 1;
				}
			}
			
			
			
			if (conflictingEmployeeBanks.size() == 0) {
				System.out.println("No-one received this preference this week due to business constraints, which is why you were not granted this preference.");
			} else {
				System.out.printf("%d other employee(s) received the preference in your place.\n", conflictingEmployeeBanks.size());
				
				if (amountEmployeesGreaterBank > 0) {
					System.out.printf("Of these employees, %d had a greater bank than you due to being treated less fairly than you over time.\n", amountEmployeesGreaterBank);
				}
				if (amountEmployeesSameBank > 0) {
					System.out.printf("Of these employees, %d had the same bank as you. \n", amountEmployeesSameBank);
					System.out.println("Because you had the same expected preferences as another employee, the decision for who got more preferences was decided randomly. Because you are on the worse end, you may receive a boost in future weeks");
				}
				if (amountEmployeesLessSamePrefsThanYou > 0) {
					System.out.printf("Of these employees, %d received less or the same preferences as you overall. \n", amountEmployeesLessSamePrefsThanYou);
				}
				
		
			}
			System.out.println("\n");
			
			float percentageBankIncrease = ((newbank - oldbank) / oldbank) * 100;
			if (percentageBankIncrease < 0) {
				System.out.println("DANGER DANGER");
			} else if (percentageBankIncrease == 0) {
				System.out.println("same.");
			} else {
				System.out.printf("As you received less preferences than your expected share this week, you have received a %.2f percent increased chance of receiving preferences in future. \n");
			}
			
		}
		
		System.out.println("\n\n");
	}
	
	// used in counter-explanation systems to simulate different results for employees over previous weeks.
	public int[] updateBanksSimDiffResults(Solution solution, int employee, int numPreferencesLess) {
		int bankAdjustment = 0;
		int[] adjustments = new int[employees.size()];
		
		int totalConflictPrefs = 0;
		int totalConflictPrefsGranted = 0;
		for (int i=0; i<employees.size(); i++) {
			totalConflictPrefs += Constants.PREFERENCES_PER_PERSON - employees.get(i).freePrefs;
			for (int j=0; j<Constants.PREFERENCES_PER_PERSON; j++) {
				if (employees.get(i).preferences.get(j).free == false && 
						solution.getIntVal(scheduler.preferences.get(i)[j]) == 1) {
					totalConflictPrefsGranted += 1;
				}
			}
		}
		for (int i=0; i<employees.size(); i++) {
			int employeeConflictPrefs = ((Constants.PREFERENCES_PER_PERSON - employees.get(i).freePrefs));
			int employeeConflictPrefsGranted = 0;
			
			for (int j=0; j<Constants.PREFERENCES_PER_PERSON; j++) {
				if (employees.get(i).preferences.get(j).free == false && 
						solution.getIntVal(scheduler.preferences.get(i)[j]) == 1) {
					employeeConflictPrefsGranted += 1;
				}
			}
			
			if (i == employee) {
				if (employeeConflictPrefsGranted >= numPreferencesLess) {
					employeeConflictPrefsGranted -= numPreferencesLess;
				}
			}
			
			float expectedShareOfConflictPrefs = (float) totalConflictPrefsGranted * 
					((float) employeeConflictPrefs / (float) totalConflictPrefs);
			bankAdjustment = (int) ((expectedShareOfConflictPrefs - employeeConflictPrefsGranted) * 100);
			
			adjustments[i] = bankAdjustment;
			
		 }
		return adjustments;
	}
	
	// first calculates bank boost needed for employee to be granted preference p.
	// then simulates how many weeks employee receiving one less preference would take for preference p to be granted this week
	public void explainDecisionCounter(int employee, Preference p) {
		boolean done = false;
		Solution replayedSolution;
		
		int neededBankBalance = 0;
		float oldBankBalance = (float)employees.get(employee).bank;
		while (!done) {
			employees.get(employee).bank += (int) (float)100/(float)employees.size();
			Scheduler rerunScheduler = new Scheduler(this.employees);
			addPreferencesToModel(rerunScheduler, false);
			rerunScheduler.optimise();
			replayedSolution = rerunScheduler.solve(false);
			
			if (replayedSolution.getIntVal(scheduler.preferences.get(employee)[p.order-1]) == 1) {
				done = true;
				
				float bankBoost = (((float)employees.get(employee).bank - oldBankBalance) / employees.get(employee).bank) *100;
				System.out.printf("You would need a bank boost of %.2f percent in order to be granted this preference.", bankBoost);
				neededBankBalance = employees.get(employee).bank;
				employees.get(employee).bank = (int) oldBankBalance;
			}
		}
		
		
		float balance = employees.get(employee).bank;
		float adjustment = 0;
		int i=0;
		while ((balance + adjustment) < neededBankBalance && i < week) {
			int[] adjustments = updateBanksSimDiffResults(
					this.finalSolutionHistory.get(week-1-i), employee, 1
				);
			
			adjustment += adjustments[employee]; // add adjustment
			balance = employees.get(employee).bankHistory.get(week-1-i);
			i+=1;
		}
		System.out.printf("If you were granted 1 less conflicting preferences for the last %d week(s), "
				+ "you would have been granted this preference this week.\n", i);
		
	}
	
	// explains concept of free preferences to recipient who is questioning the target employee for receiving more preferences than them (despite lower expected preferences)
	public void explainFreePreferences(Employee recipient, Employee target) {		
		if (target.bank > recipient.bank) {
			System.out.printf("Employee %s had a greater expected preferences value than you due to being treated less fairly than you over time.\n", target.name);
		} else if (target.bank < recipient.bank) {
			System.out.printf("Employee %s had lower expected preferences value than you due to being treated less fairly than you over time.\n", target.name);
			System.out.printf("However, %d out of %d of %s's preferences were uncontested by other employees, and therefore marked as \"free\". \r\n"
					+ "\r\n"
					+ "If %s's preferences this week had have been contesting with your preferences, you would have received more preferences than %s.", target.freePrefs, Constants.PREFERENCES_PER_PERSON, target.name, target.name, target.name);
		}
		
		
	}
	
	public void explain1(int employee, Preference p) {
		replay(employee, p);
	}
	
	public void explain2(int employee, Preference p) {
		explainExPrefsValue(employees.get(employee));
		explainPreferenceNotGranted(employee, p);	
	}
	
	public void explain3(int employee, Preference p) {
		explainDecisionCounter(employee, p);
	}
	
}