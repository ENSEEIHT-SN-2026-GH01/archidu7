package simulateur;

public class FileListe implements File {

	private Composant c;
	private FileListe suivant;

	public FileListe() {
		c = null;
		suivant = null;
	}

	public FileListe(Composant c) {
		this.c = c;
		suivant = null;
	}

	public void ajouter(Composant c) {
		if (this.c == null || this.c == c) this.c = c;
		else {
			if (suivant == null) suivant = new FileListe(c);
			else suivant.ajouter(c);
		}
	}

	public void traiter() {
		c.calculer();
		if (suivant != null) suivant.traiter();
	}
}
