package simulateur;

import java.util.*;

/** Represente et fait le calcul d'un composant logique.
 * <p> Ce composant à un nombre d'entrées et de sorties variable.
 * Il s'agit d'une classe abstraite devrant être sppécialisée en fonction du calcul logique effectué.
 * <br> Les entrées/sorties sont implémentées par des Connecteurs qui permettent de comminiquer des états logique entre differents composants.
 * C'est la méthode abstraite calculer qui doit etre reimplémenté pour chaque spécialisation de cette classe.
 * 
 * @author Mati Afriat -- Archidu7.
 */
 public abstract class Composant implements Structure {

	private TableauConnecteur entrees, sorties;

	private Structure pere;
/*
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
*/
	/** Permet de créer un Composant.
	 * C'est probablement une erreur de l'avoir mis public, j'aurais du le mettre en protected.
	 * @param nb_entrees le nombre d'entrées.
	 * @param mn_sorties le nombre de sorties.
	 * @return Le composant.
	 */
	public Composant(int nb_entrees, int nb_sorties) {
                entrees = new TableauConnecteur(nb_entrees);
                sorties = new TableauConnecteur(nb_sorties);
        }

	protected Composant(List<Connecteur> Entrees, Connecteur Sortie) {
		this(Entrees.size(),1);
		for (int i = 1; i <= Entrees.size(); i++){
			this.brancherEntree(Entrees.get(i-1),i);
		}
		this.brancherSortie(Sortie,1);
	}

	protected Composant(int nb_entrees, int nb_sorties, String[] NomSorties)  {
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

	/** Recuperer le nombre d'entrées d'un Composant.
	 * @return le nb d'entrée du composant.
	 */
	public int getNbEntree() {
		return entrees.getTaille();
	}

	/** Recupérer un connecteur de sortie du composnat par son indice.
	 * @param i le numéro de la sorie
	 * @return le Connecteur.
	 */
	public Connecteur getConnecteurSortie(int i)  {
		return sorties.getConnecteur(i);
	}

	/** Brancher un Conneteur sur un numéro d'entrée.
	 * @param l le connecteur.
	 * @param i le numéro d'entrée.
	 */
	public void brancherEntree(Connecteur l, int i)  {
		entrees.brancher(l,i);
		l.setComposant(this);
	}

	protected void ajouterEntree(Connecteur c)  {
                TableauConnecteur Tc = this.entrees;
                TableauConnecteur Tc2 = new TableauConnecteur(Tc.getTaille() + 1);
                for (int i = 1; i <= Tc.getTaille(); i++) {
                        Tc2.brancher(Tc.getConnecteur(i),i);
                }
                Tc2.brancher(c,Tc2.getTaille());
                this.entrees = Tc2;
        }

	protected void ajouterSortie(Connecteur c)  {
                TableauConnecteur Tc = this.sorties;
                TableauConnecteur Tc2 = new TableauConnecteur(Tc.getTaille() + 1);
                for (int i = 1; i <= Tc.getTaille(); i++) {
                        Tc2.brancher(Tc.getConnecteur(i),i);
                }
                Tc2.brancher(c,Tc2.getTaille());
                this.sorties = Tc2;
        }

	/** Brancher un Conneteur sur un numéro de sortie.
         * @param l le connecteur.
         * @param i le numéro de sortie.
         */
	public void brancherSortie(Connecteur l, int i)  {
		sorties.brancher(l,i);
		l.setOrigine(this);
	}

	/** Retirer un connecteur de parmi les entrées.
	 * @param l la poigné du connecteur à retirer.
	 * @return le numéro de l'entrée duquel on l'a retiré.
	 */
	public int debrancherEntree(Connecteur l)  {
                int i = entrees.debrancher(l);
		l.unsetComposant();
		return i;
        }

	/** Retirer un connecteur de parmi les sorties.
         * @param l la poigné du connecteur à retirer.
         * @return le numéro de la sortie duquel on l'a retiré.
         */
        public int debrancherSortie(Connecteur l)  {
                int i = sorties.debrancher(l);
		l.unsetOrigine();
		return i;
        }

	protected void setSortie(int i, Etat b)  {
		sorties.set(i,b);
	}

	/** Recuperer le nombre de sorties d'un Composant.
         * @return le nb de sorties du composant.
         */
	public int getNbSortie() {
		return sorties.getTaille();
	}
	
	/** Fonction logique entre les entrées et sorties.
	 */
	public abstract void calculer()  ;

	/** Plus tard !.
	 * @return Plus tard !.
	 */
	public Structure getStructure() {
		return pere;
	}

	/** Ajoute le composant à une liste de composant.
	 * C'était pour résoudre un problème technique, ne faites pas attention.
	 * @param L la liste.
	 */
	public void ajouter(List<Composant> L)  {
		L.add(this);
	}

}
