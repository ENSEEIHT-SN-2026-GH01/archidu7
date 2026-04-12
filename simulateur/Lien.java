package simulateur;

public class Lien implements Connecteur {

	private Etat etat;
	private String nom;
	private Composant origine, composantSuivant;

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

	public void setComposant(Composant c) throws ErreurIndex {
		if (composantSuivant != null) {
			composantSuivant.debrancherEntree(this);
		}
		composantSuivant = c;
	}

	public Composant getOrigine(){
                return origine;
        }

        public void setOrigine(Composant c) throws ErreurIndex{
		if (origine != null) {
			origine.debrancherSortie(this);
		}
                origine = c;
        }

}
