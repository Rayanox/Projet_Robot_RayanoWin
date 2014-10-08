import java.awt.Robot;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

import modele.Match;
import modele.Match.Score;
import modele.algo.AlgoRayanoWin_1;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;

import com.sun.org.apache.bcel.internal.generic.ALOAD;

import dao.Files_IO;
import Logs.LogFactory;
import Logs.LogFactory.Separators;
import TraitementFichier.FileFactory;
import rayanox.mailAPI.MailService;
import scrapping.Scrap;
import scrapping.Scrap.Site;

public class Main {

	//Attention au nouvel an ! Il peut y avoir un bug lors de la récupération des nouveaux matchs à cause du test estMatchDemarré qui attribut au matchl'année actuelle qui n'est peut etre pas l'année de match àquelques heures ou minutes prets !

	// A FAIRE :
	//-> Faire deux autres algos en plus de l'actuel :
	//     .Actuel : On parie toujours sur le premier prochain match
	//     .algo 2 : On pariera toujours sur la plus petite cote (toujours du nul) sur les 5 ou 10 prochains matchs
	//     .algo 3 : On pariera toujours sur la plus grosse cote (toujours du nul) sur les 5 ou 10 prochains matchs
	public static String ProjectName = "Projet_RayanoWin_V1";

	private static String slash = System.getProperty("os.name").toLowerCase()
			.contains("win") ? "\\" : "/";

	
	private static String NomLog= "Deroulement.log";
	
	
	private static String FolderConfig = "." + slash + "Config";

	private static String pathCurrentMatchs = FolderConfig + slash
			+ "currentMatchs.txt";
	private static String pathSolde= FolderConfig + slash
			+ "Solde.txt";
	private static String pathHistoriqueMatchsPerdus= FolderConfig + slash
			+ "HistoriqueMatchsPerdus.txt";
	
	
	private static int CoefBenef = 1;
	public static Score ScoreAParier = Score.Nul;
	private static double SoldeDepart = 30.00;
	
	private static int failureNumber = 0;
	private static int failureMaxNumber = 3;
	
	private static Site SiteAUtiliser = Site.Pronosoft;

	public static void main(String[] args) throws IOException {
		
		MailService.ProgramName = ProjectName;
		InitialiserDossiersEtSoldeDepart();

		while (failureNumber < failureMaxNumber) {
			try {

				RunMoteur();


				Thread.sleep(5 * 60 * 1000); //Toutes les 5 minutes car on ne scrappe plus bwin pour les resultats
			} catch (Exception e) {
				failureNumber++;
				MailService.SendErrorMail("Exception No " + failureNumber, e);
				try {
					LogFactory.Logguer(NomLog, "EXCEPTION ! (voir mail)", Separators.Tirets);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		try {
			LogFactory.Logguer(NomLog, "Fin du programme. No fail = "+failureNumber +"(failure max = "+failureMaxNumber+")"
					, Separators.Tirets);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void InitialiserDossiersEtSoldeDepart() throws IOException {
		//Creation du dossier de config
		new File(FolderConfig).mkdirs();
		
		//Creation du fichier de solde
		File fileSolde = new File(pathSolde);
		if(!fileSolde.exists())
			Files_IO.WriteNewSolde(pathSolde, SoldeDepart);
		
		//Creation du fichier HistoriqueMisesPerdues
		new File(pathHistoriqueMatchsPerdus).createNewFile();
	}

	private static void RunMoteur() {
		new Thread() {
			public void run() {
				try {
					//On teste d'abord si il y a un match en cours !
					Collection<Match> tousLesMatchsEnCours = Files_IO.ExtractCurrentMatchs(pathCurrentMatchs);
					
					String chaineALogguer = "";
					
					if(tousLesMatchsEnCours.isEmpty()) {
						//Si il n'y a aucun match en cours, alors on cherche et on ouvre un nouveau pari !
						AlgoRayanoWin_1 robot = new AlgoRayanoWin_1(ScoreAParier, CoefBenef);
						Match newMatch = Scrap.RecupNewMatchs(SiteAUtiliser);
						newMatch.setScore(ScoreAParier);
						robot = parierSurHistoriqueRobot(robot);//Pour mettre a jour le solde periodique
						robot.Parier(newMatch, newMatch.getCotes());
						Files_IO.WriteAddMatchBet(pathCurrentMatchs, newMatch, robot, false);
						Files_IO.WriteAddMatchBet(pathHistoriqueMatchsPerdus, newMatch, robot, true);
						
						//On retire la mise de notre cagnotte.
						double ancienSolde = Files_IO.ExtractSolde(pathSolde);
						double nouveauSolde = ancienSolde - robot.getMise();
						Files_IO.WriteNewSolde(pathSolde, nouveauSolde);
						
						//On loggue la nouvelle dans le fichier de logs
						chaineALogguer = buildChaineNewPari(newMatch, robot.getMise());
						LogFactory.Logguer(NomLog, chaineALogguer, Separators.Tirets);
					}else {//Si il y a déjà un pari en cours, alors on vérifie si le résultat est tombé !
						//On recupere donc d'abord le match en cours
						Match matchEnCours = ((ArrayList<Match>)tousLesMatchsEnCours).get(0);						
						
						//Puis on vérifie en ligne si le match est terminé
						Match resultatMatch = Scrap.RecupResultats(matchEnCours, Site.MatchEnDirect);
						if(!resultatMatch.getScoreResultat().equals(Score.NonJoue)) {
							double soldeDuFichier = Files_IO.ExtractSolde(pathSolde);
							AlgoRayanoWin_1 robot = new AlgoRayanoWin_1(matchEnCours.getScoreResultat(), CoefBenef, soldeDuFichier + matchEnCours.getMise());
							robot.Parier(resultatMatch, matchEnCours.getCotes());
							
							//On met à jours la cagnotte.
							boolean victoire = false;
							double gain = 0.0;
							double nouveauSolde = robot.getSoldeTotal();
							if(nouveauSolde != soldeDuFichier) { //Cas ou le pari est gagné, le solde est alors différent de l'initial
								Files_IO.WriteNewSolde(pathSolde, nouveauSolde);
								victoire = true;
								gain = nouveauSolde - soldeDuFichier;
								FileFactory.ecritureStringFichier(pathHistoriqueMatchsPerdus, "", false);//WIPE
							}
								
							
							//On loggue la nouvelle dans le fichier de logs
							chaineALogguer = "RESULTAT PARI -> " + (victoire ? "VICTOIRE !   Gain = "+gain : "DEFAITE");
							LogFactory.Logguer(NomLog, chaineALogguer, Separators.Tirets);
							
							//On vide la liste des matchs actuels
							FileFactory.ecritureStringFichier(pathCurrentMatchs, "", false);
						}
						//Si le match n'a pas été encore joué, on ne fait rien et on ne loggue RIEN.
					}
					
					
				} catch (Exception e) {
					failureNumber++;
					MailService.SendErrorMail("Exception No " + failureNumber, e);
					try {
						LogFactory.Logguer(NomLog, "EXCEPTION ! (voir mail)", Separators.Tirets);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						RollBack();
					} catch (Exception e1) {
						MailService.SendErrorMail("Echec du RollBack !", e1);
					}//Un essai de rollback qui n'est pas du tout le meilleur possible mais qui peut faire l'affaire dans la plupart des cas.
				}
			}			
		}.start();
	}
	
	
	
	protected static AlgoRayanoWin_1 parierSurHistoriqueRobot(
			AlgoRayanoWin_1 robot) throws Exception {
		ArrayList<Match> matchs = (ArrayList<Match>) Files_IO.ExtractCurrentMatchs(pathHistoriqueMatchsPerdus);
		for (Match match : matchs) {
			robot.Parier(match, match.getCotes());
		}
		return robot;
	}

	protected static void RollBack() throws Exception {
		
		ArrayList<Match> listeMatchs = (ArrayList<Match>)Files_IO.ExtractCurrentMatchs(pathCurrentMatchs);
		if(listeMatchs.size() > 0) {
			double solde = Files_IO.ExtractSolde(pathSolde);
			Match currentMatch = listeMatchs.get(0);
			double mise = currentMatch.getMise();
			Files_IO.WriteNewSolde(pathSolde, solde - mise);
		}
		
	}

	private static String buildChaineNewPari(Match newMatch, double mise) {
		String cote;
		switch (newMatch.getScoreResultat()) {
		case Nul:
			cote = newMatch.getCotes().N+"[X]";
			break;
		case VictoireEquipe1:
			cote = newMatch.getCotes().X1+"[E1]";
			break;
		case VictoireEquipe2:
			cote = newMatch.getCotes().X2+"[E2]";
			break;
		default:
			cote = newMatch.getCotes().N+"[ERROR]";
			break;
		}
		String chaine = "Nouveau Pari Ouvert ! ---> " + newMatch.getEquipe1() + " vs " + newMatch.getEquipe2() +
				" (Cote: "+cote+") <----->  Mise = "+mise;
		return chaine;
	}
}
