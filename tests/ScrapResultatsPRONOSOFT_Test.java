import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import modele.Match;
import modele.Match.Score;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import scrapping.Scrap;
import scrapping.Scrap.Site;


public class ScrapResultatsPRONOSOFT_Test {

private static String slash = System.getProperty("os.name").toLowerCase().contains("win") ? "\\" : "/";
	
	private static String cheminResultHtmlFile = "tests"+slash+"files" + slash + "GetResultatResponsePRONOSOFT.xml";
	
	
	
	//FileFactory.ecritureStringFichier("C:\\Users\\Rayanox\\Pictures\\pageResultats.html", html, false);
	
	@Test
	public void estMatchTerminePRONOSOFT_TermineTest() {
		//Arrange
		Document doc = LoadHtml(cheminResultHtmlFile);
		Element specifiElement = doc.getElementsByClass("match").get(0);
		
		//Action
		boolean termine = Scrap.estMatchTerminePRONOSOFT(specifiElement);
		
		//Assert
		assertTrue(termine);
		
	}
	
	@Test
	public void estMatchTerminePRONOSOFT_NonTermineTest() {
		//Arrange
		Document doc = LoadHtml(cheminResultHtmlFile);
		Element specifiElement = doc.getElementsByClass("match").get(68);
		
		//Action
		boolean termine = Scrap.estMatchTerminePRONOSOFT(specifiElement);
		
		//Assert
		assertFalse(termine);
		
	}
	
	@Test
	public void estMatchTerminePRONOSOFT_NonTermine2Test() {
		//Arrange
		Document doc = LoadHtml(cheminResultHtmlFile);
		Element specifiElement = doc.getElementsByClass("match").get(28);
		
		//Action
		boolean termine = Scrap.estMatchTerminePRONOSOFT(specifiElement);
		
		//Assert
		assertFalse(termine);
		
	}
	
	@Test//Partie résultat matchs
	public void GetResultatsTestMatchJouePRONOSOFT() {

		//Arrange
		String equipe1 = "Istra 1961", equipe2 = "Hnk Rijeka";
		Document doc = LoadHtml(cheminResultHtmlFile);
		Match mExpected = new Match(equipe1, equipe2);
		Match result = null;
		
		//Action
		try {
			result = Scrap.transformMatchResultPRONOSOFT(doc, mExpected);
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
	public void GetResultatsTestMatchNonPresentPRONOSOFT() {

		//Arrange
		String equipe1 = "Rayanox ChiXXvas", equipe2 = "Leones Negros Udg";
		Document doc = LoadHtml(cheminResultHtmlFile);
		Match mExpected = new Match(equipe1, equipe2);
		Match result = null;
		
		//Action
		try {
			result = Scrap.transformMatchResultPRONOSOFT(doc, mExpected);
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
	public void GetResultatsTestMatchNonJouePRONOSOFT() {

		//Arrange
		String equipe1 = "Uni.Craoiva", equipe2 = "Viit.Constanta";
		Document doc = LoadHtml(cheminResultHtmlFile);
		Match mExpected = new Match(equipe1, equipe2);
		Match result = null;
		
		//Action
		try {
			result = Scrap.transformMatchResultPRONOSOFT(doc, mExpected);
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
	public void GetResultatsTestMatchNulPRONOSOFT() {

		//Arrange
		String equipe1 = "Mac.Tel Aviv", equipe2 = "NK Maribor";
		Document doc = LoadHtml(cheminResultHtmlFile);
		Match mExpected = new Match(equipe1, equipe2);
		Match result = null;
		
		//Action
		try {
			result = Scrap.transformMatchResultPRONOSOFT(doc, mExpected);
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
	public void buildPronosoftResultatUrlByDate_TodayTestPRONOSOFT() {

		//Arrange
		Calendar c = new GregorianCalendar(2014, 6, 24);		
		
		//Action
		String result = Scrap.buildResultatUrlByDate(c, true, Scrap.urlResultatsPRONOSOFT,false);
		
		//Assert	
		assertEquals(Scrap.urlResultatsPRONOSOFT+"24/07/2014", result);
		
	}
	
	@Test
	public void buildPronosoftResultatUrlByDate_YesterdayTestPRONOSOFT() {

		//Arrange
		Calendar c = new GregorianCalendar(2014, 6, 24);
		
		//Action
		String result = Scrap.buildResultatUrlByDate(c, false, Scrap.urlResultatsPRONOSOFT,false);
		
		//Assert	
		assertEquals(Scrap.urlResultatsPRONOSOFT+"23/07/2014", result);
		
	}
	
	@Test
	public void buildPronosoftResultatUrlByDate_SmallDateTestPRONOSOFT() {

		//Arrange
		Calendar c = new GregorianCalendar(2014, 6, 5);		
		
		//Action
		String result = Scrap.buildResultatUrlByDate(c, true, Scrap.urlResultatsPRONOSOFT,false);
		
		//Assert	
		assertEquals(Scrap.urlResultatsPRONOSOFT+"05/07/2014", result);
		
	}
	
	
	
	@Test
	public void testIntegration() {
		
		 //Partie resultats match
		 try {

//			 FileFactory.ecritureStringFichier("C:\\Users\\Rayanox\\Pictures\\pagetmp.html",
//					 doc.toString(), false);
			 
			 
			 Match matchExpected = new Match("Amkar Perm", "FK Ufa");
			 
			 Match m = Scrap.RecupResultats(matchExpected, Site.Pronosoft);
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
