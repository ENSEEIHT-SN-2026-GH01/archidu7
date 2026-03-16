package simulateur;

public class TableauLien {

	private Lien[] liens;
	private int nbLiens;

	public TableauLien(int nb){
		// TODO Cas nb <= 0 ???
		liens = new Lien[nb];
		nbLiens = nb;
	}

	public int getTaille(){
		return nbLiens;
	}

	public Lien getLien(int i) {     //TODO On commence le tableau à 1 !!!
		return liens[i-1];           //TODO On recupert le cable !!! On peut alterer la valeur
	}
	
	public void initialiser(String[] Noms) {
		//TODO Exception taille de Noms != nbLiens
		for (int i = 1; i <= nbLiens; i++) {
			liens[i-1] = new Lien(Noms[i-1]);
		}
	}

	public void brancher(Lien l, int i) {
		liens[i-1] = l;
	}

	public Etat get(int i) {
		// TODO Ajouer une exception ?
		return getLien(i).getValeur();
	}

	public void set(int i, Etat b) {
		// TODO Exception ??
		getLien(i).setValeur(b);
	}
}
