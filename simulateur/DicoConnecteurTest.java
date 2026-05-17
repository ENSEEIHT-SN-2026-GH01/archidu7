package simulateur;

import org.junit.*;
import static org.junit.Assert.*;

public final class DicoConnecteurTest {

	private DicoConnecteur D;

	@Before
	public void setUp() {
		D = new DicoConnecteur();
	}

	@Test
	public void TestEnregistrement() {
		Connecteur C = new Lien("A");
		D.ajouter(C,C.getNom());
		assertTrue("Pb Enr ou detection", D.existe(C.getNom()));
		Connecteur Cc = D.getConnecteur(C.getNom());
		assertTrue("Pb Enr ou restitution", Cc == C);
		D.supprimer(C.getNom());
		assertTrue("Pb suppression", !D.existe(C.getNom()));
	}

	@Test
	public void TestCreation() {
		String Nom = new String("A");
		Connecteur C1 = D.getConnecteur(Nom);
		Connecteur C2 = D.getConnecteur(Nom);
		assertTrue("Pb Nom Création", C1.getNom().equals(Nom));
		assertTrue("Pb Création double", C1 == C2);
	}
}
