package simulateur;

import java.util.*;
import erwan.*;
import java.util.Scanner;

public class Experience5 {

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
			switch (E){
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
                                default :
                                        break;
                      	}
		}

		private String Nom() {
			return B.getNom();
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
			return B.getNom();
		}
        }

	public static Erwan A(String Nom, Erwan S){
		return Erwan.AFFECTATION(Nom,S);
	}

	public static List<Erwan> AR(String Nom,int d, int f,List<Erwan> S){
                return Erwan.ARANGE(Nom,d,f,S);
        }

	public static Erwan L(String Nom){
		return Erwan.LITTERAL(Nom);
	}

	public static Erwan E(List<Erwan> S) {
		return Erwan.AND(S);
	}

	public static List<Erwan> ER(int Taille, List<List<Erwan>> Entrees) {
                return Erwan.ANDR(Taille, Entrees);
	}

	public static Erwan O(List<Erwan> S) {
		return Erwan.OR(S);
	}

	public static List<Erwan> OR(int Taille, List<List<Erwan>> Entrees) {
                return Erwan.ORR(Taille, Entrees);
        }

	public static Erwan N(Erwan S){
		return Erwan.NOT(S);
	}

	public static List<Erwan> NR(int Taille, List<Erwan> S) {
                return Erwan.NOTR(Taille,S);
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
		PlanCircuit.addAll(AR("Sortie",0,3,L( E(L(N(L("a")),N(L("b")))), E(L(N(L("a")),L("b"))) , E(L(L("a"),N(L("b")))) ,E(L(L("a"),L("b"))) )));
		Descripteur DS = new Descripteur("Sortie",0,3);
	       	Descripteur DEa = new Descripteur("a");
		Descripteur DEb = new Descripteur("b");
		List<Descripteur> E = new ArrayList<>();
		E.add(DEa);
		E.add(DEb);
		List<Descripteur> S = new ArrayList<>();
		S.add(DS);
		erwan.Module M = new erwan.Module(PlanCircuit,E,S,null);
		//PlanCircuit.add(A("d",E(L(N(L("a")),L("b")))));
		//PlanCircuit.add(A("e",E(L(L("a"),N(L("b"))))));
		//PlanCircuit.add(A("f",E(L(L("a"),L("b")))));

		for (Erwan e : PlanCircuit) {
			System.out.println("Plan : " + e.Nom() + " = " + e.Entrees.get(0).Nom());
		}
		Simulateur Si = new FileSimulateur(M);
		List<En> Entrees = new ArrayList<>();
		List<So> Sorties = new ArrayList<>();
		for(int i = 1; i <= Si.nbEntree(); i++) {
			//System.out.println("On s'interresse à l'entree : " + Si.nomEntree(i));
			for(int j = 1; j <= Si.nbSlotEntree(i); j++){
				Entrees.add(new En(Si.nomEntree(i), Si.getEntrees(i,j)));
				//System.out.println("Entree Ajoutée avec succès !");
			}
		}
		for(int i = 1; i <= Si.nbSorties(); i++){
			//System.out.println("On s'interresse à la sortie : " + Si.nomSortie(i));
			for(int j = 1; j <= Si.nbSlotSortie(i); j++) {
				Sorties.add(new So(Si.nomSortie(i),Si.getSorties(i,j)));
			}
                }

		boolean fin = false;
		while(!fin) {
			AfficherEtatList(Entrees,Sorties);
			fin = Menu(Entrees);
		}
	}

	public static void AfficherEtatList(List<En> E, List<So> S){
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
		System.out.println("Choisissez une entrée de 1 à " + E.size() + " à modifier. \n Un autre entier ferme le programme.");
		int r = sc.nextInt();
		if (r < 1 || r > E.size()) return true;
		else {
		       E.get(r-1).click();
	       	       return false;	       
		}
	}

}

