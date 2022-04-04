package finalBosss;

import java.util.ArrayList;
import java.util.Random;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;

public class Controller {
	ArrayList<Employee> employees;
	int week;
	Scheduler scheduler;

	
	public Controller() {
		this.employees = new ArrayList<Employee>();
		this.week = 0;
	}
	
	public void addEmployee(Employee e) {
		System.out.printf("Adding Employee %s...\n", e.name);
		this.employees.add(e);
	}
	
	public void removeEmployee(Employee e) {
		System.out.printf("Removing Employee %s\n...", e.name);
		this.employees.remove(e);
	}
	
	public Scheduler initialiseScheduler() {
		System.out.println("Initialising Scheduler...");
		this.scheduler = new Scheduler(this.employees);
		return scheduler;
		
	}
	
	public void addPreferencesToModel(Scheduler scheduler) {
		System.out.println("Adding employee requests and preferences to model...");
		allocateFreePreferences();
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
	
	public boolean allocateFreePreferences() {
		int[] capacities = new int[Constants.days_per_week];
		for (int i = 0; i < capacities.length; i++) {
			capacities[i] = employees.size() - Constants.WORKERS_NEEDED_PER_DAY[i];
		}
		
		int[] demand = new int[Constants.days_per_week];
		for (Employee employee : employees) {
			for (Preference pref : employee.preferences) {
				demand[pref.day] +=1;
			}
		}
		
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
	
	public void updateBanks() {
		System.out.println("Updating Banks...");
		if (Constants.MODE == "expectedPrefs" || Constants.MODE == "creditBank" ) {
			float avg = (float)scheduler.finalSolution.getIntVal(scheduler.totalOverallPreferences) / employees.size();
			int iavg = Math.round(avg*100);
			for (int i=0; i<employees.size(); i++) {
				int prefs = scheduler.finalSolution.getIntVal(scheduler.totalPreferencesPerPerson[i]);
				int conflictPercentage = ((Constants.PREFERENCES_PER_PERSON*100 - employees.get(i).freePrefs*100) / Constants.PREFERENCES_PER_PERSON);
				int adjustedAvg = (iavg * conflictPercentage)/100;
				
				int conflictPrefsGranted =0;
				for (int j=0; j<Constants.PREFERENCES_PER_PERSON; j++) {
					if (employees.get(i).preferences.get(j).free == false && scheduler.finalSolution.getIntVal(scheduler.preferences.get(i)[j]) == 1) {
						conflictPrefsGranted += 1;
					}
				}
				int bankAdjustment = adjustedAvg - conflictPrefsGranted*100;
				
				//System.out.println(conflictPercentage + "\t" + adjustedAvg+ "\t" + conflictPrefsGranted+ "\t" + bankAdjustment);
				
				employees.get(i).bank += bankAdjustment;
			}
		} else if (Constants.MODE == "minPrefs") {
			for (int i=0; i<employees.size(); i++) {
				employees.get(i).bank += scheduler.finalSolution.getIntVal(scheduler.totalPreferencesPerPerson[i]) - scheduler.finalSolution.getIntVal(scheduler.minPrefs);
			}
		}
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
	}
	
	

	public Solution runWeek() {
		System.out.println("********************************************************************************");
		System.out.printf("-----------------------------RUNNING WEEK %d--------------------------------\n", week);	
		addPreferencesToModel(this.scheduler);
		scheduler.optimise();
		Solution solution = scheduler.solve();
		return solution;
	}
	
	public void completeWeek() {
		updateBanks();
		recordHistoryOfCurrentWeek();
	}
	
	public void newWeek() {
		System.out.println("Starting preparation for a new week...");
		week += 1;
		for (Employee e : employees) {
			e.freePrefs = 0;
			ArrayList<Preference> prefs = e.preferences;
			for (Preference p : prefs) {
				p.week = week;
				p.granted = false;
				p.free = false;
			}
		}
		scheduler = initialiseScheduler();
		System.out.println("********************************************************************************\n");
	}
	
	
	public void randomGen(boolean weekendHeavy) {
		for (Employee employee : employees) {
			Random rand = new Random();
			int intRandom;
			int[] dayList = new int[Constants.PREFERENCES_PER_PERSON];
			for (int i=0; i<Constants.PREFERENCES_PER_PERSON;) {
				if (!weekendHeavy) {
					intRandom = rand.nextInt(Constants.days_per_week);
				} else {
					intRandom = rand.nextInt(Constants.days_per_week + 2); // not very robust code
					if (intRandom >= Constants.days_per_week) {
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
				+ numWeeks + ", "
				+ periodForPrefChange  + ", " 
				+ weekendHeavy  + ", " 
				+ i + ", " 
				+ f.getIntVal(scheduler.totalOverallPreferences) + ", "
				+ f.getIntVal(scheduler.sumDists)  + ", " 
				+ f.getIntVal(scheduler.diff) + ", ");
				
				for (int j=0 ;j<employees.size(); j++) {
					fw.write(f.getIntVal(scheduler.totalPreferencesPerPerson[j]) + ", ");
				}
				/*
				for (int h=employees.size();h<6; h++) {
					fw.write("0, ");
				}
				*/
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

		System.out.println();
		System.out.println();
	}

	public void createFoldersAndHeaders() throws IOException {
		File theDir = new File(Constants.DATA_FILE);
		if (!theDir.exists()){
		    theDir.createNewFile();
		    FileWriter fw = new FileWriter(Constants.DATA_FILE,true);
			fw.write("mode, iter, weeksRunFor, weekPrefsChange, weekendHeavy, week, total_prefs, distance, maxMinDiff, ");
			for (int i=0 ;i<6; i++) {
				fw.write(String.format("prefs%d, ", i));
			}
			fw.close();
		}
	}
	
}