package finalBosss;

public class Preference {
	int week; // the week of scheduling that the preference is requested on
	boolean granted; // true if preference was granted
	int day; // the day off requested by the preference
	int order; // order of preference
	int score; // Model C: the score associated with the preference based on employee bank
	boolean free; // true if preference is free (non-contested)
	boolean modelAsHard; // true if preference should be modelled as a hard constraint
	boolean workWith;
	
		
	public Preference(int week, int day, int order, boolean modelAsHard) {
		this.week = week;
		this.day = day;
		this.granted = false;
		this.order = order;
		this.free = false;
		this.modelAsHard = modelAsHard;
		
	}
	
	public Preference(int week, int day, int order, int bank, boolean modelAsHard) {
		this.week = week;
		this.day = day;
		this.granted = false;
		this.order = order;
		this.free = false;
		this.score = calculateScore(bank);
		this.modelAsHard = modelAsHard;
	}
	
	// copies the preference, returning a new preference object
	public static Preference copy(Preference r) {
		Preference x = new Preference(r.week, r.day, r.order, r.modelAsHard);
		if (Constants.MODE == "creditBank") {
			x.score = r.score;
		}
		
		x.granted = r.granted;
		x.free = r.free;
		return x;
	}

	// calculates the score of the preference using the employee's bank
	// bank can be treated as more or less important by updating the score multiplier
	public int calculateScore(int bank) {
		int score = (int)( (Constants.SCORE_MULTIPLIER * (float)bank ) / 10); 
		return score;
	}
}