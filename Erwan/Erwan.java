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
}
