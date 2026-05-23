package simulateur;
import java.util.*;

public class Constante extends Composant {

	private Etat Val;

	public Constante(Connecteur c, boolean b)  {
		super(0,1);
		this.Val = b ? Etat.UP : Etat.DW;
		super.brancherSortie(c,1);
	}

	public void calculer(Propageur prop)  {
        propager(Val, prop);
    }

	/*
	public void ajouter(Connecteur c)  {
		TableauConnecteur Tc = super.getS();
		TableauConnecteur Tc2 = new TableauConnecteur(Tc.getTaille() + 1);
		for (int i = 1; i <= Tc.getTaille(); i++) {
			Tc2.brancher(Tc.getConnecteur(i),i);
		}
		Tc2.brancher(c,Tc2.getTaille());
		super.setS(Tc2);
	}
	*/

	public String getNom() {
		return "Constante";
	}
}
