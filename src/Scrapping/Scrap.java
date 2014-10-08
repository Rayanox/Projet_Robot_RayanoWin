package scrapping;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import modele.Cotes;
import modele.Match;
import modele.Match.Score;

public class Scrap {

	public static String urlResultatsBwin = "http://livescore.betradar.com/feed.php?type=days&from=1407088800000&to=1407103740000&key=b69cb272cf04ab3a29dddceb04780d6a&language=fr&config=0&bookid=12&mc=0&c=2";
	public static String urlResultatsMATCHENDIRECT = "http://www.matchendirect.fr/resultat-foot-";
	public static String urlResultatsPRONOSOFT = "http://www.pronosoft.com/fr/parions_sport/resultats_parions_sport.php?mode=histo&date=";

	public static String urlNewMatchsBWIN = "https://www.bwin.fr/betViewIframe.aspx?SportID=4&bv=bb&selectedLeagues=0";
	public static String urlNewMatchsPRONOSOFT = "http://www.pronosoft.com/fr/parions_sport/pronostics_1N2.htm";

	public enum Site {
		Bwin, MatchEnDirect, Pronosoft
	};

	public static Match RecupNewMatchs(Site site) throws Exception {
		String url = site.equals(Site.Bwin) ? urlNewMatchsBWIN
				: urlNewMatchsPRONOSOFT;

		Connection connection = HttpConnection.connect(new URL(url));
		Document doc = connection.get();

		if (site.equals(Site.Bwin))
			return TransformNewMatchsBWIN(doc);
		else {// Pronosoft
			return TransformNewMatchsPRONOSOFT(doc, getCurrentDate());
		}
	}

	public static Match RecupResultats(Match matchExpected, Site site)
			throws Exception {
		String url = determineResultatsUrl(site);

		Connection connection = HttpConnection.connect(new URL(url));
		Document doc = connection.get();

		switch (site) {
		case Bwin:
			return transformMatchResult(doc, matchExpected, site);
		case MatchEnDirect:
			Match todayMatchSearch = transformMatchResult(doc, matchExpected,
					site);
			if (IsTimeEarly()) {
				connection = HttpConnection.connect(new URL(
						buildMatchEnDirectResultatUrl(false)));
				doc = connection.get();
				return transformMatchResult(doc, matchExpected, site);
			}
			return todayMatchSearch;
		case Pronosoft:
			Match todayMatchSearchP = transformMatchResultPRONOSOFT(doc,
					matchExpected);
			if (IsTimeEarly()) {
				connection = HttpConnection.connect(new URL(
						buildPronosoftUrl(false)));
				doc = connection.get();
				return transformMatchResultPRONOSOFT(doc, matchExpected);
			}
			return todayMatchSearchP;
		default:
			throw new Exception("Site non spécifié dans la récup de resultats.");
		}

	}

	// Helpers

	private static String determineResultatsUrl(Site site) {
		String url;
		switch (site) {
		case Bwin:
			url = urlResultatsBwin;
			break;
		case MatchEnDirect:
			url = buildMatchEnDirectResultatUrl(true);
			break;
		case Pronosoft:
			url = buildPronosoftUrl(true);
			break;
		default:
			url = "ERROR URL IN SWITCH !!";
			break;
		}
		return url;
	}

	private static boolean IsTimeEarly() {
		return getCurrentDate().get(Calendar.HOUR_OF_DAY) < 3;
	}

	private static String buildMatchEnDirectResultatUrl(boolean today) {
		return buildResultatUrlByDate(getCurrentDate(), today,
				urlResultatsMATCHENDIRECT, true);
	}

	private static String buildPronosoftUrl(boolean today) {
		return buildResultatUrlByDate(getCurrentDate(), today,
				urlResultatsPRONOSOFT, false);
	}

	public static String buildResultatUrlByDate(Calendar c, boolean today,
			String url, boolean tirets) {
		String date = buildDateString(c, today, tirets);
		return url + date;
	}

	private static String buildDateString(Calendar c, boolean today,
			boolean tirets) {
		String separation = tirets ? "-" : "/";
		int day = c.get(Calendar.DAY_OF_MONTH) - (today ? 0 : 1);
		int month = (c.get(Calendar.MONTH) + 1);
		return (day < 10 ? "0" : "") + day + separation
				+ (month < 10 ? "0" : "") + month + separation
				+ c.get(Calendar.YEAR);
	}

	public static Calendar getCurrentDate() {
		return Calendar.getInstance();
	}

	public static Match transformMatchResult(Document doc, Match matchExpected,
			Site site) {

		Match mResult = null;

		if (site.equals(Site.Bwin)) {// Abandonné pour probleme d'id généré
										// bizarrement par le swf que nous
										// envoie le site et qui nous oblige a
										// renvoyer cette key générée mais que
										// l'on n'arrive pas à obtenir
			for (Element eMatch : doc.getElementsByTag("m")) {
				if (!eMatch.parent().tagName().equals("mp")) {
					String currentEquipe1 = eMatch.getElementsByTag("t1")
							.first().text().trim();
					String currentEquipe2 = eMatch.getElementsByTag("t2")
							.first().text().trim();
					if (reconnaissanceEquipesMatch(currentEquipe1,
							currentEquipe2, matchExpected)) {
						Score currentScore = GetTransformedScore(eMatch
								.getElementsByTag("s").first().text(), eMatch);
						mResult = new Match(currentEquipe1, currentEquipe2,
								currentScore);
						break;
					}
				}
			}
		} else {
			// Jamais utilisé
		}

		return mResult == null ? new Match(matchExpected.getEquipe1(),
				matchExpected.getEquipe2(), Score.NonJoue) : mResult;
	}

	public static Match transformMatchResultPRONOSOFT(Document doc,
			Match matchExpected) {
		Elements tousLesMatchs = doc.getElementsByClass("match_analyse");
		Match mResult = null;

		for (Element eMatch : tousLesMatchs) {
			String[] splitName = eMatch.text().split("-");
			String name1 = splitName[0].replace(String.valueOf((char) 160), " ").trim();
			String equipe1 = name1.startsWith("&nbsp;") ? name1.substring(6) : name1;
			String equipe2 = splitName[1].trim();
			if (reconnaissanceEquipesMatch(equipe1, equipe2, matchExpected)) {
				Score currentScore = GetTransformedScorePRONOSOFT(eMatch);
				mResult = new Match(equipe1, equipe2, currentScore);
				break;
			}

		}

		return mResult == null ? new Match(matchExpected.getEquipe1(),
				matchExpected.getEquipe2(), Score.NonJoue) : mResult;
	}

	private static Score GetTransformedScorePRONOSOFT(Element eMatch) {
		if(estMatchTerminePRONOSOFT(eMatch)) {
			String text = eMatch.parent().text();
			String repereBegin = "Score Final :";
			String scoreText = text.substring(text.indexOf(repereBegin) + repereBegin.length(), 
					text.indexOf('|')).trim();
			String [] scoresStrings = scoreText.split("-");
			int scoreEquipe1 = Integer.parseInt(scoresStrings[0]);
			int scoreEquipe2 = Integer.parseInt(scoresStrings[1]);
			
			Score score = calculeScoreFromInts(scoreEquipe1, scoreEquipe2);
			return score;
		}else {
			return Score.NonJoue;
		}
		
	}

	private static Score GetTransformedScore(String statut, Element eMatch) {
		Element eScores = eMatch.getElementsByTag("sc").first();
		Score score;

		if (statut.contains("Termin")) { // Fin normale

			Element eScoreNormal = eScores.getElementsByAttributeValue("t",
					"normaltime").first();
			int scoreEquipe1 = Integer.parseInt(eScoreNormal
					.getElementsByTag("t1").first().text());
			int scoreEquipe2 = Integer.parseInt(eScoreNormal
					.getElementsByTag("t2").first().text());

			score = calculeScoreFromInts(scoreEquipe1, scoreEquipe2);

		} else if (statut.toUpperCase().contains("SP")) { // Penalties

			Element eScorePenalties = eScores.getElementsByAttributeValue("t",
					"penalties").first();
			int scoreEquipe1 = Integer.parseInt(eScorePenalties
					.getElementsByTag("t1").first().text());
			int scoreEquipe2 = Integer.parseInt(eScorePenalties
					.getElementsByTag("t2").first().text());

			score = calculeScoreFromInts(scoreEquipe1, scoreEquipe2);

		} else { // Pas terminés
			score = Score.NonJoue;
		}

		return score;
	}
	
	private static Score calculeScoreFromInts(int scoreEquipe1, int scoreEquipe2) {
		return scoreEquipe1 >= scoreEquipe2 ? (scoreEquipe1 > scoreEquipe2 ? Score.VictoireEquipe1
				: Score.Nul)
				: Score.VictoireEquipe2;
	}

	public static Match TransformNewMatchsBWIN(Document eMatch) {
		Elements normalNodes = eMatch.getElementsByClass("bet-list");
		Elements eNewMatchs = normalNodes.size() > 2 ? normalNodes.get(2)
				.getElementsByClass("normal") : normalNodes.get(1)
				.getElementsByClass("normal"); // Au cas ou il n'y aurait pas de
												// grands matchs matchs de
												// prévus, d'ou la section
												// "Les meilleurs paris" serait
												// abenste
		Element eNewMatch = eNewMatchs.first();

		Cotes cotes = extractCotesBWIN(eNewMatch);
		Match match = extractAndComposeNewMatchBWIN(eNewMatch, cotes);

		return match;
	}

	public static Match TransformNewMatchsPRONOSOFT(Document eMatch, Calendar currentDate) {
		Elements matchsNodes = eMatch.getElementsByClass("match");

		for (Element element : matchsNodes) {
			Element matchElement = element.parent();
			if (matchElement.getElementsByClass("match_analyse").first()
					.getElementsByTag("img").attr("src").contains("foot")
					&& possedeCotesValides(matchElement)
					&& !estMatchTerminePRONOSOFT(element)
					&& !estMatchDemarrePRONOSOFT(matchElement, currentDate)) {
				Cotes cotes = extractCotesPRONOSOFT(matchElement);
				Match match = extractAndComposeNewMatchPRONOSOFT(matchElement,
						cotes);

				return match;
			}
			
		}

		return new Match("ERROR=NO NEW MATCH FOUND", "ERROR=NO NEW MATCH FOUND");

	}

	public static boolean estMatchDemarrePRONOSOFT(Element eMatch, Calendar currentDate) {
		String [] textDateSplit = eMatch.parent().parent().previousElementSibling().text().trim().split(" ");
		int jour = Integer.parseInt(textDateSplit[1]);
		int mois = conversionMois(textDateSplit[2]);
		int annee = getCurrentDate().get(Calendar.YEAR);
		
		String [] textTimeSplit = eMatch.getElementsByClass("heure").first().text().split("h");
		int heure = Integer.parseInt(textTimeSplit[0]);
		int minutes = Integer.parseInt(textTimeSplit[1]);
		int secondes = 0;
		
		Calendar dateMatch = new GregorianCalendar(annee, mois, jour, heure, minutes, secondes);
		
		return dateMatch.before(currentDate);
	}

	private static int conversionMois(String moisString) {
		// TODO Auto-generated method stub
		String moisText = moisString.toLowerCase().trim();
		if(moisText.contains("janv"))
			return 0;
		else if (moisText.contains("fevr"))
			return 1;
		else if (moisText.contains("mars"))
			return 2;
		else if (moisText.contains("avril"))
			return 3;
		else if (moisText.contains("mai"))
			return 4;
		else if (moisText.contains("juin"))
			return 5;
		else if (moisText.contains("juill"))
			return 6;
		else if (moisText.contains("ao"))
			return 7;
		else if (moisText.contains("sept"))
			return 8;
		else if (moisText.contains("oct"))
			return 9;
		else if (moisText.contains("nov"))
			return 10;
		else if (moisText.contains("decem"))
			return 11;
		else
			return -1;
		
	}

	private static Match extractAndComposeNewMatchBWIN(Element eNewMatch,
			Cotes cotes) {
		Elements names = eNewMatch.getElementsByClass("label");

		String equipe1 = names.first().text();
		String equipe2 = names.get(2).text();

		return new Match(equipe1, equipe2, Score.NonJoue, cotes);
	}

	public static Match extractAndComposeNewMatchPRONOSOFT(Element eNewMatch,
			Cotes cotes) {
		Element names = eNewMatch.getElementsByClass("match_analyse").first();

		String[] splitName = names.text().split("-");
		String name1 = splitName[0].replace(String.valueOf((char) 160), " ")
				.trim();
		String equipe1 = name1.startsWith("&nbsp;") ? name1.substring(6)
				: name1;
		String equipe2 = splitName[1].trim();

		return new Match(equipe1, equipe2, Score.NonJoue, cotes);
	}

	private static Cotes extractCotesBWIN(Element eNewMatch) {

		Elements cotes = eNewMatch.getElementsByClass("odd");

		double cote1 = Double.parseDouble(cotes.first().text());
		double coteX = Double.parseDouble(cotes.get(1).text());
		double cote2 = Double.parseDouble(cotes.get(2).text());

		return new Cotes(cote1, coteX, cote2);
	}

	private static boolean possedeCotesValides(Element match) {
		String eCotes = match.getElementsByClass("nr").first()
				.nextElementSibling().text();
		return !(eCotes.equals("Non disponible") || eCotes.startsWith("annul"));
	}
	
	private static Cotes extractCotesPRONOSOFT(Element match) {

		Element eCote1 = match.getElementsByClass("nr").first()
				.nextElementSibling();
		Element eCoteX = eCote1.nextElementSibling();
		Element eCote2 = eCoteX.nextElementSibling();

		return new Cotes(Double.parseDouble(eCote1.text().replace(',', '.')),
				Double.parseDouble(eCoteX.text().replace(',', '.')),
				Double.parseDouble(eCote2.text().replace(',', '.')));
	}

	// Algorithme de reconnaissance du match par noms d'équipes qui peuvent etre
	// différents. Il faut que chaque equipe
	// ait un mot similaire par rapport à l'équipe attendue pour que le match
	// soit reconnu.
	public static boolean reconnaissanceEquipesMatch(String equipe1Doc,
			String equipe2Doc, Match mExpected) {
		// On vérifie les équipes dans le meme ordre car on sait que l'équipe 1
		// signifie toujours l'équipe à domicile et c'est pareil partout.
		// (c'estune valeur sûr !)

		boolean verifEquipe1 = verifEquipe(equipe1Doc, mExpected.getEquipe1());
		if (verifEquipe1)
			return verifEquipe(equipe2Doc, mExpected.getEquipe2());

		return false;
	}

	private static boolean verifEquipe(String equipeDoc, String equipe) {
		boolean memeEquipe = false;
		String[] splitE1 = equipe.split(" ");
		String[] splitE2 = equipeDoc.split(" ");

		for (String motE1 : splitE1) {
			for (String motE2 : splitE2) {
				if (motE1.toLowerCase().equals(motE2.toLowerCase()))
					if (motE1.length() > 2)
						return true;
			}
		}
		return false;

	}

	public static boolean estMatchTerminePRONOSOFT(Element eMatch) {
		return eMatch.parent().getElementsByClass("tooltip").first().text().contains("Score Final :")
				&& !eMatch.parent().getElementsByClass("tooltip").first().text().contains("Score Final : |");
	}
}
