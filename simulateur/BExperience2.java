package simulateur;

import java.util.*;
import erwan.*;
import java.util.Scanner;

public class BExperience2 {

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

	public static List<Erwan> LR(String Nom, int d, int f) {
		return Erwan.LITTERANGE(Nom,d,f);
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

	public static List<List<Erwan>> LR(List<Erwan>... S) {
		List<List<Erwan>> l = new ArrayList<>();
                for (List<Erwan> e : S) {
                        l.add(e);
                }
                return l;
        }

	public static List<Erwan> RANGE(List<Erwan>... S) {
		List<Erwan> l = new ArrayList<>();
		for (List<Erwan> e : S) {
                        l.addAll(e);
                }
                return l;
        }



	public static void main(String[] Args) {
		List<Erwan> PlanCircuitModule = new ArrayList<>();
		PlanCircuitModule.addAll(
				RANGE(
					AR("D",0,0,NR(1,LR("C",0,0))),
					AR("D",1,1,O(
							RANGE(
								E(RANGE(LR("C",0,0), N(LR("C",1,1).get(0)).toList())).toList(),
								E(RANGE(LR("C",1,1), N(LR("C",0,0).get(0)).toList())).toList()
								)
							).toList())
				     )
					
						);
		List<AppelModule> Ap = new ArrayList<>();
		List<Descripteur> DE1 = new ArrayList<>();
		List<Descripteur> DE2 = new ArrayList<>();
		List<Descripteur> DS1 = new ArrayList<>();
		List<Descripteur> DS2 = new ArrayList<>();
		DE1.add(new Descripteur("en"));
		DE2.add(new Descripteur("en"));
		DE1.add(new Descripteur("clk"));
		DE2.add(new Descripteur("clk"));
		DE1.add(new Descripteur("D",0,0));
		DE2.add(new Descripteur("D",1,1));
		DE1.add(new Descripteur("rst"));
		DE2.add(new Descripteur("rst"));
		DS1.add(new Descripteur("C",0,0));
		DS2.add(new Descripteur("C",1,1));
		DS1.add(new Descripteur("Inutile",0,0));
		DS2.add(new Descripteur("Inutile",1,1));
		Ap.add(new AppelModule(new erwan.Module("$BaculeD",new ArrayList<>(),DE2,DS2,null),DE2,DS2));
		Ap.add(new AppelModule(new erwan.Module("$BaculeD",new ArrayList<>(),DE1,DS1,null),DE1,DS1));
		List<Descripteur> DE = new ArrayList<>();
		List<Descripteur> DS = new ArrayList<>();
		DE.add(new Descripteur("en"));
		DE.add(new Descripteur("clk"));
		DE.add(new Descripteur("rst"));
		DS.add(new Descripteur("C",0,1));
		DS.add(new Descripteur("D",0,1));
		Simulateur Si = new FileSimulateur(new erwan.Module("count2",PlanCircuitModule,DE,DS,Ap));
		List<En> Entrees = new ArrayList<>();
		List<So> Sorties = new ArrayList<>();
		for(int i = 1; i <= Si.nbEntree(); i++) {
			System.out.println("On s'interresse à l'entree : " + Si.nomEntree(i));
			for(int j = 1; j <= Si.nbSlotEntree(i); j++){
				Entrees.add(new En(Si.nomEntree(i), Si.getEntrees(i,j)));
				//System.out.println("Entree Ajoutée avec succès !");
			}
		}
		for(int i = 1; i <= Si.nbSorties(); i++){
			System.out.println("On s'interresse à la sortie : " + Si.nomSortie(i));
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

