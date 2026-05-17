package erwan;

import java.util.*;

/** Decrire une entree/sortie qu'elle contienne un ou plusieurs signaux.
 * <p> Cette classe va permettre au simulateur d'extraire les signaux séléctionnés pour être des entrées/sorties.<p>
 *
 * <p> D'une part, cette classe permet de décrire et indiquer à un simulateur les signaux qui sont des entrée, ou sortie.
 * C'est à dire que l'on utilisera un descripteur pour decrire les entrées et un autre pour décrire les sorties.<br>
 * Ceci permet nottament de dicider qu'un signal lu et généré peut aussi être une sortie,
 * mais également de décréter l'ordre des entrées sorties. Concrètement, cela permet de dire que tel signal (rs grp de signaux),
 * est la première entrée, celui-ci le second ... et idem pour les sorties.
 * Tout cela est indispensable pour les appel module où l'on veut faire des correspodance entre des signaux d'un circuit
 * avec des entrées et sortie d'un autre.</p>
 *
 * <p> D'autre part, cette classe permet également de décrire des échanges de signaux lors d'appels modules : 
 * Quand on fait appel à un module, on fournit des signaux de notre circuit au modules, 
 * dans ses entrées et dans ses sorties. <br>
 * Ces signaux sont cepandant des noms du circuit appelant,
 * et le simulateur ne sais pas quels signaux fournir si on ne dit que que l'on fait appel à un module.
 * Pour cette raison quand on fera l'appel à un module et que l'on utilisera la classe AppelModule,
 * on utilisera également des Descripteur pour préciser quels signaux sont fournit et quels signaux sont générés par le module.
 * <br>
 * Attention à ne pas confondre les descripteurs des appels modules, qui représentent les signaux fournits au module,
 * et les descripteur du sous module qui représentes les entrées et sorties du module.
 * Ces derniers ne doivent être en aucun cas modifiés lors d'un appel module car ils contiennent les noms des entreées en interne,
 * ces nom permettent de les retrouvé dans le circuit généré par le code shdl du sous module. </p>
 *
 * <p> Bonne Chance </p>
 *
 * 
 *
 * @author Mati Afriat -- archidu7
 */
public class Descripteur {

	private String Nom;
	private int indiceDebut, indiceFin;

	/** Décrire un vecteur d'entree ou de sortie en spécifiant son nom et les indices séléctionnés.
	 * Il est possible de ne selectionner qu'une partie de vecteur.
	 * @param Nom le nom du vecteur de signaux.
	 * @param d l'indice du premier signal du vecteur selectionné.
	 * @param f l'indice du dernier signal du vecteur selectionné.
	 */
	public Descripteur(String Nom, int d, int f) {
		this.Nom = Nom;
		this.indiceDebut = d;
		this.indiceFin = f;
	}

	/** Décrire une entree/sortie ne représentant qu'un signal unique.
	 * @param Nom le nom du signal.
	 */
	public Descripteur(String Nom){
		this(Nom,0,0);
	}

	/** Recuperer le nom de l'entree.
	 * @return le nom de l'entree.
	 */
	public String Nom() {
		return Nom;
	}

	/** Determiner si une entree contient un unique signal.
	 * @return true si il est unique.
	 */
	public boolean unique() {
		return indiceDebut == indiceFin;
	}

	/** Renvoie l'indice du premier signal séléctionné du vecteur.
	 * Renvoie 0 pour un signal unique.
	 * @return l'indice.
	 */
	public int indiceDebut() {
		return indiceDebut;
	}

	/** Renvoie l'indice du dernier signal séléctionné du vecteur.
         * Renvoie 0 pour un signal unique.
         * @return l'indice.
         */
	public int indiceFin() {
		return indiceFin;
	}

	/** Renvoie la liste des noms des signaux contenus dans le vecteur.
	 * @return une liste de string des nom des signaux.
	 */
	public List<String> Noms() {
		List<String> L = new ArrayList<>();
		if (unique()) {
			L.add(Nom);
			return L;
		} else {
			for(int i = indiceDebut; i <= indiceFin; i++) {
				L.add(Nom + "[" + i + "]");
			}
			return L;
		}
	}

	/** La liste des signaux sous forme de Erwan.
	 * @return un liste de Erwan modélisant les signaux.
	 */
	public List<Erwan> Erwans() {
                List<Erwan> L = new ArrayList<>();
                if (unique()) {
                        L.add(Erwan.LITTERAL(Nom));
                } else {
                        L.addAll(Erwan.LITTERANGE(Nom,indiceDebut,indiceFin));
                }
                return L;
        }

	/** Le nombre de signaux contenus dans l'entree/sortie.
	 * @return le nombre.
	 */
	public int nbSignaux(){
		return indiceFin - indiceDebut + 1;
	}
}

