package simulateur;

public class Lien {

	private Etat etat;
	private String nom;
	private Composant composantSuivant;

	public Lien(String nom) {
		this.etat = Etat.ND;
		this.nom = nom;
		this.composantSuivant = null;
	}

	public void setValeur(Etat b) {
		this.etat = b;
	}

	public Etat getValeur() {
		return this.etat;
	}

	public String getNom() {
		return new String(this.nom);
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public Composant getComposant() {
		return this.composantSuivant;
	}

	public void setComposant(Composant c) {
		this.composantSuivant = c;
	}

}
