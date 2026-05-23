package simulateur;

public class StructSortie {

	private String nom;
	private TableauConnecteur T;

	public StructSortie(String nom, TableauConnecteur T) {
		this.nom = nom;
		this.T = T;
	}

	public String getNom() {
		return nom;
	}

	public String getNom(int i){
		return T.getConnecteur(i).getNom();
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
