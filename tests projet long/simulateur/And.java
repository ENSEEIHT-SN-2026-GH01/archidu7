package simulateur;

public class And extends Composant {

	public And() {
		super(2,1);
	}

	public void calculer() {
		boolean e1 = super.getEntree(1) == null ? false : super.get(1).booleanValue();
		boolean e2 = super.getEntree(2) == null ? false : super.get(2).booleanValue();
		super.setSortie(i,new Boolean( e1 && e2));
	}
}
