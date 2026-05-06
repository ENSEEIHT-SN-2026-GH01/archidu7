import java.util.*;

import javafx.application.Application;
import javafx.stage.Stage;

import simulateur.*;
import simulateur.Erwan.*;
import simulateur.affichage.*;

/** test basé sur le Experience2. */
public class TestFenetreSimulateur extends Application {

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

	public void start(Stage fen) {

		List<Erwan> PlanCircuit = new ArrayList<>();
		PlanCircuit.add(A("d", E(L(N(L("a")), N(L("b")), N(L("c"))))));
		PlanCircuit.add(A("e", E(L(N(L("a")), N(L("b")), L("c")))));
		PlanCircuit.add(A("f", E(L(N(L("a")), L("b"), N(L("c"))))));
		PlanCircuit.add(A("g", E(L(N(L("a")), L("b"), L("c")))));
		PlanCircuit.add(A("h", E(L(L("a"), N(L("b")), N(L("c"))))));
		PlanCircuit.add(A("i", E(L(L("a"), N(L("b")), L("c")))));
		PlanCircuit.add(A("j", E(L(L("a"), L("b"), N(L("c"))))));
		PlanCircuit.add(A("k", E(L(L("a"), L("b"), L("c")))));

		for (Erwan e : PlanCircuit) {
			System.out.println("Plan : " + e.Nom() + " = " + e.Entrees.get(0).Nom());
		}
		Simulateur Si = new FileSimulateur(PlanCircuit);

		FenetreSimulateur scene = new FenetreSimulateur(Si);
		fen.setScene(scene);
		fen.setTitle("simulateur");
		fen.show();
	}

	public static void main(String args[]) {
		launch(args);
	}
}
