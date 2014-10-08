package modele.algo;

import modele.Cotes;
import modele.Match;
import modele.Match.Score;

//->Algo: 
//		Mise = (Coef benef + |solde periodique|) / (Cote - 1)
public class AlgoRayanoWin_1 {
	private Score scoreAParier;

	private int coefBenef;
	private double mise;

	private double soldeTotal;
	private double soldePeriodique;

	public AlgoRayanoWin_1() {
		this.scoreAParier = Score.Nul;
		this.coefBenef = 1;
		this.mise = 0;
		this.soldeTotal = 0;
		this.soldePeriodique = 0;
	}

	public AlgoRayanoWin_1(Score scoreAParier, int coefBenef) {
		this();
		this.scoreAParier = scoreAParier;
		this.coefBenef = coefBenef;
	}

	public AlgoRayanoWin_1(Score scoreAParier, int coefBenef, double soldeTotal) {
		this(scoreAParier, coefBenef);
		this.soldeTotal = soldeTotal;
	}

	public void Parier(Match match, Cotes cotes) {
		if(!match.getScoreResultat().equals(Score.NonJoue)) {//car la situation doit etre celle d'un match termin� (tout-en-un)
			double cote = (scoreAParier == Score.VictoireEquipe1 ? cotes.X1
				: (scoreAParier == Score.VictoireEquipe2 ? cotes.X2 : cotes.N));
		this.parier(match, cote);
		}
		
	}

	public void Parier(Match match, double cote) {
		if(!match.getScoreResultat().equals(Score.NonJoue)) {
			this.parier(match, cote);
		}		
	}

	
	
	// --------------- PARIS ----------------
	
	

	// Mise = (Coef benef + |solde periodique|) / (Cote - 1)
	private void parier(Match match, double cote) {

		double benef = 0;
		this.mise = (this.coefBenef + Math.abs(soldePeriodique)) / (cote - 1);
		this.mise = arrondirSup(mise, 2); // On arrondi toujours au supérieur pour etre sur de ne pas gagner moins que prévu pour rattraper les précédentes mises perdues..
		if (match.getScoreResultat().equals(this.scoreAParier)) {
			this.soldePeriodique = 0;
			// .variable benef = Cote * Mise - Mise
			benef = arrondirInf(cote * this.mise - this.mise, 2); // On arrondi à l'inférieur pour etre sur de ne pas sur-estimer nos  bénefs
		} else {
			this.soldePeriodique -= this.mise;
			benef += -this.mise;
		}

		this.soldeTotal =arrondir(this.soldeTotal + benef, 2);

	}

	
	
	
	private double arrondirSup(double nombre, int indiceApresVirgule) {
		int coefMul = (int) Math.pow(10, Math.abs(indiceApresVirgule));
		return (double)(Math.ceil(nombre * coefMul)) / coefMul;
	}
	
	private double arrondirInf(double nombre, int indiceApresVirgule) {
		int coefMul = (int) Math.pow(10, Math.abs(indiceApresVirgule)); //on multiplie pour avoir la partie décimale voulue en partie entiere
		return (double)(Math.floor(nombre * coefMul)) / coefMul; // et on arrondi à l'entier inférieur avant de diviser pour re-obtenir le nombre en décimal
	}
	
	private double arrondir(double nombre, int indiceApresVirgule) {
		int coefMul = (int)Math.pow(10, Math.abs(indiceApresVirgule));
		return (double)((int)(float)(nombre * coefMul)) / coefMul;
	}
	
	//*********** getters / setters *************
	
	
	
	public int getCoefBenef() {
		return coefBenef;
	}
	public double getMise() {
		return mise;
	}
	public Score getScoreAParier() {
		return scoreAParier;
	}
	public double getSoldePeriodique() {
		return soldePeriodique;
	}
	public double getSoldeTotal() {
		return soldeTotal;
	}
	
	
}
