package simulateur;

import org.junit.*;
import static org.junit.Assert.*;

public class AndTest {

	private Lien L1, L2, L3, L4, L5, L6;
	private Composant P1, P2;

	@Before
	public void setUp() {
		L1 = new Lien("E1");
		L2 = new Lien("E2");
		L3 = new Lien("E3");
		L4 = new Lien("E4");
		L5 = new Lien("E5");
		L6 = new Lien("S1");
		P1 = new And(L1, L2, L6);
		P2 = new And(L3, L4, "S2");
	}

	// @Test
	// public void TestNom() {
	// assertEquals("Pb de nom", "S1",P1.getLienSortie().getNom());
	// assertEquals("Pb de nom", "S2",P2.getLienSortie().getNom());
	// }

	@Test
	public void TestValDefaut() {
		assertTrue("Pb val défaut", L3.getValeur() == Etat.ND);
	}

	@Test
	public void TestChangerVal() {
		L1.setValeur(Etat.UP);
		L2.setValeur(Etat.DW);
		assertEquals("Pb de set/get", Etat.UP, L1.getValeur());
		assertEquals("Pb de set/get", Etat.DW, L2.getValeur());
	}

	/*
	 * public static void main(String[] Args) {
	 * org.junit.runner.JUnitCore();
	 * }
	 */
}
