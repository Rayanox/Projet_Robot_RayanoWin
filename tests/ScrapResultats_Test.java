import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import modele.Match;
import modele.Match.Score;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;

import scrapping.Scrap;
import scrapping.Scrap.Site;
import sun.org.mozilla.javascript.internal.json.JsonParser;
import Scrap.Echangeur;
import TraitementFichier.FileFactory;


public class ScrapResultats_Test {

	private static String slash = System.getProperty("os.name").toLowerCase().contains("win") ? "\\" : "/";
	
	private static String cheminResultHtmlFile = "tests"+slash+"files" + slash + "GetResultatResponseBWIN.xml";
	
	
	
	//FileFactory.ecritureStringFichier("C:\\Users\\Rayanox\\Pictures\\pageResultats.html", html, false);
	
	
	@Test
	public void ReconnaissanceEquipeMatchBWIN_SuccessTest() {

		//Arrange
		String equipe1 = "Deportivo CoatXXXXepeque", equipe2 = "Csd XelXXXXaju MC";
		Match mExpected = new Match("Deportivo Coatepeque", "Csd Xelaju MC");
		boolean result = false;
		
		//Action
		try {
			result = Scrap.reconnaissanceEquipesMatch(equipe1, equipe2, mExpected);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		//Assert	
		assertEquals(true, result);
		
	}
	
	@Test
	public void ReconnaissanceEquipeMatch_EchecTest() {

		//Arrange
		String equipe1 = "DepXXXortivo CoatXXXXepeque", equipe2 = "CXXsd XelXXXXaju MC";
		Match mExpected = new Match("Deportivo Coatepeque", "Csd Xelaju MC");
		boolean result = true;
		
		//Action
		try {
			result = Scrap.reconnaissanceEquipesMatch(equipe1, equipe2, mExpected);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		//Assert	
		assertEquals(false, result);
		
	}
	
	
	@Test//Partie résultat matchs
	public void GetResultatsTestMatchJoueBWIN() {

		//Arrange
		String equipe1 = "Deportivo Coatepeque", equipe2 = "Csd Xelaju MC";
		Document doc = LoadHtml(cheminResultHtmlFile);
		Match mExpected = new Match(equipe1, equipe2);
		Match result = null;
		
		//Action
		try {
			result = Scrap.transformMatchResult(doc, mExpected, Site.MatchEnDirect);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		//Assert	
		assertEquals(equipe1, result.getEquipe1());
		assertEquals(equipe2, result.getEquipe2());
		assertEquals(Score.VictoireEquipe2, result.getScoreResultat());
		
	}
	
	@Test//Partie résultat matchs
	public void GetResultatsTestMatchJouePenaltiesBWIN() {

		//Arrange
				String equipe1 = "CF La Roda", equipe2 = "Hercules";
				Document doc = LoadHtml(cheminResultHtmlFile);
				Match mExpected = new Match(equipe1, equipe2);
				Match result = null;
				
				//Action
				try {
					result = Scrap.transformMatchResult(doc, mExpected, Site.MatchEnDirect);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					fail();
				}
				
				//Assert	
				assertEquals(equipe1, result.getEquipe1());
				assertEquals(equipe2, result.getEquipe2());
				assertEquals(Score.VictoireEquipe2, result.getScoreResultat());
		
	}
	
	@Test//Partie résultat matchs
	public void GetResultatsTestMatchNonJoueBWIN() {

		//Arrange
		String equipe1 = "Guadalajara Chivas", equipe2 = "Leones Negros Udg";
		Document doc = LoadHtml(cheminResultHtmlFile);
		Match mExpected = new Match(equipe1, equipe2);
		Match result = null;
		
		//Action
		try {
			result = Scrap.transformMatchResult(doc, mExpected, Site.MatchEnDirect);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		//Assert	
		assertEquals(equipe1, result.getEquipe1());
		assertEquals(equipe2, result.getEquipe2());
		assertEquals(Score.NonJoue, result.getScoreResultat());
		
	}
	
	@Test//Partie résultat matchs
	public void GetResultatsTestMatchNonPresentBWIN() {

		//Arrange
		String equipe1 = "PSG hehe", equipe2 = "Blabla Rayanox BG";
		Document doc = LoadHtml(cheminResultHtmlFile);
		Match mExpected = new Match(equipe1, equipe2);
		Match result = null;
		
		//Action
		try {
			result = Scrap.transformMatchResult(doc, mExpected, Site.MatchEnDirect);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		//Assert	
		assertEquals(equipe1, result.getEquipe1());
		assertEquals(equipe2, result.getEquipe2());
		assertEquals(Score.NonJoue, result.getScoreResultat());
		
	}
	
	@Test//Partie résultat matchs
	public void GetResultatsTestMatchNulBWIN() {

		//Arrange
		String equipe1 = "Alianza Petrolera", equipe2 = "Atletico Nacional Medellin";
		Document doc = LoadHtml(cheminResultHtmlFile);
		Match mExpected = new Match(equipe1, equipe2);
		Match result = null;
		
		//Action
		try {
			result = Scrap.transformMatchResult(doc, mExpected, Site.MatchEnDirect);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		//Assert	
		assertEquals(equipe1, result.getEquipe1());
		assertEquals(equipe2, result.getEquipe2());
		assertEquals(Score.Nul, result.getScoreResultat());
		
	}
	
	@Test
	public void buildMatchEnDirectResultatUrlByDate_TodayTestMATCHENDIRECT() {

		//Arrange
		Calendar c = new GregorianCalendar(2014, 6, 24);		
		
		//Action
		String result = Scrap.buildResultatUrlByDate(c, true, Scrap.urlResultatsMATCHENDIRECT,true);
		
		//Assert	
		assertEquals(Scrap.urlResultatsMATCHENDIRECT+"24-07-2014", result);
		
	}
	
	@Test
	public void buildMatchEnDirectResultatUrlByDate_YesterdayTestMATCHENDIRECT() {

		//Arrange
		Calendar c = new GregorianCalendar(2014, 6, 24);
		
		//Action
		String result = Scrap.buildResultatUrlByDate(c, false, Scrap.urlResultatsMATCHENDIRECT,true);
		
		//Assert	
		assertEquals(Scrap.urlResultatsMATCHENDIRECT+"23-07-2014", result);
		
	}
	
	@Test
	public void buildMatchEnDirectResultatUrlByDate_SmallDateTestMATCHENDIRECT() {

		//Arrange
		Calendar c = new GregorianCalendar(2014, 6, 5);		
		
		//Action
		String result = Scrap.buildResultatUrlByDate(c, true, Scrap.urlResultatsMATCHENDIRECT,true);
		
		//Assert	
		assertEquals(Scrap.urlResultatsMATCHENDIRECT+"05-07-2014", result);
		
	}
	
	
	
	//@Test
	public void testIntegration() {
		
		 //Partie resultats match
		 try {
			 
		 Connection con = HttpConnection.connect(new
		 URL(Scrap.urlResultatsMATCHENDIRECT+"17-07-2014"));
		 Match matchExpected = new Match("Bate Borisov", "Debreceni");
		 Document doc = con.get();

//		 FileFactory.ecritureStringFichier("C:\\Users\\Rayanox\\Pictures\\pagetmp.html",
//				 doc.toString(), false);
		 Scrap.transformMatchResult(doc, matchExpected, Site.MatchEnDirect);
		 System.out.println("");
		 } catch (Exception e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }
		
		
	}
	
	
	//Helpers
	
	private Document LoadHtml(String path) {
		File f = new File(path);
		String html = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				html += tmp;				
			}
			return Jsoup.parse(html);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Document("");
		}
		//reader.re
	}

}
