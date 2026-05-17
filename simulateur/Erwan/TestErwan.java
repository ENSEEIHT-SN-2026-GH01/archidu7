package simulateur.Erwan;
import java.util.*;

public class TestErwan {

	public static void main(String[] Arg) {

		Erwan R1, R2;

		List<Erwan> L1, L2;

		L1 = new ArrayList<>(); L2 = new ArrayList<>();

		L1.add(Erwan.LITTERAL("A")); L1.add(Erwan.LITTERAL("B")); L1.add(Erwan.LITTERAL("C"));

		L2.add(Erwan.NOT(Erwan.LITTERAL("A"))); L2.add(Erwan.LITTERAL("B"));

		R1 = Erwan.NOT(Erwan.OR(L1));

		R2 = Erwan.AND(L2);

		List<Erwan> L3 = new ArrayList<>(); L3.add(R1) ; L3.add(R2);

		System.out.println("Nom R1 : " + R1.Nom());
		System.out.println("Nom R2 : " + R2.Nom());
		System.out.println("Nom /A : " + Erwan.NOT(Erwan.LITTERAL("A")).Nom());
		System.out.println("Nom R1 + R2 : " + Erwan.OR(L3).Nom());
	}
}
