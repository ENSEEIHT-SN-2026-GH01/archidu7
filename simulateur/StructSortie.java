package simulateur;

public class StructSortie {

	private String nom;
	private TableauConnecteur T;

	public StructSortie() {
	}

	public String getNom() {
		return nom;
	}

	public int getNombre(){
		return T.getTaille();
	}

	public Etat getValeur(int i)  {
		return T.get(i);
	}

	public Connecteur getConnecteur(int i)  {
		return T.getConnecteur(i);
	}

}
