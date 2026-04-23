package simulateur;

public class TableauConnecteur {

	private Connecteur[] connecteurs;

	public TableauConnecteur(int nb){
		// TODO Cas nb <= 0 ???
		connecteurs = new Connecteur[nb];
	}

	public int getTaille(){
		return connecteurs.length;
	}

	public Connecteur getConnecteur(int i)  {     //TODO On commence le tableau à 1 !!!
		if (i <= 0) throw new ErreurIndex(this.connecteurs.length,i," L'index est trop petit ! ");
		if (i > connecteurs.length) throw new ErreurIndex(this.connecteurs.length,i," L'index est trop grand ! ");
		return connecteurs[i-1];           //TODO On recupert le cable !!! On peut alterer la valeur
	}
	
	public void initialiser(String[] Noms)  {
		if (Noms.length < connecteurs.length) throw new ErreurIndex(this.connecteurs.length,Noms.length," La liste de nom est trop petite ! ");
                if (Noms.length > connecteurs.length) throw new ErreurIndex(this.connecteurs.length,Noms.length," L'index est trop grand ! ");
		for (int i = 1; i <= connecteurs.length; i++) {
			connecteurs[i-1] = new Lien(Noms[i-1]);
		}
	}

	public void brancher(Connecteur l, int i)  {
		if (i <= 0) throw new ErreurIndex(this.connecteurs.length,i," L'index est trop petit ! ");
                if (i > connecteurs.length) throw new ErreurIndex(this.connecteurs.length,i," L'index est trop grand ! ");
		connecteurs[i-1] = l;
	}

	public Etat get(int i)  {
		if (i <= 0) throw new ErreurIndex(this.connecteurs.length,i," L'index est trop petit ! ");
                if (i > connecteurs.length) throw new ErreurIndex(this.connecteurs.length,i," L'index est trop grand ! ");
		return getConnecteur(i).getValeur();
	}

	public void set(int i, Etat b)  {
		if (i <= 0) throw new ErreurIndex(this.connecteurs.length,i," L'index est trop petit ! ");
                if (i > connecteurs.length) throw new ErreurIndex(this.connecteurs.length,i," L'index est trop grand ! ");
		getConnecteur(i).setValeur(b);
	}

	public int debrancher(Connecteur c)  {
		int cur = 0;
		for (int i = 1; i <= getTaille(); i ++) {
			if (getConnecteur(i) == c) {
				brancher(null, i);
				cur = i;
			}
		}
		return cur;
	}
}
