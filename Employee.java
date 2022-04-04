package finalBosss;
import java.util.ArrayList;

public class Employee {
	int[] preferencesReceived;
	ArrayList<Preference> preferences;
	ArrayList<Preference> preferenceHistory;
	int freePrefs;
	
	String name;
	int minDaysPerWeek;
	int maxDaysPerWeek;
	int skillLevel;
	
	ArrayList<Integer> bankHistory;
	ArrayList<Integer> prefScoreHistory;
	int bank;
	int totalGivenScore;
	
	public Employee(String name, int min_days, int max_days, int skill_level) {
		
		this.preferences = new ArrayList<Preference>();
		this.preferencesReceived = new int[Constants.PREFERENCES_PER_PERSON];
		this.preferenceHistory = new ArrayList<Preference>();
		this.freePrefs = 0;
		
		this.name = name;
		this.minDaysPerWeek = min_days;
		this.maxDaysPerWeek= max_days;
		this.skillLevel = skill_level;
		
		
		if (Constants.MODE == "expectedPrefs" || Constants.MODE == "creditBank") {
			this.bank = Constants.START_BANK;
			this.bankHistory = new ArrayList<Integer>();
			this.totalGivenScore = 0;
			this.prefScoreHistory = new ArrayList<Integer>();
		} else if (Constants.MODE == "minPrefs") {
			this.bank = 0;
			this.bankHistory = new ArrayList<Integer>();
		}
	}

	
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
	
	public void printCurrentPrefs() {
		System.out.println("Current preferences");
		for (Preference preference : preferences) {
			System.out.println("Order: " + preference.order + ", Day: " + preference.day);
		}
		System.out.println();
	}
	
	public void printPrefHistory() {
		System.out.println("********************************************************************************");
		System.out.println("Preference history");
		for (Preference preference : preferences) {
			System.out.println("Order: " + preference.order + ", Day: " + preference.day);
		}
	}
	
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
