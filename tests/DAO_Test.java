import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import modele.Cotes;
import modele.Match;
import modele.Match.Score;

import org.junit.Test;

import dao.Files_IO;
import modele.algo.AlgoRayanoWin_1;


public class DAO_Test {

	@Test
	public void BuildChaine_Test() {
		//Arrange
		Cotes cotes = new Cotes(1.25, 3.5, 4.3);
		Match m = new Match("Paris SG", "Lille LOSC", Score.Nul);
		m.setCotes(cotes);
		AlgoRayanoWin_1 robot = new AlgoRayanoWin_1(Score.Nul, 2);		
		robot.Parier(m, cotes);
		
		//Action
		String chaine = Files_IO.buildStringMatch(m, robot);
		
		//Asserts
		assertEquals("Paris SG-Lille LOSC-Nul-0.8-1.25:3.5:4.3", chaine);
	}
	
	@Test
	public void TransformChaine_Test() {
		//Arrange
		String chaine = "Paris SG-Lille LOSC-Nul-0.8-1.25:3.5:4.3";
		Collection<String> chaines = new ArrayList<String>();
		chaines.add(chaine);
		AlgoRayanoWin_1 robot = new AlgoRayanoWin_1(Score.Nul, 2);
		
		//Action
		Collection<Match> matchs = Files_IO.transformListStringToMatch(chaines);
		
		//Asserts
		assertEquals(1, matchs.size());
		Match m = new ArrayList<>(matchs).get(0);
		assertEquals("Paris SG", m.getEquipe1());
		assertEquals("Lille LOSC", m.getEquipe2());
		assertEquals(Score.Nul, m.getScoreResultat());
		assertEquals(0.8, m.getMise(), 0.0);
		assertEquals(1.25, m.getCotes().X1, 0.0);
		assertEquals(3.5, m.getCotes().N, 0.0);
		assertEquals(4.3, m.getCotes().X2, 0.0);
	}
	
	@Test
	public void TransformChaineWithEmptyString_Test() {
		//Arrange
		String chaine = "";
		Collection<String> chaines = new ArrayList<String>();
		chaines.add(chaine);
		AlgoRayanoWin_1 robot = new AlgoRayanoWin_1(Score.Nul, 2);
		
		//Action
		Collection<Match> matchs = Files_IO.transformListStringToMatch(chaines);
		
		//Asserts
		assertEquals(0, matchs.size());
	}

}
