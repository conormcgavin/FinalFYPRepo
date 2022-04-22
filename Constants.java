package finalBosss;

public final class Constants {

    private Constants() {
            // restrict instantiation
    }
    static String MODE = "expectedPrefs";
    
    static String DATA_FILE = String.format("C:\\Users\\cmcga\\balancedataXXXXX.txt");
    
    static int[] WORKERS_NEEDED_PER_DAY = {2, 1, 1, 1, 1, 1, 1};
   //static int[] WORKERS_NEEDED_PER_DAY = {3, 3, 3, 2, 2, 2, 1};
    //static int[] WORKERS_NEEDED_PER_DAY = {1, 1, 2, 2, 2, 2, 2};
    
    
    //static int[] WORKERS_NEEDED_PER_DAY = {2, 2, 2, 4, 5, 5, 4};

	static int DAYS_PER_WEEK = 7;
	
	
	static float SCORE_MULTIPLIER = (float) 1.0;
	static int PREFERENCES_PER_PERSON = 3;
	static int START_BANK = 1000;

}