package simulateur;

import java.util.Map;
import java.util.HashMap;
/** Permet d'acceder par le nom à tous les signaux d'un circuit.
 * <p> Cette classe se comporte comme un dictionnaire : si on lui fourni un nom elle donnera un "connecteur" qui permettra de rattacher deux composant.
 * Ce connecteur représente un signal logique.
 * Si un signal n'a pas encore de lien attribué, c'est le dico qui se charge de la création.
 * C'est à dire que le comportement est exatement le même si le connecteur existait déjà ou non. <br>
 * De plus, il existe une methode pour savoir si un lien existe déjà ou non. </p>
 *
 * @author Mati Afriat -- Archidu7.
 */
public class DicoConnecteur {

	private Map<String, Connecteur> dico;

	/** Création du dictionnaire.
	 * @return le dico.
	 */
	public DicoConnecteur() {
		dico = new HashMap<>();
	}

	/** Recupération d'un Connecteur via son nom.
	 * Renvoie la poignée du lien si il est déjà généré,
	 * il le créé et renvoie la poignée sinon.
	 * @return le connecteur.
	 */
	public Connecteur getConnecteur(String S) {
		Connecteur C = dico.get(S);
		if (C == null) {
			C = new Lien(S);
			dico.put(S,C);
			//System.out.println("Création du lien : " + S);
		}
		return C;
	}

	/** Determiner si un Connecteur existe déjà.
	 * @return true si le connecteur existe, false sinon.
	 */
	public boolean existe(String S) {
		Connecteur C = dico.get(S);
		if (C == null) return false;
		else return true;
	}

	/** Ajouter un connecteur dans le dico.
	 * Cette méthode permet d'ajouter manuellement un connecteur dans le dico.
	 * <br> A savoir que cela est rarement nécessaire puisque le dico peut se charger lui meme de la création.
	 * @param c La poignée du connecteur à stocker.
	 * @param s le String du nom auquel on associe la poignée. (on peut associer une poignée à différents noms).
	 */
	public void ajouter(Connecteur c, String s) {

		dico.put(s,c);

	}

	/** Supprimer un enregistrement Connecteur - Nom.
	 * @param s le nom sous lequel on a enregistré ce connecteur.
	 */
	public void supprimer(String s) {
		
		dico.remove(s);

	}
}
