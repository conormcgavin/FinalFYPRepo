package finalBosss;

import java.io.IOException;

import org.chocosolver.solver.Solution;


public class Main {
	static Employee e1;
	static Employee e2;
	static Employee e3;
	static Employee e4;
	static Employee e5;
	static Employee e6;
	static Employee e7;
	static Employee e8;
	static Controller c;
	
	public static void main(String[] args) throws IOException {
		//c.addEmployee(e5);
		//c.addEmployee(e6);
		//c.addEmployee(e7);
		//c.addEmployee(e8);
		

		/*
		
		int numWeeks = 2;
		
		reset();
		for (int i = 0; i < numWeeks; i++) {
			
			c.initialiseScheduler();
			
			e1.addPreference(c.week, 4, 1);
			e1.addPreference(c.week, 5, 2);
			e1.addPreference(c.week, 6, 3);
			e2.addPreference(c.week, 4, 1);
			e2.addPreference(c.week, 5, 2);
			e2.addPreference(c.week, 6, 3);
			
			/*
			e3.addPreference(c.week, 1, 1);
			e3.addPreference(c.week, 2, 2);
			e3.addPreference(c.week, 3, 3);
			/*
			e4.addPreference(c.week, 4, 1);
			e4.addPreference(c.week, 5, 2);
			e4.addPreference(c.week, 6, 3);
			/*
			e5.addPreference(c.week, 0, 1);
			e5.addPreference(c.week, 2, 2);
			e5.addPreference(c.week, 3, 3);
			e6.addPreference(c.week, 0, 1);
			e6.addPreference(c.week, 2, 2);
			e6.addPreference(c.week, 3, 3);
			
			Solution s = c.runWeek();
			
			if (i < numWeeks -1) {
				c.completeWeek();
				c.newWeek();
			}
		}
		*/
		
		//e1.printStats();
		//e2.printStats();
		/*
		c.explain1(1, e2.preferences.get(2));
		System.out.println("_________________________\n\n");
		*/
		//c.explainExPrefsValue(e2);
		//System.out.println("_________________________\n\n");
		//c.explainFreePreferences(e1, e2);
		//c.explain3(1, e2.preferences.get(2));
		
		//c.explain3(1, e2.preferences.get(2));
		/*
		reset();
		c.sampleRun(2, 1, true, false, 0);
		*/
		
		reset();
		c.sampleRun(52, 1, true, true, 0);
		/*
		int numIters = 5;
		
		
		// change every week, weekend heavy / not weekend heavy, 4 employees
		
		for (int i = 0; i < numIters; i++) {
	
			
			reset();
			c.sampleRun(52, 4, true, true, i);
		}
		
		for (int i = 0; i < numIters; i++) {
			
			reset();
			c.sampleRun(1, 4, true, false, i);
			reset();
			c.sampleRun(6, 4, true, false, i);
			reset();
			c.sampleRun(24, 4, true, false, i);
			reset();
			c.sampleRun(52, 4, true, true, i);
			reset();
			c.sampleRun(1, 4, false, true, i);
			reset();
			c.sampleRun(6, 4, false, true, i);
			reset();
			c.sampleRun(24, 4, false, true, i);
			reset();
			c.sampleRun(52, 4, false, true, i);
			reset();
		}
		
		for (int i = 0; i < numIters; i++) {
			c.sampleRun(1, 52, true, true, i);
			reset();
			c.sampleRun(6, 52, true, true, i);
			reset();
			c.sampleRun(24, 52, true, true, i);
			reset();
			c.sampleRun(52, 52, true, true, i);
			reset();
			c.sampleRun(1, 52, false, true, i);
			reset();
			c.sampleRun(6, 52, false, true, i);
			reset();
			c.sampleRun(24, 52, false, true, i);
			reset();
			c.sampleRun(52, 52, true, true, i);
		}
		*/
		
		
		// track bank
		// track preferences received
		
		
		//e1.printCurrentPrefs();
		//e2.printCurrentPrefs();
		//e3.printCurrentPrefs();
		//e4.printCurrentPrefs();
		
		//e5.printCurrentPrefs();
		//e6.printCurrentPrefs();
		//e7.printCurrentPrefs();
		//e8.printCurrentPrefs();
		
		
		//e1.printStats();
		//e2.printStats();
		//e3.printStats();
		//e4.printStats();
		/*
		e5.printStats();
		e6.printStats();
		e7.printStats();
		e8.printStats();
		*/
		
		//e1.printStats();
		//e2.printStats();
	}
	
	public static void reset() {
		c = new Controller();
		e1 = new Employee("John", 4, 4, 1);
		e2 = new Employee("Mary", 4, 4, 1);

		/*
		if (Constants.MODE == "minPrefs") {
			e1.bank += 10;
			e2.bank += 20;
		} else*///if (Constants.MODE == "expectedPrefs" || Constants.MODE == "creditBank"){
			//e1.bank += 100;
			//e2.bank += 2000;
		//}
		
		e3 = new Employee("Paul", 4, 5, 1);
		e4 = new Employee("Joanne", 4, 5, 1);
		//e5 = new Employee("Homer", 2, 4, 1);
		//e6 = new Employee("Marge", 2, 4, 1);
		c.addEmployee(e1);
		c.addEmployee(e2);
		//c.addEmployee(e3);
		//c.addEmployee(e4);
		//c.addEmployee(e5);
		//c.addEmployee(e6);
	}
}
