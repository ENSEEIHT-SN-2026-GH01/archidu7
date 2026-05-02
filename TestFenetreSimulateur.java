import java.util.*;
import Erwan.*;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Scanner;
import simulateur.*;
import simulateur.affichage.*;

/**test basé sur le Experience2. */
public class TestFenetreSimulateur extends Application{

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

	public void start(Stage fen) {

		List<Erwan> PlanCircuit = new ArrayList<>();
		PlanCircuit.add(A("d", E( L( N( L("a") ), N(L("b") ),N( L( "c"))) ) ) );
		PlanCircuit.add(A("e", E( L( N( L("a") ), N(L("b") ), L( "c")) ) ) );
		PlanCircuit.add(A("f",E(L(N(L("a")),L("b"),N(L("c"))))));
		PlanCircuit.add(A("g",E(L(N(L("a")),L("b"),L("c")))));
		PlanCircuit.add(A("h",E(L(L("a"),N(L("b")),N(L("c"))))));
		PlanCircuit.add(A("i",E(L(L("a"),N(L("b")),L("c")))));
		PlanCircuit.add(A("j",E(L(L("a"),L("b"),N(L("c"))))));
		PlanCircuit.add(A("k",E(L(L("a"),L("b"),L("c")))));

		for (Erwan e : PlanCircuit) {
			System.out.println("Plan : " + e.Nom() + " = " + e.Entrees.get(0).Nom());
		}
		Simulateur Si = new FileSimulateur(PlanCircuit);
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

		FenetreSimulateur scene = new FenetreSimulateur(Si);
		fen.setScene(scene);
		fen.setTitle("simulateur");
		fen.show();
	}

	public static void main(String args[]){
		launch(args);
	}
}

