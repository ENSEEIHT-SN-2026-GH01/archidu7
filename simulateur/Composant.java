package simulateur;

import java.util.*;

public abstract class Composant implements Structure {

	private TableauConnecteur entrees, sorties;

	private Structure pere;

	protected TableauConnecteur getE(){
		return entrees;
	}

	protected TableauConnecteur getS(){
                return sorties;
        }

	protected void setE(TableauConnecteur T) {
		entrees = T;
	}

	protected void setS(TableauConnecteur T) {
		sorties = T;
	}

	public Composant(int nb_entrees, int nb_sorties) {
                entrees = new TableauConnecteur(nb_entrees);
                sorties = new TableauConnecteur(nb_sorties);
        }

	public Composant(int nb_entrees, int nb_sorties, String[] NomSorties) throws ErreurIndex {
		this(nb_entrees, nb_sorties);
		sorties.initialiser(NomSorties);
	}

	/*public Composant(int nb_entrees, int nb_sorties, TableauConnecteur entrees) {
		this(nb_entrees,nb_sorties);
		//TODO
	}*/

	protected Etat getEntree(int i) throws ErreurIndex {
		return entrees.get(i);
	}

	public int getNbEntree() {
		return entrees.getTaille();
	}

	public Connecteur getConnecteurSortie(int i) throws ErreurIndex {
		return sorties.getConnecteur(i);
	}

	public void brancherEntree(Connecteur l, int i) throws ErreurIndex {
		entrees.brancher(l,i);
		l.setComposant(this);
	}

	public void brancherSortie(Connecteur l, int i) throws ErreurIndex {
		sorties.brancher(l,i);
		l.setOrigine(this);
	}

	public void debrancherEntree(Connecteur l) throws ErreurIndex {
                entrees.debrancher(l);
		l.setComposant(null);
        }

        public void debrancherSortie(Connecteur l) throws ErreurIndex {
                sorties.debrancher(l);
		l.setOrigine(null);
        }

	protected void setSortie(int i, Etat b) throws ErreurIndex {
		sorties.set(i,b);
	}

	public int getNbSortie() {
		return sorties.getTaille();
	}
	
	public abstract void calculer() throws ErreurIndex ;

	public Structure getStructure() {
		return pere;
	}

	public void ajouter(List<Composant> L) throws ErreurIndex {
		L.add(this);
	}

}
