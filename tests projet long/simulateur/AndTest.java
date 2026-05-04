package simulateur;

import org.junit.*;
import static org.junit.Assert.*;

public class AndTest {

	private Lien L1, L2, L3, L4, L5, L6;
	private And P1, P2;
	
	@Before
	public void setUp () throws ErreurIndex {
		L1 = new Lien("E1");
		L2 = new Lien("E2");
		L3 = new Lien("E3");
		L4 = new Lien("E4");
		L5 = new Lien("E5");
		L6 = new Lien("S1");
		P1 = new And(L1,L2,L6);
		P2 = new And(L3,L4,"S2");
	}

	@Test
	public void TestNom() throws ErreurIndex {
		assertEquals("Pb de nom", "S1",P1.getLienSortie().getNom());
		assertEquals("Pb de nom", "S2",P2.getLienSortie().getNom());
	}

	@Test 
	public void TestValDefaut() throws ErreurIndex {
		assertTrue("Pb val défaut",P1.getLienSortie().getValeur() == Etat.ND);
		assertTrue("Pb val défaut",P2.getLienSortie().getValeur() == Etat.ND);
	}

	@Test
        public void TestCalculDefaut() throws ErreurIndex {
		P1.calculer();
		P2.calculer();
                assertTrue("Pb val défaut",P1.getLienSortie().getValeur() == Etat.ND);
                assertTrue("Pb val défaut",P2.getLienSortie().getValeur() == Etat.ND);
        }

	@Test
        public void TestCalculDW1() throws ErreurIndex {
		L1.setValeur(Etat.DW);
		L3.setValeur(Etat.DW);
                P1.calculer();
                P2.calculer();
                assertTrue("Pb calcul down 1",P1.getLienSortie().getValeur() == Etat.DW);
                assertTrue("Pb calcul down 1",P2.getLienSortie().getValeur() == Etat.DW);
		L2.setValeur(Etat.UP);
		L4.setValeur(Etat.UP);
		P1.calculer();
                P2.calculer();
                assertTrue("Pb calcul down 1",P1.getLienSortie().getValeur() == Etat.DW);
                assertTrue("Pb calcul down 1",P2.getLienSortie().getValeur() == Etat.DW);
        }

	@Test
        public void TestCalculDW2() throws ErreurIndex {
                L2.setValeur(Etat.DW);
                L4.setValeur(Etat.DW);
                P1.calculer();
                P2.calculer();
                assertTrue("Pb calcul down 2",P1.getLienSortie().getValeur() == Etat.DW);
                assertTrue("Pb calcul down 2",P2.getLienSortie().getValeur() == Etat.DW);
                L1.setValeur(Etat.UP);
                L3.setValeur(Etat.UP);
                P1.calculer();
                P2.calculer();
                assertTrue("Pb calcul down 2",P1.getLienSortie().getValeur() == Etat.DW);
                assertTrue("Pb calcul down 2",P2.getLienSortie().getValeur() == Etat.DW);
        }

	@Test
        public void TestCalculDW12() throws ErreurIndex {
                L1.setValeur(Etat.DW);
                L3.setValeur(Etat.DW);
		L2.setValeur(Etat.DW);
                L4.setValeur(Etat.DW);
                P1.calculer();
                P2.calculer();
                assertTrue("Pb calcul down 1&2",P1.getLienSortie().getValeur() == Etat.DW);
                assertTrue("Pb calcul down 1&2",P2.getLienSortie().getValeur() == Etat.DW);
        }

	@Test
        public void TestCalculUP() throws ErreurIndex {
                L1.setValeur(Etat.UP);
                L3.setValeur(Etat.UP);
                L2.setValeur(Etat.UP);
                L4.setValeur(Etat.UP);
                P1.calculer();
                P2.calculer();
                assertTrue("Pb calcul down 1&2",P1.getLienSortie().getValeur() == Etat.UP);
                assertTrue("Pb calcul down 1&2",P2.getLienSortie().getValeur() == Etat.UP);
        }

	@Test
	public void TestChangerVal() throws ErreurIndex {
		L1.setValeur(Etat.UP);
		L2.setValeur(Etat.DW);
		assertEquals("Pb de set/get", Etat.UP,L1.getValeur());
		assertEquals("Pb de set/get", Etat.DW,L2.getValeur());
	}
}
