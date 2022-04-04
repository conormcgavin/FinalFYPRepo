package finalBosss;

import java.io.IOException;


public class Main {
	public static void main(String[] args) throws IOException {
		Employee e1 = new Employee("John", 2, 4, 1);
		Employee e2 = new Employee("Mary", 2, 4, 1);
		Employee e3 = new Employee("Paul", 2, 4, 1);
		Employee e4 = new Employee("Joanne", 1, 4, 1);
		Employee e5 = new Employee("Phil", 2, 4, 1);
		Employee e6 = new Employee("David", 2, 4, 1);
		Employee e7 = new Employee("Philomena", 2, 4, 1);
		Employee e8 = new Employee("Jacob", 1, 4, 1);
		
		Controller c = new Controller();
		c.addEmployee(e1);
		c.addEmployee(e2);
		c.addEmployee(e3);
		c.addEmployee(e4);
		//c.addEmployee(e5);
		//c.addEmployee(e6);
		//c.addEmployee(e7);
		//c.addEmployee(e8);
		
		
		/*
		c.initialiseScheduler();
		
		int numWeeks = 1;
		
		
		for (int i = 0; i < numWeeks; i++) {
			c.initialiseScheduler();
			
			e1.addPreference(c.week, 0, 1);
			e1.addPreference(c.week, 1, 2);
			e1.addPreference(c.week, 2, 3);
			e2.addPreference(c.week, 3, 1);
			e2.addPreference(c.week, 4, 2);
			e2.addPreference(c.week, 5, 3);
			
			Solution s = c.runWeek();
			c.completeWeek();
			
			
			c.newWeek();
			
		}
		*/

		c.sampleRun(4, 1, true, false, 0);
		
		//e1.printCurrentPrefs();
		//e2.printCurrentPrefs();
		//e3.printCurrentPrefs();
		//e4.printCurrentPrefs();
		
		//e5.printCurrentPrefs();
		//e6.printCurrentPrefs();
		//e7.printCurrentPrefs();
		//e8.printCurrentPrefs();
		
		
		e1.printStats();
		e2.printStats();
		e3.printStats();
		e4.printStats();
		/*
		e5.printStats();
		e6.printStats();
		e7.printStats();
		e8.printStats();
		*/
	}
	
	
}
