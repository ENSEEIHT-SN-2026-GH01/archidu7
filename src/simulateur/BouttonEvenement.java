package simulateur;

import util.Pair;

public class BouttonEvenement extends BouttonEntree {

		private Connecteur Con;
		private Composant Com;

		public BouttonEvenement(Connecteur Con, Composant Com) {
			super(null,0);
			this.Con = Con;
			this.Com = Com;
		}

		public BouttonEvenement(Connecteur Con) {
			super(null,0);
			this.Con = Con;
			this.Com = null;
		}

		@Override
		public void set(Etat e) {
			Propageur prop = new Propageur();
			prop.add(new Pair<Connecteur,Etat>(Con, e));
			if (this.Com != null) this.Com.calculer(prop);
			while (!prop.isEmpty()) {
				prop.propagerSuivant();
			}
		}

		public void sauv() {
			if (this.Com != null) {
				BasculeD B = (BasculeD) this.Com;
				B.sauv();
			}
		}

		@Override
		public String getNom(){
			return this.Con.getNom();
		}
	}

