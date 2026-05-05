package simulateur;

import java.util.*;
import java.util.Scanner;

import simulateur.Erwan.*;

public class Experience4 {

	private static class En {
		private String Nom;
		private Etat E;
		private BouttonEntree B;

		private En(String Nom, BouttonEntree B) {
			this.Nom = Nom;
			E = Etat.ND;
			this.B = B;
		}

		private void click() {
			switch (E) {
				case Etat.ND:
					E = Etat.DW;
					B.set(Etat.DW);
					break;
				case Etat.DW:
					E = Etat.UP;
					B.set(Etat.UP);
					break;
				case Etat.UP:
					E = Etat.DW;
					B.set(Etat.DW);
					break;
				default:
					break;
			}
		}

		private String Nom() {
			return Nom;
		}
	}

	private static class So {
		private String Nom;
		private Connecteur B;

		private So(String Nom, Connecteur B) {
			this.Nom = Nom;
			this.B = B;
		}

		private Etat Val() {
			return B.getValeur();
		}

		private String Nom() {
			return Nom;
		}
	}

	public static Erwan A(String Nom, Erwan S) {
		return Erwan.AFFECTATION(Nom, S);
	}

	public static Erwan L(String Nom) {
		return Erwan.LITTERAL(Nom);
	}

	public static Erwan E(List<Erwan> S) {
		return Erwan.AND(S);
	}

	public static Erwan O(List<Erwan> S) {
		return Erwan.OR(S);
	}

	public static Erwan N(Erwan S) {
		return Erwan.NOT(S);
	}

	public static List<Erwan> L(Erwan... S) {
		List<Erwan> l = new ArrayList<>();
		for (Erwan e : S) {
			l.add(e);
		}
		return l;
	}

	public static void main(String[] Args) {

		List<Erwan> PlanCircuit = new ArrayList<>();
		PlanCircuit.add(A("q", N(O(L(L("S"), L("nq"))))));
		PlanCircuit.add(A("nq", N(O(L(L("R"), L("q"))))));
		PlanCircuit.add(A("S", N(O(L(L("J"), L("nq"), L("clk"))))));
		PlanCircuit.add(A("R", N(O(L(L("K"), L("q"), L("clk"))))));
		PlanCircuit.add(A("Q", L("q")));
		PlanCircuit.add(A("NQ", L("nq")));

		for (Erwan e : PlanCircuit) {
			System.out.println("Plan : " + e.Nom() + " = " + e.Entrees.get(0).Nom());
		}
		Simulateur Si = new FileSimulateur(PlanCircuit);
		List<En> Entrees = new ArrayList<>();
		List<So> Sorties = new ArrayList<>();
		for (int i = 1; i <= Si.nbEntree(); i++) {
			// System.out.println("On s'interresse à l'entree : " + Si.nomEntree(i));
			for (int j = 1; j <= Si.nbSlotEntree(i); j++) {
				Entrees.add(new En(Si.nomEntree(i), Si.getEntrees(i, j)));
				// System.out.println("Entree Ajoutée avec succès !");
			}
		}
		for (int i = 1; i <= Si.nbSorties(); i++) {
			// System.out.println("On s'interresse à la sortie : " + Si.nomSortie(i));
			for (int j = 1; j <= Si.nbSlotSortie(i); j++) {
				Sorties.add(new So(Si.nomSortie(i), Si.getSorties(i, j)));
			}
		}

		boolean fin = false;
		while (!fin) {
			AfficherEtatList(Entrees, Sorties);
			fin = Menu(Entrees);
		}
	}

	public static void AfficherEtatList(List<En> E, List<So> S) {
		for (En e : E) {
			System.out.print(" >" + e.Nom() + " : " + e.E);
		}
		System.out.println();
		for (So c : S) {
			System.out.print(" >" + c.Nom() + " : " + c.Val());
		}
		System.out.println();
	}

	public static boolean Menu(List<En> E) {
		Scanner sc = new Scanner(System.in);
		System.out
				.println("Choisissez une entrée de 1 à " + E.size() + " à modifier. \n Un autre entier ferme le programme.");
		int r = sc.nextInt();
		if (r < 1 || r > E.size())
			return true;
		else {
			E.get(r - 1).click();
			return false;
		}
	}

}
