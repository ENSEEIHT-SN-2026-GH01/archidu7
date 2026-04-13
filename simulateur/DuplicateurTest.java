package simulateur;

import org.junit.*;
import static org.junit.Assert.*;

public class DuplicateurTest {

	private Lien L1, L2, L3, L4, L5, L6;
	private Duplicateur D1, D2;
	
	@Before
	public void setUp () throws ErreurIndex {
		L1 = new Lien("E1");
		L2 = new Lien("E2");
		L3 = new Lien("S11");
		L4 = new Lien("S12");
		L5 = new Lien("S21");
		L6 = new Non("S22");
		D1 = new Duplicateur(L1,L3,L4);
		D2 = new Duplicateur();
		D2.brancherEntree(L2,1);
		D2.brancherSortie(L5,1);
		D2.brancherSortie(L6,2);
	}

	@Test
	public void TestNom() throws ErreurIndex {
		assertEquals("Pb de nom", "S11",D1.getLienSortie(1).getNom());
		assertEquals("Pb de nom", "S12",D1.getLienSortie(2).getNom());
		assertEquals("Pb de nom", "S21",D2.getLienSortie(1).getNom());
		assertEquals("Pb de nom", "S22",D2.getLienSortie(2).getNom());
	}

	@Test 
	public void TestValDefaut() throws ErreurIndex {
		assertTrue("Pb val défaut",D1.getLienSortie(1).getValeur() == Etat.ND);
		assertTrue("Pb val défaut",D1.getLienSortie(2).getValeur() == Etat.ND);
		assertTrue("Pb val défaut",D2.getLienSortie(1).getValeur() == Etat.ND);
		assertTrue("Pb val défaut",D2.getLienSortie(2).getValeur() == Etat.ND);
	}

	@Test
        public void TestCalculDefaut() throws ErreurIndex {
		D1.calculer();
		D2.calculer();
		assertTrue("Pb val défaut",D1.getLienSortie(1).getValeur() == Etat.ND);
		assertTrue("Pb val défaut",D1.getLienSortie(2).getValeur() == Etat.ND);
		assertTrue("Pb val défaut",D2.getLienSortie(1).getValeur() == Etat.ND);
		assertTrue("Pb val défaut",D2.getLienSortie(2).getValeur() == Etat.ND);
        }

	@Test
        public void TestCalculDW() throws ErreurIndex {
		L1.setValeur(Etat.DW);
		L2.setValeur(Etat.DW);
                D1.calculer();
                D2.calculer();
		assertTrue("Pb val down sortie",D1.getLienSortie(1).getValeur() == Etat.DW);
                assertTrue("Pb val down sortie",D1.getLienSortie(2).getValeur() == Etat.DW);
                assertTrue("Pb val down sortie",D2.getLienSortie(1).getValeur() == Etat.DW);
                assertTrue("Pb val down sortie",D2.getLienSortie(2).getValeur() == Etat.UP);
		assertTrue("Pb val down lien",L3.getValeur() == Etat.DW);
                assertTrue("Pb val down lien",L4.getValeur() == Etat.DW);
                assertTrue("Pb val down lien",L5.getValeur() == Etat.DW);
                assertTrue("Pb val down lien",L6.getValeur() == Etat.UP);
        }

	@Test
        public void TestCalculUP() throws ErreurIndex {
		L1.setValeur(Etat.UP);
                L2.setValeur(Etat.UP);
                D1.calculer();
                D2.calculer();
                assertTrue("Pb val up sortie",D1.getLienSortie(1).getValeur() == Etat.UP);
                assertTrue("Pb val up sortie",D1.getLienSortie(2).getValeur() == Etat.UP);
                assertTrue("Pb val up sortie",D2.getLienSortie(1).getValeur() == Etat.UP);
                assertTrue("Pb val up sortie",D2.getLienSortie(2).getValeur() == Etat.DW);
                assertTrue("Pb val up lien",L3.getValeur() == Etat.UP);
                assertTrue("Pb val up lien",L4.getValeur() == Etat.UP);
                assertTrue("Pb val up lien",L5.getValeur() == Etat.UP);
                assertTrue("Pb val up lien",L6.getValeur() == Etat.DW);
        }

	@Test
	public void TestChangerVal() throws ErreurIndex {
		L1.setValeur(Etat.UP);
		L2.setValeur(Etat.DW);
		assertEquals("Pb de set/get", Etat.UP,L1.getValeur());
		assertEquals("Pb de set/get", Etat.DW,L2.getValeur());
	}
}
