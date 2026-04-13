package simulateur;

public class FileListe implements File {

	private class Cellule {

		private Composant c;
		private Cellule suivant;

		private Cellule(Composant c) {
			this.c = c;
			suivant = null;
		}

		private void ajouter(Composant c) {
         	       if (this.c == null || this.c == c) this.c = c;
                	else {
                        	if (suivant == null) suivant = new Cellule(c);
                        	else suivant.ajouter(c);
                	}
        	}

		private void traiter() throws ErreurIndex {
                	c.calculer();
                	if (suivant != null) suivant.traiter();
        	}

	}

	private Cellule Tete;

	public FileListe() {
		Tete = null;
	}

	public FileListe(Composant c) {
		this.Tete = new Cellule(c);
	}

	public void ajouter(Composant c) {
		this.Tete.ajouter(c);
	}

	public void traiter() throws ErreurIndex {
		this.Tete.traiter();
	}
}
