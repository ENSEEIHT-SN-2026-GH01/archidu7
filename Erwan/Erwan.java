import Operation;
import java.util.*;

public class Erwan {

	public Operation Operation;

	public List<Erwan> Entrees;

	public String Nom;

	private Erwan(Operation Operation, List<Erwan> Entrees, String Nom) {
		this.Operation = Operation;
		this.Entrees = Entrees;
		this.Nom = Nom;
	}

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

	public static Erwan RANGE(String Nom, List<Erwan> Entrees) {
                return new Erwan(Operation.RANGE,Entrees,Nom);
        }

	public static Erwan CONSTANTE(String Nom, List<Erwan> Entrees) {
                return new Erwan(Operation.CONSTANTE,Entrees,Nom);          //TODO J'hésite a faire des méthode cst 1 et 0
									    //TODO J'hésite a remplacer CONSTANTE par LITTERAL avec simplement
									    //des signaux qui s'appellent 0 et 1 ....
        }

	public static List<Erwan> MODULE(String NomModule, List<String> NomEntrees, List<String> NomSorties){
		return null ; //TODO A Faire
	}
}

