import Operation;
import java.util.*;

/** Erwan Modélise une opération du langage vhdl.
 * Cette classe permet de faire la jonction entre l'interpretation et la simulation.
 *
 * Erwan possède une strucure qui peut être vue arborifique ou recursive : 
 * un Erwan contiendra lui-même d'autres Erwan pour décrire un signal dans son ensemble.
 * Chaque Erwan contient :
      - le nom du signal qu'il génère
      - le(s) Erwan (signaux) sur le(s)quelles il s'appuie pour le généré
      - l'opération qu'il modélise avec le(s) Erwan d'entrée 
 * Le Erwan "père", soit celui qui n'est pas contenu dans un autre Erwan sera celui de l'affectation, 
 * tandis que le "dernier descendant", celui qui ne contient pas d'autre Erwan, sera celui d'une lecture "numérique":
 * soit celle d'une entrée, d'une constante, ou d'un signal généré. 
 * Entre ces deux types de Erwan on a les Erwan des Opérations logiques.
 *
 * Pour generer ces Erwan, aucun constructeur n'est mis à disposition. 
 * Il faudra passer exclusivement par les méthodes de classe.
 *
 * Le langage implémanté pour ce projet comprend la possibilité de créer des "vecteur" de signaux, 
 * soit un regroupement cohérant de signaux ayant un seul nom mais etant identifiable par un indice.
 * Ici cela sera modélisé comme des sigaux indépendants, 
 * mais cela ne se repercutera pas vraiment sur la syntax pour generer ces signaux.
 * Ceci sera détaillé plus tard ! TODO
 *
 * @author  Mati AFRIAT -- archidu7
 */

public class Erwan {

	public Operation Operation;

	public List<Erwan> Entrees;

	public String Nom;

	public Integer Numero;

	private Erwan(Operation Operation, List<Erwan> Entrees, String Nom) {
		this.Operation = Operation;
		this.Entrees = Entrees;
		this.Nom = Nom;
		this.Numero = null;
	}

	private Erwan(Operation Operation, List<Erwan> Entrees, String Nom, int num) {
		this(Operation, Entrees,Nom);
                this.Numero = new Integer(num);
        }

	/** Affectation de la valeur d'un signal à un autre signal.
	 * Cela permet de "changer le nom" d'un signal, 
	 * puisque les autre signaux ont un nom qui dépend exclusivement du nom de leur(s) entrée(s) et de leur opération.
	 * C'est l'origine de tout signal généré.
	 * @param Nom C'est le nom que l'on souhaite donner au signal "copié".
	 * @param Entree De type Erwan, Entree modélise le signal que l'on copie.
	 * @return La modélisation du signal ainsi copié. 
	 */
	public static Erwan AFFECTATION(String Nom, Erwan Entree) {
		List<Erwan> Entrees = new ArrayList<>();
                Entrees.add(Entree);
                return new Erwan(Operation.AFFECTATION,Entrees,Nom);
	}

	/** Modélisation d'un signal résultant d'un ET logique.
	 * @param Nom Il s'agit du nom résultant de l'algorithme de nommage non présent ici. Il n'est pas fait automatiquement pour le moment.
	 * @param Entrees Il s'agit de la liste des signaux sur lesquelles on effectue l'operation.
	 * @return La modélisation du signal ainsi généré.
	 */
	public static Erwan AND(String Nom, List<Erwan> Entrees) {
		return new Erwan(Operation.AND,Entrees,Nom);
	}

	/** Modélisation d'un signal résultant d'un OU logique.
         * @param Nom Il s'agit du nom résultant de l'algorithme de nommage non présent ici. Il n'est pas fait automatiq
uement pour le moment.
         * @param Entrees Il s'agit de la liste des signaux sur lesquelles on effectue l'operation.
	 * @return La modélisation du signal ainsi généré.
         */
	public static Erwan OR(String Nom, List<Erwan> Entrees) {
                return new Erwan(Operation.AND,Entrees,Nom);
        }
	
	/** Modélisation d'un signal résultant d'un NON logique.
         * @param Nom Il s'agit du nom résultant de l'algorithme de nommage non présent ici. Il n'est pas fait automatiq
uement pour le moment.
         * @param Entree Il s'agit du signal sur lequel on effectue l'operation.
	 * @return La modélisation du signal ainsi généré.
         */
	public static Erwan NOT(String Nom, Erwan Entree) {
		List<Erwan> Entrees = new ArrayList<>();
		Entrees.add(Entree);
                return new Erwan(Operation.AND,Entrees,Nom);
        }

	/** Modélisation d'un signal resultant d'une lecture logique.
	 * @param Nom C'est le nom du signal que l'on souhaite LIRE.
	 * @return La modélisation du signal ainsi généré.
	 */
	public static Erwan LITTERAL(String Nom) {
                return new Erwan(Operation.LITTERAL,null,Nom);
        }

	public static Erwan CONSTANTE(boolean b) {
		return LITTERAL(b ? "1" : "0");
	}

	public static List<Erwan> ARANGE(String Nom,int IndiceDebut, int IndiceFin, List<Erwan> Entrees) {
		List<Erwan> R = new ArrayList<>();
		int i = IndiceDebut;
		for(Erwan e : Entrees
                return R;
        }

	public static List<Erwan> ANDR(String Nom,int IndiceDebut, int IndiceFin, List<List<Erwan>> Entrees) {
		List<Erwan> R = new ArrayList<>();
		/*
                int i = IndiceDebut;
                for(Erwan e : Entrees) {
			Erwan s = AND(Nom,e);
			s.Numero = new Integer(i);
			R.add(s);
			i = i + 1;
		}
		//TODO si i != IndiceFin Erreur ou alors on vire IndiceFin
		//TODO Tout ce qui est commenté au dessus est obsolète
		*/
		List<List<Erwan>> A = new ArrayList<>();
		for (int c = IndiceDebut; c <= IndiceFin; c++) {
			List<Erwan> L = new ArrayList<>();
			A.add(L);
		}
		for (Erwan e : Entrees) {
			if (e.size() == 1) {
				for (int c = 0; c <= (IndiceFin - IndiceDebut); c++) {
					A.get(c).add(e.get(0));
				}
			} else if (e.size() ==  (IndiceFin - IndiceDebut + 1) {
				for (int c = 0; c <= (IndiceFin - IndiceDebut); c++) {
                                        A.get(c).add(e.get(c));
                                }
			} else throw new RuntimeError("Pb de taille"); //TODO changer l'erreur
                }
		int i = IndiceDebut;
                for(List<Erwan> L : A) {
                        Erwan s = AND(Nom,L);
                        s.Numero = new Integer(i);
                        R.add(s);
                        i = i + 1;
                }
                return R;
        }

	public static List<Erwan> ORR(String Nom,int IndiceDebut, int IndiceFin, List<List<Erwan>> Entrees) {
                List<Erwan> R = new ArrayList<>();
		List<List<Erwan>> A = new ArrayList<>();
                for (int c = IndiceDebut; c <= IndiceFin; c++) {
                        List<Erwan> L = new ArrayList<>();
                        A.add(L);
                }
                for (Erwan e : Entrees) {
                        if (e.size() == 1) {
                                for (int c = 0; c <= (IndiceFin - IndiceDebut); c++) {
                                        A.get(c).add(e.get(0));
                                }
                        } else if (e.size() ==  (IndiceFin - IndiceDebut + 1) {
                                for (int c = 0; c <= (IndiceFin - IndiceDebut); c++) {
                                        A.get(c).add(e.get(c));
                                }
                        } else throw new RuntimeError("Pb de taille"); //TODO changer l'erreur
                }
                int i = IndiceDebut;
                for(List<Erwan> L : A) {
                        Erwan s = OR(Nom,L);
                        s.Numero = new Integer(i);
                        R.add(s);
                        i = i + 1;
                }
                return R;
        }

	public static List<Erwan> NOTR(String Nom,int IndiceDebut, int IndiceFin, List<Erwan> Entrees) {
		if (Entrees.size() != (IndiceFin - IndiceDebut + 1)) throw new RuntimeError("Pb de taille"); //TODO changer l'erreur
		List<Erwan> R = new ArrayList<>();
                for (int c = 0; c <= (IndiceFin - IndiceDebut); c++) {
			Erwan s = NOT(Nom,Entrees.get(c));
			s.Numero = new Integer(IndiceDebut + c);
			R.add(s);
                }
		return R;
	}

	public static List<Erwan> CONSTANTER(String Nom,int IndiceDebut, int IndiceFin, List<Boolean> Constantes){
		if (Constantes.size() != (IndiceFin - IndiceDebut + 1)) throw new RuntimeError("Pb de taille"); //TODO changer l'erreur
                List<Erwan> R = new ArrayList<>();
                for (int c = 0; c <= (IndiceFin - IndiceDebut); c++) {
			if (Constantes.get(c) == null) throw new RuntimeError("Boolean mal initialisé"); /* TODO changer l'erreur */
                        Erwan s = CONSTANTE(Constantes.get(c).booleanValue());
                        s.Numero = new Integer(IndiceDebut + c);
                        R.add(s);
                }
                return R;
        }

	public static Erwan CONSTANTE_UP() {
                return new Erwan(Operation.LITTERAL,null,"1");          //TODO J'hésite a faire des méthode cst 1 et 0
									    //TODO J'hésite a remplacer CONSTANTE par LITTERAL avec simplement
									    //des signaux qui s'appellent 0 et 1 ....
        }

	public static Erwan CONSTANTE_DW() {
                return new Erwan(Operation.LITTERAL,null,"0");
	}

	public static List<Erwan> APPELMODULE(String NomModule, List<String> NomEntrees, List<String> NomSorties){
		return null ; //TODO A Faire
	}
}

