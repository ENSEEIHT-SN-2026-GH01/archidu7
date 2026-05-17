package simulateur;

public class Lien {

	private Etat etat;
	private String nom;
	private Composant composantSuivant;

	public Lien(String nom) {
		etat = Etat.ND;
		this.nom = nom;
		composantSuivant = null;
	}

	public void setValeur(Etat b) {
		etat = b;
	}

	public Etat getValeur() {
		return etat;
	}

	public String getNom() {
		return new String(nom);
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public Composant getComposant(){
		return composantSuivant;
	}

	public void setComposant(Composant c) {
		composantSuivant = c;
	}

}
