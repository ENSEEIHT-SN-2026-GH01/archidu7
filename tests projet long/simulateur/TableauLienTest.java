package simulateur;

import org.junit.*;
import static org.junit.Assert.*;

public class TableauLienTest {

	private Lien L1, L2, L3, L4, L5, L6;
	private TableauLien T1, T2;
	
	@Before
	public void setUp () {
		L1 = new Lien("E1");
		L2 = new Lien("E2");
		L3 = new Lien("E3");
		L4 = new Lien("E4");
		L5 = new Lien("E5");
		L6 = new Lien("S1");
		T1 = new TableauLien(2);
		T2 = new TableauLien(3);
	}
	@Test
        public void TestTaille() {
                assertEquals("Pb de taille", 2,T1.getTaille());
                assertEquals("Pb de taille", 3,T2.getTaille());
        }

	@Test
	public void TestCreation() throws ErreurIndex {
		assertTrue("Devrait être null", T1.getLien(1) == null);
		assertTrue("Devrait être null", T1.getLien(2) == null);
		assertTrue("Devrait être null", T2.getLien(1) == null);
		assertTrue("Devrait être null", T2.getLien(2) == null);
		assertTrue("Devrait être null", T2.getLien(3) == null);
	}

	@Test
        public void TestBranchement() throws ErreurIndex {
		T1.brancher(L1,1);
		T1.brancher(L2,2);
		T2.brancher(L3,1);
		T2.brancher(L4,2);
		T2.brancher(L5,3);
                assertEquals("Erreur nom -> Branchement erroné", T1.getLien(1).getNom(),"E1");
                assertEquals("Erreur nom -> Branchement erroné", T1.getLien(2).getNom(),"E2");
                assertEquals("Erreur nom -> Branchement erroné", T2.getLien(1).getNom(),"E3");
                assertEquals("Erreur nom -> Branchement erroné", T2.getLien(2).getNom(),"E4");
                assertEquals("Erreur nom -> Branchement erroné", T2.getLien(3).getNom(),"E5");

	}

	@Test
	public void TestInitialisation() throws ErreurIndex {
		String[] Nom2 = {"Jean", "Benoît"};
		String[] Nom3 = {"Francis", "Pierre", "Louis"};
		T1.initialiser(Nom2);
		T2.initialiser(Nom3);
                assertEquals("Erreur nom -> Branchement erroné", T1.getLien(1).getNom(),"Jean");
                assertEquals("Erreur nom -> Branchement erroné", T1.getLien(2).getNom(),"Benoît");
                assertEquals("Erreur nom -> Branchement erroné", T2.getLien(1).getNom(),"Francis");
                assertEquals("Erreur nom -> Branchement erroné", T2.getLien(2).getNom(),"Pierre");
                assertEquals("Erreur nom -> Branchement erroné", T2.getLien(3).getNom(),"Louis");
	}

	@Test(expected = ErreurIndex.class)
	public void TestErreurIndexTropPetit() throws ErreurIndex {
		T2.getLien(0);
	}

	@Test(expected = ErreurIndex.class)
        public void TestErreurIndexTropGrand() throws ErreurIndex {
                T2.getLien(4);
        }

	@Test(expected = ErreurIndex.class)
        public void TestErreurIndexNomTropPetit() throws ErreurIndex {
		String[] Nom2 = {"Jean", "Benoît"};
                T2.initialiser(Nom2);
        }

	@Test(expected = ErreurIndex.class)
        public void TestErreurIndexNomTropGrand() throws ErreurIndex {
		String[] Nom3 = {"Francis", "Pierre", "Louis"};
                T1.initialiser(Nom3);
        }
}
