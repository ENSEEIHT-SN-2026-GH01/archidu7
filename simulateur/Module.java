package simulateur;

import java.util.*;
import simulateur.Erwan.*;

public class Module {

	public String Nom;
	public List<Erwan> Plan;
	public List<Descripteur> Entrees;
	public List<Descripteur> Sorties;
	public List<AppelModule> Branchements;

	public Module(String Nom, List<Erwan> Plan, List<Descripteur> Entrees, List<Descripteur> Sorties, List<AppelModule> Branchements) {
		this.Nom = Nom;
		this.Plan = Plan;
		this.Entrees = Entrees;
		this.Sorties = Sorties;
		this.Branchements = Branchements;
	}

	public Module(List<Branchement> Plan, List<Descripteur> Entrees, List<Descripteur> Sorties) {
		List<Erwan> plans = new LinkedList<>();
		List<AppelModule> branchements = new LinkedList<>();

		for (var e : Plan) {
			if (e instanceof Erwan p) {
				plans.add(p);
			} else if (e instanceof AppelModule a) {
				branchements.add(a);
			}
		}

		this.Plan = plans;
		this.Entrees = Entrees;
		this.Sorties = Sorties;
		this.Branchements = branchements;
	}
}
