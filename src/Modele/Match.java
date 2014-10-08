package modele;

public class Match {
	public enum Score { VictoireEquipe1, VictoireEquipe2, Nul, NonJoue};
	
	private String equipe1;
	private String equipe2;
	private Score scoreResultat;
	private double mise;
	private Cotes cotes;
	
	public Match (String equipe1, String equipe2) {
		this.equipe1 = equipe1;
		this.equipe2 = equipe2;
		this.scoreResultat = Score.NonJoue;
		this.mise = -1;
		this.cotes = new Cotes(-1, -1, -1);
	}
	
	public Match (String equipe1, String equipe2, Score score) {
		this(equipe1, equipe2);
		this.scoreResultat = score;
		
	}
	
	public Match (String equipe1, String equipe2, Score score, double mise) {
		this(equipe1, equipe2);
		this.scoreResultat = score;
		this.mise = mise;
	}
	
	public Match (String equipe1, String equipe2, Score score, Cotes cote) {
		this(equipe1, equipe2);
		this.scoreResultat = score;
		this.cotes = cote;
	}
	
	//getters / setters
	public String getEquipe1() {
		return equipe1;
	}
	public String getEquipe2() {
		return equipe2;
	}
	public double getMise() {
		return mise;
	}
	public Cotes getCotes() {
		return cotes;
	}
	public Score getScoreResultat() {
		return scoreResultat;
	}
	
	public void setScore(Score score) {
		this.scoreResultat = score;
	}
	public void setCotes(Cotes cotes) {
		this.cotes = cotes;
	}
	
}
