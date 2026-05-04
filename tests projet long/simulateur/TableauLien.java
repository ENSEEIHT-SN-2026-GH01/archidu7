package simulateur;

public class TableauLien {

	private Lien[] liens;

	public TableauLien(int nb){
		// TODO Cas nb <= 0 ???
		liens = new Lien[nb];
	}

	public int getTaille(){
		return liens.length;
	}

	public Lien getLien(int i) throws ErreurIndex {     //TODO On commence le tableau à 1 !!!
		if (i <= 0) throw new ErreurIndex(this.liens.length,i," L'index est trop petit ! ");
		if (i > liens.length) throw new ErreurIndex(this.liens.length,i," L'index est trop grand ! ");
		return liens[i-1];           //TODO On recupert le cable !!! On peut alterer la valeur
	}
	
	public void initialiser(String[] Noms) throws ErreurIndex {
		//TODO Exception taille de Noms != liens.length
		if (Noms.length < liens.length) throw new ErreurIndex(this.liens.length,Noms.length," La liste de nom est trop petite ! ");
                if (Noms.length > liens.length) throw new ErreurIndex(this.liens.length,Noms.length," L'index est trop grand ! ");
		for (int i = 1; i <= liens.length; i++) {
			liens[i-1] = new Lien(Noms[i-1]);
		}
	}

	public void brancher(Lien l, int i) throws ErreurIndex {
		if (i <= 0) throw new ErreurIndex(this.liens.length,i," L'index est trop petit ! ");
                if (i > liens.length) throw new ErreurIndex(this.liens.length,i," L'index est trop grand ! ");
		liens[i-1] = l;
	}

	public Etat get(int i) throws ErreurIndex {
		// TODO Ajouer une exception ?
		if (i <= 0) throw new ErreurIndex(this.liens.length,i," L'index est trop petit ! ");
                if (i > liens.length) throw new ErreurIndex(this.liens.length,i," L'index est trop grand ! ");
		return getLien(i).getValeur();
	}

	public void set(int i, Etat b) throws ErreurIndex {
		// TODO Exception ??
		if (i <= 0) throw new ErreurIndex(this.liens.length,i," L'index est trop petit ! ");
                if (i > liens.length) throw new ErreurIndex(this.liens.length,i," L'index est trop grand ! ");
		getLien(i).setValeur(b);
	}
}
