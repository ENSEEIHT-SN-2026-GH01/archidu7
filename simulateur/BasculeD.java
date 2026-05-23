package simulateur;

import util.Pair;

public class BasculeD extends Composant {

		private boolean montant;
		private Etat etat;

		public BasculeD(Connecteur en, Connecteur clock, Connecteur signal, Connecteur reset) {
			super(4,2);
			super.brancherEntree(en,1);
			super.brancherEntree(clock,2);
			super.brancherEntree(signal,3);
			super.brancherEntree(reset,4);
			super.brancherSortie(new Lien("Q"),1);
			super.brancherSortie(new Lien("/Q"),2);
			this.montant = true;
			this.etat = Etat.ND;
		}

		@Override
		public void calculer(Propageur prop) {
			if (super.getEntree(4) == Etat.UP) {
				prop.add(new Pair<Connecteur,Etat>(sorties.getConnecteur(1), Etat.DW));
				prop.add(new Pair<Connecteur,Etat>(sorties.getConnecteur(2), Etat.UP));
			} else {
				if (super.getEntree(2) == Etat.UP && super.getEntree(1) == Etat.UP && this.montant) {
					prop.add(new Pair<Connecteur,Etat>(sorties.getConnecteur(1), etat));
					prop.add(new Pair<Connecteur,Etat>(sorties.getConnecteur(2), Etat.E(this.etat.getValeur() * -1)));
					this.montant = false;
				} else if (super.getEntree(2) == Etat.DW) this.montant = true;
			}
			prop.propagerSuivant();
		}

		public String getNom() {
			return "BasculeD";
		}

		public void sauv() {
			this.etat = super.getEntree(3);
		}
	}
