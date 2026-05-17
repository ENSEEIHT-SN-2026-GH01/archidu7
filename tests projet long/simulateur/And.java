package simulateur;

public class And extends Composant {

	private String[] NomSortieDefaut = { "S" };

	public And(String[] NomSorties) {
		super(2,1, NomSorties);
	}

	/*public And() {
		super(1,2,this.NomSortieDefaut);
	}*/

	public And(Lien Entree1, Lien Entree2, String NomSortie) {
		super(2,1);
		super.brancherEntree(Entree1,1);
		super.brancherEntree(Entree2,2);
		Lien s = new Lien(NomSortie);
		super.brancherSortie(s,1);
	}

	public And(Lien Entree1, Lien Entree2, Lien Sortie) {
                super(2,1);
                super.brancherEntree(Entree1,1);
                super.brancherEntree(Entree2,2);
                super.brancherSortie(Sortie,1);
        }

	public Lien getLienSortie() {
		return super.getLienSortie(1);
	}

	public void calculer() {
		if (super.getEntree(1) == Etat.DW || super.getEntree(2) == Etat.DW) {
			super.setSortie(1,Etat.DW);
		} else {
			if (super.getEntree(1) == Etat.UP && super.getEntree(2) == Etat.UP) {
				super.setSortie(1,Etat.UP);
			} else {
				super.setSortie(1,Etat.ND);
			}
		}
	}
}
