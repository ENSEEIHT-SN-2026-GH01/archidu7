package Erwan;

import java.util.*.

public class Module {

	public List<Erwan> Plan;
	public List<Descripteur> Entrees;
	public List<Descripteur> Sorties;
	public List<AppelModule> Branchements;

	public Module(List<Erwan> Plan, List<Descripteur> Entrees, List<Descripteur> Sorties, List<AppelModule> Branchements) {
		this.Plan = Plan;
		this.Entrees = Entrees;
		this.Sorties = Sorties;
		this.Branchements = Branchements;
	}
}
