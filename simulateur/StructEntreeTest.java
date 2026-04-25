package simulateur;

import org.junit.*;
import static org.junit.Assert.*;

public class StructEntreeTest {

	private StructEntree S;
	private TableauConnecteur T;
	private Connecteur C1, C2, C3, C4;
	private Composant c1, c2;
	private BouttonEntree B1,B2;

	@Before
	public void setUp() {
		C1 = new Lien("A");
		C2 = new Lien("B");
		C3 = new Lien("A * B");
		C4 = new Lien("/(A * B)");
		c1 = new And(C1,C2,C3);
		c2 = new Not(C3,C4);
		T = new TableauConnecteur(2);
		T.brancher(C1,1);
		T.brancher(C2,2);
		S = new StructEntree("Test",T);
		B1 = new BouttonEntree(S,1);
		B2 = new BouttonEntree(S,2);
	}

	@Test
	public void getMethodesTest() {
		assertTrue("Pas le bon nom", S.getNom().equals("Test"));
		assertTrue("Pas le bon nombre", S.getNombre() == 2);
		assertTrue("Pas le bon etat", S.getValeur(1) == Etat.ND && S.getValeur(2) == Etat.ND);
		assertTrue("Pas le bon connecteur", S.getConnecteur(1) == C1 && S.getConnecteur(2) == C2);
	}

	@Test
	public void calculTest () {
		assertTrue("Pas le bon etat par défaut", C1.getValeur() == Etat.ND && C2.getValeur() == Etat.ND && C3.getValeur() == Etat.ND && C4.getValeur() == Etat.ND); 
		S.setValeur(1,Etat.DW);
		assertTrue("Etat - 1 - C1", C1.getValeur() == Etat.DW); 
		assertTrue("Etat - 1 - C2", C2.getValeur() == Etat.ND); 
		assertTrue("Etat - 1 - C3", C3.getValeur() == Etat.DW); 
		assertTrue("Etat - 1 - C4", C4.getValeur() == Etat.UP); 
		S.setValeur(1,Etat.UP);
		assertTrue("Etat - 2", C1.getValeur() == Etat.UP && C2.getValeur() == Etat.ND && C3.getValeur() == Etat.ND && C4.getValeur() == Etat.ND); 
		S.setValeur(2,Etat.UP);
		assertTrue("Etat - 3", C1.getValeur() == Etat.UP && C2.getValeur() == Etat.UP && C3.getValeur() == Etat.UP && C4.getValeur() == Etat.DW); 
	}

	@Test
	public void calculBouttonTest () {
                B1.set(Etat.DW);
                assertTrue("Etat - 1B", C1.getValeur() == Etat.DW && C2.getValeur() == Etat.ND && C3.getValeur() == Etat.DW && C4.getValeur() == Etat.UP);
                B1.set(Etat.UP);
		assertTrue("Etat - 2B", C1.getValeur() == Etat.UP && C2.getValeur() == Etat.ND && C3.getValeur() == Etat.ND && C4.getValeur() == Etat.ND);
                B2.set(Etat.UP);
                assertTrue("Etat - 3B", C1.getValeur() == Etat.UP && C2.getValeur() == Etat.UP && C3.getValeur() == Etat.UP && C4.getValeur() == Etat.DW);
        }
}
