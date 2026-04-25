package simulateur;
import java.util.*;

public class Or extends Composant {

	public Or(Connecteur s1, Connecteur s2, String nomResultat)  {
		super(2,1);
		super.brancherEntree(s1,1);
		super.brancherEntree(s2,2);
		super.brancherSortie(new Lien(nomResultat),1);

	}

	public Or(Connecteur s1, Connecteur s2, Connecteur r)  {
		super(2,1);
		super.brancherEntree(s1,1);
		super.brancherEntree(s2,2);
		super.brancherSortie(r,1);
	}

	public Or(List<Connecteur> Entrees, Connecteur Sortie) {
		super(Entrees,Sortie);
	}

	public void calculer()  {
		boolean fini = false;
		int i = 1;
		Etat e = Etat.DW;
		while (!fini) {
			if (super.getEntree(i).getValeur() > e.getValeur()) e = super.getEntree(i);
			fini = (i >= super.getNbEntree()) || (e.getValeur() == 1);
			i++;
		}
		super.setSortie(1,e);

        }

	public void ajouter(Connecteur c)  {
		TableauConnecteur Tc = super.getE();
		TableauConnecteur Tc2 = new TableauConnecteur(Tc.getTaille() + 1);
		for (int i = 1; i <= Tc.getTaille(); i++) {
			Tc2.brancher(Tc.getConnecteur(i),i);
		}
		Tc2.brancher(c,Tc2.getTaille());
		super.setE(Tc2);
	}

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
		return "Or";
	}
}
