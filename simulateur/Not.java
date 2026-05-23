package simulateur;
import java.util.*;

public class Not extends Composant {

	public Not(Connecteur s, String nomResultat)  {
		super(1,1);
		super.brancherEntree(s,1);
		super.brancherSortie(new Lien(nomResultat),1);

	}

	public Not(Connecteur s, Connecteur r)  {
		super(1,1);
		super.brancherEntree(s,1);
		super.brancherSortie(r,1);
	}

	public void calculer(Propageur prop)  {
		switch (super.getEntree(1)) {
			case Etat.UP:
				propager(Etat.DW, prop);
				break;
			case Etat.ND:
				propager(Etat.ND, prop);
				break;
			case Etat.DW:
				propager(Etat.UP, prop);
				break;
			default:
				break;
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
		return "Not";
	}
}
