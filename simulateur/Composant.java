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

	public Composant(List<Connecteur> Entrees, Connecteur Sortie) {
		this(Entrees.size(),1);
		for (int i = 1; i <= Entrees.size(); i++){
			this.brancherEntree(Entrees.get(i-1),i);
		}
		this.brancherSortie(Sortie,1);
	}

	public Composant(int nb_entrees, int nb_sorties, String[] NomSorties)  {
		this(nb_entrees, nb_sorties);
		sorties.initialiser(NomSorties);
	}

	/*public Composant(int nb_entrees, int nb_sorties, TableauConnecteur entrees) {
		this(nb_entrees,nb_sorties);
		//TODO
	}*/

	protected Etat getEntree(int i)  {
		return entrees.get(i);
	}

	public int getNbEntree() {
		return entrees.getTaille();
	}

	public Connecteur getConnecteurSortie(int i)  {
		return sorties.getConnecteur(i);
	}

	public void brancherEntree(Connecteur l, int i)  {
		entrees.brancher(l,i);
		l.setComposant(this);
	}

	public void brancherSortie(Connecteur l, int i)  {
		sorties.brancher(l,i);
		l.setOrigine(this);
	}

	public int debrancherEntree(Connecteur l)  {
                int i = entrees.debrancher(l);
		l.unsetComposant();
		return i;
        }

        public int debrancherSortie(Connecteur l)  {
                int i = sorties.debrancher(l);
		l.unsetOrigine();
		return i;
        }

	protected void setSortie(int i, Etat b)  {
		sorties.set(i,b);
	}

	public int getNbSortie() {
		return sorties.getTaille();
	}
	
	public abstract void calculer()  ;

	public Structure getStructure() {
		return pere;
	}

	public void ajouter(List<Composant> L)  {
		L.add(this);
	}

}
