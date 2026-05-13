package erwan;
import java.util.*;

/** Erwan Modélise une opération du langage vhdl.
 * <p> Cette classe permet de faire la jonction entre l'interpretation et la simulation.</p>
 *
 * <p> Erwan possède une strucure qui peut être vue arborifique ou recursive : 
 * un Erwan contiendra lui-même d'autres Erwan pour décrire un signal dans son ensemble.
 * Chaque Erwan contient : <br>
      - le nom du signal qu'il génère <br>
      - le(s) Erwan (signaux) sur le(s)quelles il s'appuie pour le généré <br>
      - l'opération qu'il modélise avec le(s) Erwan d'entrée <br>
 * Le Erwan "père", soit celui qui n'est pas contenu dans un autre Erwan sera celui de l'affectation, 
 * tandis que le "dernier descendant", celui qui ne contient pas d'autre Erwan, sera celui d'une lecture "numérique":
 * soit celle d'une entrée, d'une constante, ou d'un signal généré. 
 * Entre ces deux types de Erwan on a les Erwan des Opérations logiques. </p>
 *
 * <p> Pour generer ces Erwan, aucun constructeur n'est mis à disposition. 
 * Il faudra passer exclusivement par les méthodes de classe.</p>
 *
 * <p> Le langage implémanté pour ce projet comprend la possibilité de créer des "vecteur" de signaux, 
 * soit un regroupement cohérant de signaux ayant un seul nom mais etant identifiable par un indice.
 * Ici cela sera modélisé comme des sigaux indépendants, 
 * mais cela ne se repercutera pas vraiment sur la syntax pour generer ces signaux. <br>
 * Ceci sera détaillé plus tard ! TODO </p>
 *
 * @author  Mati AFRIAT -- archidu7
 */

public class Erwan implements Branchement {

	/** Opération du signal.*/
	public Operation Op;

	/** Les signaux représentant les entrées du signal.*/
	public List<Erwan> Entrees;

	public String Nom;

	/** Dans le cas d'un vecteur, représente l'index du signal.
	 * vaut null si le signal ne fait pas parti d'un vecteur. */
	public Integer Numero;

	private Erwan(Operation Op, List<Erwan> Entrees, String Nom) {
		this.Op = Op;
		this.Entrees = Entrees;
		this.Nom = Nom;
		this.Numero = null;
	}

	private Erwan(Operation Op, List<Erwan> Entrees, String Nom, int num) {
		this(Op, Entrees,Nom);
                this.Numero = new Integer(num);
        }

	/** Permet d'obtenir le nom d'un signal.
	 * Cette méthode permet de prendre en compte si un signal fait parti d'un vecteur.
	 * @return Le nom su signal
	 */
	public String Nom() {
		return new String(Nom + ((this.Numero == null) ? "" : "[" + this.Numero + "]"));
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
	
	public static Erwan AFFECTATION(String Nom,int numero, Erwan Entree) {
		List<Erwan> E = new ArrayList<>();
		E.add(Entree);
		return ARANGE(Nom,numero,numero,E).get(0);
	}

	/** Modélisation d'un signal résultant d'un ET logique.
	 * @param Entrees Il s'agit de la liste des signaux sur lesquelles on effectue l'operation.
	 * @return La modélisation du signal ainsi généré.
	 */
	public static Erwan AND(List<Erwan> Entrees) {
		String Nom = null;
		for (Erwan E : Entrees) {
			String NE = null;
			if (E.Op == Operation.LITTERAL || E.Op == Operation.NOT) NE = E.Nom();
                        else NE = new String("(" + E.Nom() + ")");
			if (Nom == null) Nom = new String(NE);
			else Nom += " * " + NE;
		}
		return new Erwan(Operation.AND,Entrees,Nom);
	}

	/** Modélisation d'un signal résultant d'un OU logique.
         * @param Entrees Il s'agit de la liste des signaux sur lesquelles on effectue l'operation.
	 * @return La modélisation du signal ainsi généré.
         */
	public static Erwan OR(List<Erwan> Entrees) {
		String Nom = null;
                for (Erwan E : Entrees) {
			String NE = null;
			if (E.Op == Operation.LITTERAL || E.Op == Operation.NOT) NE = E.Nom();
			else NE = new String("(" + E.Nom() + ")");
                        if (Nom == null) Nom = new String(NE);
                        else Nom += " + " + NE;
		}
                return new Erwan(Operation.AND,Entrees,Nom);
        }
	
	/** Modélisation d'un signal résultant d'un NON logique.
         * @param Entree Il s'agit du signal sur lequel on effectue l'operation.
	 * @return La modélisation du signal ainsi généré.
         */
	public static Erwan NOT(Erwan Entree) {
		List<Erwan> Entrees = new ArrayList<>();
		Entrees.add(Entree);
		String Nom = new String("/" + ((Entree.Op == Operation.LITTERAL || Entree.Op == Operation.NOT) ? Entree.Nom() : "(" + Entree.Nom() + ")"));
                return new Erwan(Operation.NOT,Entrees,Nom);
        }

	/** Modélisation d'un signal resultant d'une lecture logique.
	 * @param Nom C'est le nom du signal que l'on souhaite LIRE.
	 * @return La modélisation du signal ainsi généré.
	 */
	public static Erwan LITTERAL(String Nom) {
                return new Erwan(Operation.LITTERAL,null,Nom);
        }

	public static Erwan LITTERAL(String Nom, int numero) {
		return LITTERANGE(Nom,numero, numero).get(0);
	}

	/** Modélisation d'un signal constant.
	 * C'est une spécialisation de LITTERAL.
	 * @param b booléen correspondant à la constante voulu.
	 * @return La médélisation du signal ainsi généré.
	 */
	public static Erwan CONSTANTE(boolean b) {
		return LITTERAL(b ? "1" : "0");
	}

	/** Affectation dans le cas d'un vecteur.
	 * L'idée est la même que pour l'affectation classique mais la logique change un peu :
	 * Puisque on ne modélise que des signaux et pas de vecteur de signaux, il faudra récupérer le résultat comme étant une liste de signaux.
	 * On fournit la liste des signaux à intégré au vecteur, le nom du vecteur et les indice de début et de fin auxquelles on faut l'affectation.
	 * @param Nom C'est le nom du vecteur à génerer. (Choix libre)
	 * @param IndiceDebut c'est l'indice du vecteur à partir duquel commence l'affectation.
	 * @param IndiceFin c'est l'indice du vecteur auquel s'arrête l'affectation.
	 * @param Entrees c'est la liste des signaux à intégrer au vecteur.
	 * @return La liste des modélisation des signaux inclus dans le vecteur.
	 */
	public static List<Erwan> ARANGE(String Nom,int IndiceDebut, int IndiceFin, List<Erwan> Entrees) {
		List<Erwan> R = new ArrayList<>();
		int i = IndiceDebut;
		for(Erwan e : Entrees) {
			Erwan E = AFFECTATION(Nom,e);
			E.Numero = new Integer(i);
			R.add(E);
			i = i + 1;
		}
		if (Entrees.size() != IndiceFin - IndiceDebut + 1 ) throw new RuntimeException("Nombre non correspondant d'entrées"); //TODO changer l'erreur
                return R;
        }
	
	/** Lecture numérique dans le cas de signaux appartenant à un vecteur.
	 * il faut utiliser cette méthode meme si on ne souhaire lire que un seul signal.
	 * @param Nom le nom du vecteur duquel on souhaite lire.
	 * @param IndiceDebut l'indice à partir duquel on souhaite lire.
	 * @param IndiceFin l'indice jusqu'auquel on veut lire, inclu. Attention, dans le cas on l'on ne souhaite lire qu'un signal, c'est égal à IndiceDebut.
	 * @return la liste des signaux modélisés en Erwan.
	 */
	public static List<Erwan> LITTERANGE(String Nom, int IndiceDebut, int IndiceFin) {
		List<Erwan> R = new ArrayList<>();
		for(int i = IndiceDebut; i<= IndiceFin; i++) {
			Erwan E = LITTERAL(Nom);
			E.Numero = new Integer(i);
			R.add(E);
		}
		return R;
	}

	/** Opération "ET" dans le cas d'un vecteur. 
	 * Cette méthode permet de simplifier l'opération ET bit à bit de deux vecteur, ou d'un vecteur et d'un signal.
	 * En suposant la taille des vecteur etant de 'n' on peut mettre autant de vecteur de taille n et 1 que l'on souhaite dans les Entrees.
	 * Un vecteur de taille différente entrainera une erreur.
	 * @param Taille C'est la taille des vecteur entre lesquelles on fait l'opération.
	 * @param Entrees C'est la liste des vecteur (eux mêmes des liste de Erwan).
	 * @return La liste des modélisation des signaux inclus dans le vecteur généré par l'opération.
	 */
	public static List<Erwan> ANDR(int Taille, List<List<Erwan>> Entrees) {
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
		for (int c = 0; c < Taille; c++) {
			List<Erwan> L = new ArrayList<>();
			A.add(L);
		}
		for (List<Erwan> e : Entrees) {
			if (e.size() == 1) {
				for (int c = 0; c < Taille; c++) {
					A.get(c).add(e.get(0));
				}
			} else if (e.size() ==  Taille) {
				for (int c = 0; c < Taille; c++) {
                                        A.get(c).add(e.get(c));
                                }
			} else throw new RuntimeException("Pb de taille"); //TODO changer l'erreur
                }
                for(List<Erwan> L : A) {
                        Erwan s = AND(L);
                        R.add(s);
                }
                return R;
        }

	public static List<Erwan> ANDR(List<List<Erwan>> Entrees){
		int Taille = 0;
		for (List<Erwan> Entree : Entrees) {
			if(Entree.size() > Taille) Taille = Entree.size();
		}
		return ANDR(Taille,Entrees);
	}

	/** Opération "OU" dans le cas d'un vecteur.
         * Cette méthode permet de simplifier l'opération OU bit à bit de deux vecteur, ou d'un vecteur et d'un signal.
         * En suposant la taille des vecteur etant de 'n' on peut mettre autant de vecteur de taille n et 1 que l'on souhaite dans les Entrees.
         * Un vecteur de taille différente entrainera une erreur.
         * @param Taille C'est la taille des vecteur entre lesquelles on fait l'opération.
         * @param Entrees C'est la liste des vecteur (eux mêmes des liste de Erwan).
         * @return La liste des modélisation des signaux inclus dans le vecteur généré par l'opération.
         */
	public static List<Erwan> ORR(int Taille, List<List<Erwan>> Entrees) {
                List<Erwan> R = new ArrayList<>();
		List<List<Erwan>> A = new ArrayList<>();
                for (int c = 0; c < Taille; c++) {
                        List<Erwan> L = new ArrayList<>();
                        A.add(L);
                }
                for (List<Erwan> e : Entrees) {
                        if (e.size() == 1) {
                                for (int c = 0; c < Taille; c++) {
                                        A.get(c).add(e.get(0));
                                }
                        } else if (e.size() ==  Taille) {
                                for (int c = 0; c < Taille; c++) {
                                        A.get(c).add(e.get(c));
                                }
                        } else throw new RuntimeException("Pb de taille"); //TODO changer l'erreur
                }
                for(List<Erwan> L : A) {
                        Erwan s = OR(L);
                        R.add(s);
                }
                return R;
        }

	public static List<Erwan> ORR(List<List<Erwan>> Entrees) {
		int Taille = 0;
                for (List<Erwan> Entree : Entrees) {
                        if(Entree.size() > Taille) Taille = Entree.size();
                }
                return ORR(Taille,Entrees);
	}

	/** Opération "NON" dans le cas d'un vecteur.
         * Cette méthode permet de simplifier l'opération NON bit à bit d'un vecteur.
         * On précise aussi la taille pour la détection d'erreur.
         * @param Taille C'est la taille du vecteur sur lequel on fait l'opération.
         * @param Entrees C'est le vecteur (lui même une liste de Erwan).
         * @return La liste des modélisation des signaux inclus dans le vecteur généré par l'opération.
         */
	public static List<Erwan> NOTR(int Taille, List<Erwan> Entrees) {
		if (Entrees.size() != Taille) throw new RuntimeException("Pb de taille"); //TODO changer l'erreur
		List<Erwan> R = new ArrayList<>();
                for (int c = 0; c < Taille; c++) {
			Erwan s = NOT(Entrees.get(c));
			R.add(s);
                }
		return R;
	}
	
	public static List<Erwan> NOTR(List<Erwan> Entrees) {
		return NOTR(Entrees.size(),Entrees);
	}

	/** Génération d'un vecteur Constant.
         * Cette méthode permet de simplifier la création d'un vecteur constant.
	 * On précise aussi la taille pour la détection d'erreur.
         * @param Taille C'est la taille des vecteur entre lesquelles on fait l'opération.
         * @param Constantes C'est la liste des Boolean représentant le vecteur Constant. Attention, un Boolean à null entrainera une erreur.
         * @return La liste des modélisation des signaux inclus dans le vecteur généré par l'opération.
         */
	public static List<Erwan> CONSTANTER(int Taille, List<Boolean> Constantes){
		if (Constantes.size() != Taille) throw new RuntimeException("Pb de taille"); //TODO changer l'erreur
                List<Erwan> R = new ArrayList<>();
                for (int c = 0; c < Taille; c++) {
			if (Constantes.get(c) == null) throw new RuntimeException("Boolean mal initialisé"); /* TODO changer l'erreur */
                        Erwan s = CONSTANTE(Constantes.get(c).booleanValue());
                        R.add(s);
                }
                return R;
        }
	
	public static List<Erwan> CONSTANTER(List<Boolean> Constantes){
		return CONSTANTER(Constantes.size(),Constantes);
	}

	/** Generation d'un signal constant à UP.
	 * @return Assez évident.
	 */
	public static Erwan CONSTANTE_UP() {
                return new Erwan(Operation.LITTERAL,null,"1");          //TODO J'hésite a faire des méthode cst 1 et 0
									    //TODO J'hésite a remplacer CONSTANTE par LITTERAL avec simplement
									    //des signaux qui s'appellent 0 et 1 ....
        }

	/** Generation d'un signal constant à DW.
         * @return Assez évident.
         */
	public static Erwan CONSTANTE_DW() {
                return new Erwan(Operation.LITTERAL,null,"0");
	}

	/** Génération des signaux grâce à un module.
	 * Pas encore implémenté !! TODO !!!
	 */
	public static List<Erwan> APPELMODULE(String NomModule, List<String> NomEntrees, List<String> NomSorties){
		return null ; //TODO A Faire
	}
}

