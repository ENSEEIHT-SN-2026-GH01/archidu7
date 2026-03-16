package simulateur;

import org.junit.*;
import static org.junit.Assert.*;

public class LienTest {

	private Lien L1, L2, L3;
	
	@Before
	public void setUp () {
		L1 = new Lien("E1");
		L2 = new Lien("E2");
		L3 = new Lien("S");
	}

	@Test
	public void TestNom() {
		assertEquals("Pb de nom", "E1",L1.getNom());
		assertEquals("Pb de nom", "E2",L2.getNom());
		assertEquals("Pb de nom", "S",L3.getNom());
	}

	@Test 
	public void TestValDefaut() {
		assertTrue("Pb val défaut",L3.getValeur() == Etat.ND);
	}

	@Test
	public void TestChangerVal() {
		L1.setValeur(Etat.UP);
		L2.setValeur(Etat.DW);
		assertEquals("Pb de set/get", Etat.UP,L1.getValeur());
		assertEquals("Pb de set/get", Etat.DW,L2.getValeur());
	}

	/*public static void main(String[] Args) {
		org.junit.runner.JUnitCore();
	}*/
}
