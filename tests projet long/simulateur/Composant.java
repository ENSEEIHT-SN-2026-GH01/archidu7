package simulateur;

public abstract class Composant {

	private TableauLien entrees, sorties;

	public Composant(int nb_entrees, int nb_sorties, String[] NomEntrees) {
		entrees = new TableauLien(nb_entrees);
		sorties = new TableauLien(nb_sorties);
		sorties.initialiser(NomEntrees);
	}

	/*public Composant(int nb_entrees, int nb_sorties, TableauLien entrees) {
		this(nb_entrees,nb_sorties);
		//TODO
	}*/

	protected Etat getEntree(int i) {
		return entrees.get(i);
	}

	public int getNbEntree() {
		return entrees.getTaille();
	}

	public Lien getLienSortie(int i) {
		return sorties.getLien(i);
	}

	public void brancherEntree(Lien l, int i) {
		entrees.brancher(l,i);
	}

	public void brancherSortie(Lien l, int i) {
		sorties.brancher(l,i);
	}

	protected void setSortie(int i, Etat b) {
		sorties.set(i,b);
	}

	public int getNbSortie() {
		return sorties.getTaille();
	}
	
	public abstract void calculer();

}
