package simulateur;

import java.util.*;

/** Interface simulateur permet de génerer la modélisation du circuit et d'interagire avec sa simulation.
 * <p> Une implémentation FileSimulateur sera bientot disponible, un peu de patience ! <br>
 * Cette interface est destinnée à celui/celle qui s'occupe de l'interface graphique de la simulation.
 * Elle permet d'obtenir des détails nécessaire à la création de la fenetre ainsi que au bon fonctionnement des interaction avec la simulation.
 * Elle permet nottemment d'obtenir le nombre d'entrée de la simulation, leur nom, par index ainsi que leur nombre de signaux, par index aussi.
 * Et naturellement, elle permet la meme chose pour les sorties. </p>
 *
 * <p> Si cette interface vous concerne, vous êtes cordialement inviter à lire les commentaires des méthodes pour plus de détails.</p>
 *
 * @author Mati Afriat -- archidu7.
 */
public interface Simulateur {

	/** Permet d'obtenir le nombre d'entrées, permettant de créer le nombre de fenêtres associé.
	 * @return Le nombre d'entrées de la simulation. Attention cepandant, il s'agit de StructEntree, ce qui signifie que chaque entrée peut contir plusieur signaux ...
	 */
	int nbEntree();

	/** Permet d'obtenir le nombre de sorties, permettant de créer le nombre de fenêtres associé.
         * @return Le nombre de sorties de la simulation. Attention cepandant, il s'agit de StructSorties, ce qui signifie que chaque sortie peut contir plusieur signaux ...
         */
	int nbSorties();

	/** Permet de savoir le nombre de signaux pour chaque entrée, en repérant l'entrée par son indice.
	 * Attention, les indices commencent à 1 !!
	 * @param i l'indice de l'entrée, !! les indices commencent à 1 !!
	 * @return le nombre de signaux dans l'entrée.
	 */
	int nbSlotEntree(int i);

	/** Permet de savoir le nombre de signaux pour chaque sortie, en repérant la sortie par son indice.
         * Attention, les indices commencent à 1 !!
         * @param i l'indice de la sortie, !! les indices commencent à 1 !!
         * @return le nombre de signaux dans la sortie.
         */
	int nbSlotSortie(int i);

	/** Permet de savoir le nom de chaque entrée, en repérant l'entrée par son indice.
         * Attention, les indices commencent à 1 !!
         * @param i l'indice de l'entrée, !! les indices commencent à 1 !!
         * @return le nom de l'entrée.
         */
	String nomEntree(int i);

	/** Permet de savoir le nom de la sortie, en repérant la sortie par son indice.
         * Attention, les indices commencent à 1 !!
         * @param i l'indice de la sortie, !! les indices commencent à 1 !!
         * @return le nom de la sortie.
         */
	String nomSortie(int i);

	/** Recuperation d'un bouton correspondant à un signal d'entrée.
	 * Voir BouttonEntree et StructEntree pour mieux comprendre.
	 * Chaque boutton correspond à un signal d'entrée, dans une entrée. Il se repère donc par deux indices : celui de l'entrée parmi les entrée et celui du signal dans l'entrée.
	 * @param i l'indice de l'entree.
	 * @param j l'indice du signal dans l'entrée.
	 * @return le BouttonEntree.
	 */
	BouttonEntree getEntrees(int i, int j);

	/**Recupération d'un Connecteur correspondant à un signal de sortie, permetant de récuperer l'état de ce signal.
	 * Pour recuperer l'état d'un Connecteur on utilise la methode getValeur().
	 * Je ne pense pas avoir déjà fait la doc de Connecteur mais ce n'est pas nécessaire pour cette utilisation.
	 * Ici on suit la même logique que pour getEntree.
	 * @param i l'indice de la sortie.
	 * @param j l'indice du signal dans la sortie.
	 * @return le Connecteur.
	 */
	Connecteur getSorties(int i, int j);

}
