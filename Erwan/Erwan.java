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

	public static Erwan AFFECTATION(String Nom, Erwan Entree) {
		return new Erwan(Operation.AFFECTACTION,E

	public static Erwan AND(String Nom, List<Erwan> Entrees) {
		return new Erwan(Operation.AND,Entrees,Nom);
	}

	public static Erwan OR(String Nom, List<Erwan> Entrees) {
                return new Erwan(Operation.AND,Entrees,Nom);
        }

	public static Erwan NOT(String Nom, Erwan Entree) {
		List<Erwan> Entrees = new ArrayList<>();
		Entrees.add(Entree);
                return new Erwan(Operation.AND,Entrees,Nom);
        }

	public static Erwan LITTERAL(String Nom) {
                return new Erwan(Operation.LITTERAL,null,Nom);
        }

	public static List<Erwan> ARANGE(String Nom,int IndiceDebut, int IndiceFin, List<Erwan> Entrees) {
		List<Erwan> R = new ArrayList<>();
		int i = IndiceDebut;
		for(Erwan e : Entrees
                return R;
        }

	public static List<Erwan> ANDR(String Nom,int IndiceDebut, int IndiceFin, List<List<Erwan>> Entrees) {
		List<Erwan> R = new ArrayList<>();
                int i = IndiceDebut;
                for(Erwan e : Entrees) {
			Erwan s = AND(Nom,e);
			s.Numero = new Integer(i);
			R.add(s);
			i = i + 1;
		}
		//TODO si i != IndiceFin Erreur ou alors on vire IndiceFin
                return R;
        }

	public static List<Erwan> ORR(String Nom,int IndiceDebut, int IndiceFin, List<List<Erwan>> Entrees) {
                List<Erwan> R = new ArrayList<>();
                int i = IndiceDebut;
                for(Erwan e : Entrees) {
                        Erwan s = OR(Nom,e);
                        s.Numero = new Integer(i);
                        R.add(s);
                        i = i + 1;
                }
		//TODO si i != IndiceFin Erreur ou alors on vire IndiceFin
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

	public static List<Erwan> MODULE(String NomModule, List<String> NomEntrees, List<String> NomSorties){
		return null ; //TODO A Faire
	}
}

