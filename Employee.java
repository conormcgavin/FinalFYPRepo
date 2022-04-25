package finalBosss;
import java.util.ArrayList;

public class Employee {
	int[] preferencesReceived; // amount of each order preference received
	ArrayList<Preference> preferences; // current preference of employee
	ArrayList<Preference> preferenceHistory; // history of past preferences of employee
	int freePrefs; // number of free preferences the employee has in the current week
	
	String name;
	int minDaysPerWeek; // minimum number of days employee should work a week
	int maxDaysPerWeek; // maximum number of days employee should work a week
	
	ArrayList<Integer> bankHistory; // history of bank balances over weeks
	ArrayList<Integer> prefScoreHistory; //  Model C: history of preference scores given to employee over weeks
	ArrayList<Integer> freePrefHistory;  // history of free preferences granted to employee over weeks
	ArrayList<Float> conflictPercentageGrantedHistory; //  history of the percentage of the employee's non-free preferences that were granted over weeks
	int bank; // Model C + D: credit bank of employee. Model B: surplus balance of employee.
	
	public Employee(String name, int min_days, int max_days, int skill_level) {
		
		this.preferences = new ArrayList<Preference>();
		this.preferencesReceived = new int[Constants.PREFERENCES_PER_PERSON];
		this.preferenceHistory = new ArrayList<Preference>();
		this.freePrefs = 0;
		this.freePrefHistory = new ArrayList<Integer>();
		this.conflictPercentageGrantedHistory = new ArrayList<Float>();
		
		this.name = name;
		this.minDaysPerWeek = min_days;
		this.maxDaysPerWeek= max_days;
		
		
		if (Constants.MODE == "expectedPrefs" || Constants.MODE == "creditBank") {
			this.bank = Constants.START_BANK;
			this.bankHistory = new ArrayList<Integer>();
			this.bankHistory.add(this.bank);
			this.prefScoreHistory = new ArrayList<Integer>();
		} else if (Constants.MODE == "minPrefs") {
			this.bank = 0;
			this.bankHistory = new ArrayList<Integer>();
		}
	}

	// adds new preference for employee. If preference already present for the order chosen,replaces that preference.
	public void addPreference(int week, int day, int order) {
		Preference p;
		if (Constants.MODE == "creditBank") {
			p = new Preference(week, day, order, bank, false); 
		} else {
			p = new Preference(week, day, order, false); 
		}
		if (preferences.size() < order) {
			preferences.add(p);
		} else {
			preferences.set(order-1, p);
		}
		
	}
	
	// prints current preferences of employee
	public void printCurrentPrefs() {
		System.out.println("Current preferences");
		for (Preference preference : preferences) {
			System.out.println("Order: " + preference.order + ", Day: " + preference.day);
		}
		System.out.println();
	}
	
	// prints information on previous preferences of employee
	public void printPrefHistory() {
		System.out.println("********************************************************************************");
		System.out.println("Preference history");
		for (Preference preference : preferences) {
			System.out.println("Order: " + preference.order + ", Day: " + preference.day);
		}
	}
	
	// prints statistics on employee treatment and preferences over time.
	public void printStats() {
		System.out.println("********************************************************************************");
		System.out.println("Printing stats for " + name + "...");
		System.out.println("--------------------------------------------------------------------------------");

		System.out.println("Printing Preference stats...");
		System.out.println("--------------------------------------------------------------------------------");
		printCurrentPrefs();
		System.out.println("--------------------------------------------------------------------------------");
		
		System.out.println("Preferences received: ");
		for (int i=0; i<Constants.PREFERENCES_PER_PERSON; i++) {
			System.out.println(i + ": " + preferencesReceived[i]);
		}
		
		if (Constants.MODE == "creditBank" || Constants.MODE == "expectedPrefs" || Constants.MODE == "minPrefs" ) {
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("Current Bank Balance: " + this.bank);
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("Bank History: ");
			for (Integer i : bankHistory) {
				System.out.print(i + "\t");
			}
			System.out.println();
			System.out.println("********************************************************************************\n");
		}
	}
	
}
