package finalBosss;

public class Preference {
	int week;
	boolean granted;
	int day;
	int order;
	int score;
	boolean free;
	boolean modelAsHard;
	
		
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
	
	public static Preference copy(Preference r) {
		Preference x = new Preference(r.week, r.day, r.order, r.modelAsHard);
		if (Constants.MODE == "maxPrefs") {
			x.score = r.score;
		}
		
		x.granted = r.granted;
		return x;
	}

	public int calculateScore(int bank) {
		int score = (int)( (Constants.SCORE_MULTIPLIER * (float)bank ) / 10);
		return score;
	}
}