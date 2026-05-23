package simulateur;

import java.util.*;

/**
 * Représente et effectue le calcul d'une porte ET logique.
 * Cette classe hérite de Composant. Elle peut avoir différent nombre d'entrée
 * et une seule sorie.
 * 
 * @author Mati Afriat -- Archidu7.
 */
public class And extends Composant {

	/**
	 * Création d'un composant AND.
	 * 
	 * @param s1
	 *                      un connecteur d'entrée,
	 * @param s2
	 *                      un connecteur d'entrée,
	 * @param nomResultat
	 *                      le nom de sortie. Le connecteur sera généré
	 *                      automatiquement.
	 * @return le AND.
	 */
	public And(Connecteur s1, Connecteur s2, String nomResultat) {
		super(2, 1);
		super.brancherEntree(s1, 1);
		super.brancherEntree(s2, 2);
		super.brancherSortie(new Lien(nomResultat), 1);

	}

	/**
	 * Création d'un composant AND.
	 * 
	 * @param s1
	 *             un connecteur d'entrée,
	 * @param s2
	 *             un connecteur d'entrée,
	 * @param r
	 *             le connecteur de sortie.
	 * @return le AND.
	 */
	public And(Connecteur s1, Connecteur s2, Connecteur r) {
		super(2, 1);
		super.brancherEntree(s1, 1);
		super.brancherEntree(s2, 2);
		super.brancherSortie(r, 1);
	}

	/**
	 * Création d'un composant AND.
	 * 
	 * @param Entres
	 *                 une liste de connecteur d'entrée,
	 * @param Sortie
	 *                 le connecteur de sortie.
	 * @return le AND.
	 */
	public And(List<Connecteur> Entrees, Connecteur Sortie) {
		super(Entrees, Sortie);
	}

	/**
	 * Méthode abstarite spécialisée.
	 * Calcul un ET logique entre les sortie et met le résutat en entrée.
	 */
	public void calculer(Propageur prop) {
		Etat e = Etat.ND;
		if (super.getEntree(1) == Etat.UP && super.getEntree(2) == Etat.UP) {
			e = Etat.UP;
		} else if (super.getEntree(1) == Etat.DW || super.getEntree(2) == Etat.DW) {
			e = Etat.DW;
		}
		propager(e, prop);
	}

	/**
	 * Ajouter un connecteur en tant qu'entrée.
	 * Augmente le nombre d'entrée !.
	 * 
	 * @param c
	 *            le connecteur.
	 */
	public void ajouter(Connecteur c) {
		/*
		 * TableauConnecteur Tc = super.getE();
		 * TableauConnecteur Tc2 = new TableauConnecteur(Tc.getTaille() + 1);
		 * for (int i = 1; i <= Tc.getTaille(); i++) {
		 * Tc2.brancher(Tc.getConnecteur(i),i);
		 * }
		 * Tc2.brancher(c,Tc2.getTaille());
		 * super.setE(Tc2);
		 */
		super.ajouterEntree(c);
	}

	/*
	 * @Override
	 * public void ajouter(List<Composant> L) {
	 * super.ajouter(L);
	 * for (int i = 1; i <= super.getNbSortie(); i++) {
	 * Connecteur cd = getConnecteurSortie(i);
	 * if (cd.getComposant() != null) cd.getComposant().ajouter(L);
	 * }
	 * }
	 */
	/**
	 * Permet de connaitre le type et de l'afficher facilement.
	 * Sert pour le debug et plus tard.
	 * 
	 * @return "And".
	 */
	public String getNom() {
		return "And";
	}
}
