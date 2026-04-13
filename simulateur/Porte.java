package simulateur;

public abstract class Porte extends Composant {

	public Porte(String[] NomSorties) throws ErreurIndex {
		super(2,1, NomSorties);
	}


	public Porte(Lien Entree1, Lien Entree2, String NomSortie) throws ErreurIndex {
		super(2,1);
		super.brancherEntree(Entree1,1);
		super.brancherEntree(Entree2,2);
		Lien s = new Lien(NomSortie);
		super.brancherSortie(s,1);
	}

	public Porte(Lien Entree1, Lien Entree2, Lien Sortie) throws ErreurIndex {
                super(2,1);
                super.brancherEntree(Entree1,1);
                super.brancherEntree(Entree2,2);
                super.brancherSortie(Sortie,1);
        }

	public Lien getLienSortie() throws ErreurIndex {
		return super.getLienSortie(1);
	}

}
