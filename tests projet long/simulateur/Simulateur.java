package simulateur;

public class Simulateur {

	private TableauLien entrees;
	private TableauLien sorties;
	private FileListe file;

	public Simulateur(int nbEntrees, int nbSorties) {
		entrees = new TableauLien(nbEntrees);
		sorties = new TableauLien(nbSorties);

		String[] nomsEntrees = new String[nbEntrees];
		for (int i = 0; i < nbEntrees; i++) nomsEntrees[i] = "E" + (i + 1); {
		    entrees.initialiser(nomsEntrees);
        }

		String[] nomsSorties = new String[nbSorties];
		for (int i = 0; i < nbSorties; i++) nomsSorties[i] = "S" + (i + 1); {
		    sorties.initialiser(nomsSorties);
        }

		file = null;
	}

	public void ajouterComposant(Composant c) {
		if (file == null) {
			file = new FileListe(c);
		} else {
			file.ajouter(c);
		}
	}

	public Lien getLienEntree(int i) {
		return entrees.getLien(i + 1);
	}

	public Lien getLienSortie(int i) {
		return sorties.getLien(i + 1);
	}

	public void setEntree(int i, Etat e) {
		entrees.set(i + 1, e);
	}

	public Etat getSortieEtat(int i) {
		return sorties.get(i + 1);
	}

	public int getNbEntrees() {
		return entrees.getTaille();
	}

	public int getNbSorties() {
		return sorties.getTaille();
	}

	public void calculer() {
		if (file != null) {
			file.traiter();
		}
	}
}
