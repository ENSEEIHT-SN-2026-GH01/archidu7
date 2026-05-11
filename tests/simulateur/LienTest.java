package simulateur;

import org.junit.*;
import static org.junit.Assert.*;

public class LienTest {

	private Lien L1, L2, L3;

	@Before
	public void setUp() {
		L1 = new Lien("E1");
		L2 = new Lien("E2");
		L3 = new Lien("S");
	}

	@Test
	public void TestNom() {
		assertEquals("Pb de nom", "E1", L1.getNom());
		assertEquals("Pb de nom", "E2", L2.getNom());
		assertEquals("Pb de nom", "S", L3.getNom());
	}

	@Test
	public void TestValDefaut() {
		assertTrue("Pb val défaut", L3.getValeur() == Etat.ND);
		assertTrue("Pb val défaut", L3.getOrigine() == null);
		assertTrue("Pb val défaut", L3.getComposant() == null);
	}

	@Test
	public void TestChangerVal() {
		L1.setValeur(Etat.UP);
		L2.setValeur(Etat.DW);
		assertEquals("Pb de set/get", Etat.UP, L1.getValeur());
		assertEquals("Pb de set/get", Etat.DW, L2.getValeur());
	}

	@Test
	public void TestComposantOrigineSet() {

		Not N1 = new Not(L1, L2);
		assertTrue("Pb Création Not", L2.getOrigine() == N1 && L1.getComposant() == N1);
		assertTrue("Pb Création Not", N1.getConnecteurSortie(1) == L2);
		Not N2 = new Not(L1, L3);
		assertTrue("Pb Création Not 2", L3.getOrigine() == N2 && L1.getComposant() == N2);
		assertTrue("Pb Création Not 2", N2.getConnecteurSortie(1) == L3);
		L3.setOrigine(N1);
		assertTrue("Pb Cohérence 2", N2.getConnecteurSortie(1) == null);
	}

	@Test
	public void TestNomNouveau() {
		DicoConnecteur D = new DicoConnecteur();
		D.ajouter(L1, L1.getNom());
		assertTrue("Pb Création nom 1", L1.NomNouveau(D).equals(new String(L1.getNom() + " - " + 1)));
		assertTrue("Pb Création nom 1- 2", L1.NomNouveau(D).equals(new String(L1.getNom() + " - " + 1)));
		Connecteur LC = D.getConnecteur(new String(L1.getNom() + " - " + 1));
		assertTrue("Pb Création nom 2", L1.NomNouveau(D).equals(new String(L1.getNom() + " - " + 2)));
	}

	@Test
	public void TestGetSignal() {
		DicoConnecteur D = new DicoConnecteur();
		D.ajouter(L1, L1.getNom());
		assertTrue("Pb getSignal null", L1.getSignal(D) == L1);
		Not N = new Not(L1, L2);
		assertTrue("Pb get Signal M1", L1.getSignal(D).getNom().equals(new String(L1.getNom() + " - " + 2)));
		assertTrue("Pb get Signal M1 - rebranchement",
				D.getConnecteur(new String(L1.getNom() + " - " + 1)).getComposant() == N);
		assertTrue("Pb get Signal M1 - 2", L1.getComposant() instanceof Multiplicateur);
		assertTrue("Pb get Signal M1 - 3", L1.getSignal(D).getNom().equals(new String(L1.getNom() + " - " + 3)));
	}

	public static void main(String[] Args) {
		org.junit.runner.JUnitCore.main();
	}
}
