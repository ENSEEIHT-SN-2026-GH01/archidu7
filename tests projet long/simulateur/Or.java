package simulateur;

public class Or extends Porte {

	public Or(Lien Entree1, Lien Entree2, String NomSortie) throws ErreurIndex {
                super(Entree1,Entree2,NomSortie);
        }

        public Or(Lien Entree1, Lien Entree2, Lien Sortie) throws ErreurIndex {
                super(Entree1,Entree2,Sortie);
        }

	public void calculer() throws ErreurIndex {
		if (super.getEntree(1) == Etat.UP || super.getEntree(2) == Etat.UP) {
			super.setSortie(1,Etat.UP);
		} else {
			if (super.getEntree(1) == Etat.DW && super.getEntree(2) == Etat.DW) {
				super.setSortie(1,Etat.DW);
			} else {
				super.setSortie(1,Etat.ND);
			}
		}
	}
}
