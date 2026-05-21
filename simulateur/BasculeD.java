package simulateur;

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
		public void calculer() {
			if (super.getEntree(4) == Etat.UP) {
				super.setSortie(1,Etat.DW);
				super.getConnecteurSortie(1).getComposant().calculer();
				super.setSortie(2,Etat.UP);
				super.getConnecteurSortie(2).getComposant().calculer();
			} else {
				if (super.getEntree(2) == Etat.UP && super.getEntree(1) == Etat.UP && this.montant) {
					super.setSortie(1,this.etat);
					super.getConnecteurSortie(1).getComposant().calculer();
					super.setSortie(2,Etat.E(this.etat.getValeur() * -1));
					super.getConnecteurSortie(2).getComposant().calculer();
					this.montant = false;
				} else if (super.getEntree(2) == Etat.DW) this.montant = true;
			}
		}

		public String getNom() {
			return "BasculeD";
		}

		public void sauv() {
			this.etat = super.getEntree(3);
		}
	}
