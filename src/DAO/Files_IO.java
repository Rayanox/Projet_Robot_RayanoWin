package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import modele.Cotes;
import modele.Match;
import modele.Match.Score;
import modele.algo.AlgoRayanoWin_1;
import TraitementFichier.FileFactory;

public class Files_IO {
	
	private static char splitCarac = '-';
	private static char splitCaracCotes = ':';
	
	
	public static void WriteNewSolde(String path, double solde) throws FileNotFoundException, IOException {
		FileFactory.ecritureStringFichier(path, ""+solde, false);
	}
	
	public static double ExtractSolde(String path) throws NumberFormatException, FileNotFoundException, IOException {
		double solde = Double.parseDouble(FileFactory.lectureStringFichier(path));
		return solde;
	}
	
	
	
	//Structure fichier config : equipe1 equipe2 scorePronostic mise
	public static void WriteAddMatchBet(String path, Match match, AlgoRayanoWin_1 robot, boolean append) throws Exception{
		
		String chaine = buildStringMatch(match, robot);
		FileFactory.ecritureStringFichier(path, chaine, append);
	}

	public static Collection<Match> ExtractCurrentMatchs(String path) throws Exception{
		try {
			ArrayList<String> lines = (ArrayList<String>) FileFactory.lectureListStringFichier(path);
			Collection<Match> results = transformListStringToMatch(lines);
			return results;
		}catch(FileNotFoundException e) {
			return new ArrayList<>();
		}
		
		
	}

	
	//Helpers
	
	public static String buildStringMatch(Match match, AlgoRayanoWin_1 robot) {
		return match.getEquipe1()+splitCarac
				+ match.getEquipe2() + splitCarac
				+ robot.getScoreAParier() + splitCarac
				+ robot.getMise() + splitCarac
				+ match.getCotes().X1 + splitCaracCotes + match.getCotes().N + splitCaracCotes + match.getCotes().X2;
		
	}
	
	public static Collection<Match> transformListStringToMatch(
			Collection<String> lines) {

		ArrayList<Match> matchs = new ArrayList<Match>();
		for (String line : lines) {
			if(!line.isEmpty()) {
				String[] elements = line.split(String.valueOf(splitCarac));
				
				String equipe1 = elements[0];
				String equipe2 = elements[1];
				Score pronostic = Score.valueOf(elements[2]);
				double mise = Double.parseDouble(elements[3]);
				
				String [] cotesString = elements[4].split(String.valueOf(splitCaracCotes));
				Cotes cotes = new Cotes(Double.parseDouble(cotesString[0]), Double.parseDouble(cotesString[1]), Double.parseDouble(cotesString[2]));
				
				Match m = new Match(equipe1, equipe2, pronostic, mise);
				m.setCotes(cotes);
				matchs.add(m);
			}
		}
		
		return matchs;
	}
	
}
