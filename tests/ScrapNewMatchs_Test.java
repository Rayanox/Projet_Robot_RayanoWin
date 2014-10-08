import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import modele.Cotes;
import modele.Match;
import modele.Match.Score;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import scrapping.Scrap;
import scrapping.Scrap.Site;
import Scrap.Echangeur;
import TraitementFichier.FileFactory;

public class ScrapNewMatchs_Test {

	private static String slash = System.getProperty("os.name").toLowerCase()
			.contains("win") ? "\\" : "/";

	private static String cheminNewMatchsHtmlBWINFile = "tests" + slash + "files"
			+ slash + "GetNewMatchsResponseBWIN.xml";
	
	private static String cheminNewMatchsHtmlPRONOSOFTFile = "tests" + slash + "files"
			+ slash + "GetNewMatchsResponsePRONOSOFT.xml";

	private static Site siteUtilise = Site.Pronosoft;
	
	@Test
	public void ScrapNewMatchSuccess_Test() {

		// Arrange
		String equipe1 , equipe2 ;
		if(siteUtilise.equals(Site.Bwin)) {
			equipe1 = "Bate Borisov";
			equipe2 = "Debreceni";
		}else {//Pronosoft
			equipe1 = "Aradippou";
			equipe2 = "YoungBoysBerne";
		}
		
		Calendar currentDate = new GregorianCalendar(2014, 7, 7, 14, 00, 0);
		Document doc = LoadHtml(cheminNewMatchsHtmlPRONOSOFTFile);
		Match mExpected = new Match(equipe1, equipe2, Score.NonJoue, new Cotes(5.00, 3.50, 1.45));
		Match result = null;

		// Action
		try {
			result = siteUtilise.equals(Site.Bwin) ? Scrap.TransformNewMatchsBWIN(doc) : Scrap.TransformNewMatchsPRONOSOFT(doc, currentDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}

		// Assert
		assertEquals(mExpected.getEquipe1(), result.getEquipe1());
		assertEquals(mExpected.getEquipe2(), result.getEquipe2());
		assertEquals(mExpected.getScoreResultat(), result.getScoreResultat());
		assertEquals(mExpected.getCotes().N, result.getCotes().N, 0.0);
		assertEquals(mExpected.getCotes().X1, result.getCotes().X1, 0.0);
		assertEquals(mExpected.getCotes().X2, result.getCotes().X2, 0.0);
	}

	@Test
	public void transformationNomWithMultipleSpaces_Test() {

		if(siteUtilise.equals(Site.Pronosoft)) {
			// Arrange
			String equipe1 , equipe2 ;
			equipe1 = "FK Krasnodar";
			equipe2 = "Diosgyori";			
			
			Document doc = LoadHtml(cheminNewMatchsHtmlPRONOSOFTFile);
			Element specifiElement = doc.getElementsByClass("match").get(9).parent();
			Match mExpected = new Match(equipe1, equipe2, Score.NonJoue, new Cotes(5.00, 3.50, 1.45));
			Match result = null;

			// Action
			try {
				result = Scrap.extractAndComposeNewMatchPRONOSOFT(specifiElement, mExpected.getCotes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail();
			}

			// Assert
			assertEquals(mExpected.getEquipe1(), result.getEquipe1());
			assertEquals(mExpected.getEquipe2(), result.getEquipe2());
			assertEquals(mExpected.getScoreResultat(), result.getScoreResultat());
			assertEquals(mExpected.getCotes().N, result.getCotes().N, 0.0);
			assertEquals(mExpected.getCotes().X1, result.getCotes().X1, 0.0);
			assertEquals(mExpected.getCotes().X2, result.getCotes().X2, 0.0);
		}else {
			Assert.fail("Not already implemented for bwin site");
		}
		
	}
	
	@Test
	public void estMatchDemarrePRONOSOFT_demarreTest() {
		//Arrange
		Calendar currentDate = new GregorianCalendar(2014, 9, 1, 15, 30, 05);
		Document doc = LoadHtml(cheminNewMatchsHtmlPRONOSOFTFile);
		Element specifiElement = doc.getElementsByClass("match").first().parent();
		
		//Action
		boolean demarre = Scrap.estMatchDemarrePRONOSOFT(specifiElement, currentDate);
		
		//Assert
		assertTrue(demarre);
	}
	
	@Test
	public void estMatchDemarrePRONOSOFT_nonDemarreTest() {
		//Arrange
		Calendar currentDate = new GregorianCalendar(2013, 9, 1, 15, 30, 05);
		Document doc = LoadHtml(cheminNewMatchsHtmlPRONOSOFTFile);
		Element specifiElement = doc.getElementsByClass("match").first().parent();
		
		
		//Action
		boolean demarre = Scrap.estMatchDemarrePRONOSOFT(specifiElement, currentDate);
		
		//Assert
		assertFalse(demarre);
	}
	
	@Test
	public void testIntegration() {
		
		 //Partie nouveaux matchs
		 try {
//		 String html = Echangeur.SendGetRequest(new
//		 URL("https://www.bwin.fr/betViewIframe.aspx?SportID=4&bv=bb&selectedLeagues=0 "));
//		 FileFactory.ecritureStringFichier("C:\\Users\\Rayanox\\Pictures\\pageTestNouveauxMatchs.html",
//		 html, false);
			 
//		 Connection con = HttpConnection.connect(new
//		 URL(Scrap.urlNewMatchsPRONOSOFT));
//			 
//		 Document doc = con.get();
//		 Match m = Scrap.TransformNewMatchsPRONOSOFT(doc, Scrap.getCurrentDate());
			 
		 Match m = Scrap.RecupNewMatchs(siteUtilise);
			 
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
