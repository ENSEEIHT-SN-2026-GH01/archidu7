package simulateur;
import java.util.*;

public class Multiplicateur extends Composant {

	public Multiplicateur(Connecteur c) throws ErreurIndex {
		super(1,2);
		super.brancherEntree(c,1);
	}

	public Multiplicateur(Connecteur s, Connecteur c1, Connecteur c2) throws ErreurIndex {
		super(1,2);
		super.brancherEntree(s,1);
		super.brancherSortie(c1,1);
		super.brancherSortie(c2,2);
	}

	public void calculer() throws ErreurIndex {
                for (int i = 1; i <= super.getNbSortie(); i++) {
                        super.setSortie(i,super.getEntree(1));
                }
        }

	public void ajouter(Connecteur c) throws ErreurIndex {
		TableauConnecteur Tc = super.getS();
		TableauConnecteur Tc2 = new TableauConnecteur(Tc.getTaille() + 1);
		for (int i = 1; i <= Tc.getTaille(); i++) {
			Tc2.brancher(Tc.getConnecteur(i),i);
		}
		Tc2.brancher(c,Tc2.getTaille());
		super.setE(Tc2);
	}

	@Override
	public void ajouter(List<Composant> L) throws ErreurIndex {
		super.ajouter(L);
		for (int i = 1; i <= super.getNbSortie(); i++) {
			Connecteur cd = getConnecteurSortie(i);
			if (cd.getComposant() != null) cd.getComposant().ajouter(L);
                }
	}

	public String getNom() {
		return "Multiplicateur";
	}
}
