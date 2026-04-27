package simulateur;

import java.util.*;
import Erwan.*;
import java.util.Scanner;

public class Experience {

	public static Erwan A(String Nom, Erwan S){
		return Erwan.AFFECTATION(Nom,S);
	}

	public static Erwan L(String Nom){
		return Erwan.LITTERAL(Nom);
	}

	public static Erwan E(List<Erwan> S) {
		return Erwan.AND(S);
	}

	public static Erwan O(List<Erwan> S) {
		return Erwan.OR(S);
	}

	public static Erwan N(Erwan S){
		return Erwan.NOT(S);
	}

	public static List<Erwan> L(Erwan... S){
		List<Erwan> l = new ArrayList<>();
		for (Erwan e : S) {
			l.add(e);
		}
		return l;
	}

	public static void main(String[] Args) {

		List<Erwan> PlanCircuit = new ArrayList<>();
		PlanCircuit.add(A("c",E(L(N(L("a")),N(L("b"))))));
		PlanCircuit.add(A("d",E(L(N(L("a")),L("b")))));
		PlanCircuit.add(A("e",E(L(L("a"),N(L("b"))))));
		PlanCircuit.add(A("f",E(L(L("a"),L("b")))));

		Simulateur Si = new FileSimulateur(PlanCircuit);
		List<Etat> Entrees = new ArrayList<>();
		List<BouttonEntree> EntreesM = new ArrayList<>();
		List<Connecteur> Sorties = new ArrayList<>();
		for(int i = 1; i <= Si.nbEntree(); i++) {
			for(int j = 1; j <= Si.nbSlotEntree(i); j++){
				Entrees.add(Etat.ND);
				EntreesM.add(Si.getEntrees(i,j));
				System.out.println("Entree Ajoutée avec succès !");
			}
		}
		for(int i = 1; i <= Si.nbSorties(); i++){
			for(int j = 1; j <= Si.nbSlotSortie(i); j++) {
				Sorties.add(Si.getSorties(i,j));
			}
                }

		boolean fin = false;
		while(!fin) {
			AfficherEtatList(Entrees,Sorties);
			fin = Menu(Entrees,EntreesM);
		}
	}

	public static void AfficherEtatList(List<Etat> E, List<Connecteur> S){
		for (Etat e : E) {
			System.out.print(" >" + e);
		}
		System.out.println();
		for (Connecteur c : S) {
                        System.out.print(" >" + c.getNom() + " : " + c.getValeur());
                }
		System.out.println();
	}

	public static boolean Menu(List<Etat> E, List<BouttonEntree> EM) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Choisissez une entrée de 1 à " + E.size() + " à modifier. \n Un autre entier ferme le programme.");
		int r = sc.nextInt();
		if (r < 1 || r > E.size()) return true;
		else {
			switch ( E.get(r -1)){
				case Etat.ND:
					E.set(r-1, Etat.DW);
					EM.get(r-1).set(Etat.DW);
					return false;
				case Etat.DW:
                                        E.set(r-1, Etat.UP);
                                        EM.get(r-1).set(Etat.UP);
                                        return false;
				case Etat.UP:
                                        E.set(r-1, Etat.DW);
                                        EM.get(r-1).set(Etat.DW);
                                        return false;
				default :
					return true;
				}
		}
	}

}

