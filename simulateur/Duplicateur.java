package simulateur;

public class Duplicateur extends Composant {

	public Duplicateur(Lien entree, int nb) throws ErreurIndex {
		super(1,nb);
		super.brancherEntree(entree,1);
	}

	public Duplicateur(Lien entree, int nb, String[] NomSorties) throws ErreurIndex {
                super(1,nb, NomSorties);
                super.brancherEntree(entree,1);
        }

	public Duplicateur() {
		super(1,2);
	}

	public Duplicateur(Lien entree, Lien S1, Lien S2) throws ErreurIndex {
		super(1,2);
		super.brancherEntree(entree,1);
		super.brancherSortie(S1,1);
		super.brancherSortie(S2,2);
	}

	public void calculer() throws ErreurIndex {
		for (int i = 1; i <= super.getNbSortie(); i++) {
			super.setSortie(i,super.getEntree(1));
		}
	}
}
