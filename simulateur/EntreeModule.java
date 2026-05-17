package simulateur;
import java.util.*;

public class EntreeModule extends Composant {

	private BouttonEntree B;

	public EntreeModule(Connecteur s)  {
		super(1,0);
		super.brancherEntree(s,1);

	}

	public EntreeModule(Connecteur s, BouttonEntree B)  {
		super(1,0);
		super.brancherEntree(s,1);
		this.B = B;
	}

	public void setEntree(BouttonEntree B) {
		this.B = B;
	}

	public BouttonEntree getEntree() {
		return B;
	}

	public void calculer()  {
		if (B != null) {
			B.set(super.getEntree(1));
		}
        }
/*
	public void ajouter(Connecteur c)  {
		TableauConnecteur Tc = super.getE();
		TableauConnecteur Tc2 = new TableauConnecteur(Tc.getTaille() + 1);
		for (int i = 1; i <= Tc.getTaille(); i++) {
			Tc2.brancher(Tc.getConnecteur(i),i);
		}
		Tc2.brancher(c,Tc2.getTaille());
		super.setE(Tc2);
		
		
	}*/

/*
	@Override
	public void ajouter(List<Composant> L)  {
		super.ajouter(L);
		for (int i = 1; i <= super.getNbSortie(); i++) {
			Connecteur cd = getConnecteurSortie(i);
			if (cd.getComposant() != null) cd.getComposant().ajouter(L);
                }
	}
*/

	public String getNom() {
		return "EntreeModule";
	}
}
