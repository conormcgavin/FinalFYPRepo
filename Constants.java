package finalBosss;

public final class Constants {

    private Constants() {
            // restrict instantiation
    }
    static String MODE = "minPrefs";
    
    static String DATA_FILE = String.format("C:\\Users\\cmcga\\data.txt");
    
    static int[] WORKERS_NEEDED_PER_DAY = {1, 1, 1, 1, 3, 3, 2};

    static int hours_per_day = 10;
	static int days_per_week = 7;
	
	
	static float SCORE_MULTIPLIER = (float) 1.0;
	static int PREFERENCES_PER_PERSON = 3;
	static int START_BANK = 1000;

	
}