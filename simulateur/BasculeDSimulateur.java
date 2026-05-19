package simulateur;

import erwan.*;
import java.util.*;

public class BasculeDSimulateur implements Simulateur{	

	public class BasculeD extends Composant {

		public BasculeD(Connecteur en, Connecteur clock, Connecteur signal, Connecteur reset) {
			super(4,2);
			super.brancherEntree(en,1);
			super.brancherEntree(clock,2);
			super.brancherEntree(signal,3);
			super.brancherEntree(reset,4);
			super.brancherSortie(new Lien("Q"),1);
			super.brancherSortie(new Lien("/Q"),2);
		}

		@Override
		public void calculer() {
			if (super.getEntree(4) == Etat.UP) {
				super.setSortie(1,Etat.DW);
				super.setSortie(2,Etat.UP);
			} else {
				if (super.getEntree(2) == Etat.UP && super.getEntree(1) == Etat.UP) {
					super.setSortie(1,super.getEntree(3));
					super.setSortie(2,Etat.E(super.getEntree(3).getValeur() * -1));
				}
			}
		}

		public String getNom() {
			return "BasculeD";
		}
	}

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
			this.Con.setValeur(e);
			if (this.Com != null && e == Etat.UP) this.Com.calculer();
		}

		@Override
		public String getNom(){
			return this.Con.getNom();
		}
	}

	private List<BouttonEvenement> EntreesG;
	private List<Connecteur> SortiesG;
	private BasculeD bas;

	public BasculeDSimulateur(){
		Connecteur en = new Lien("en");
		Connecteur clock = new Lien("clock");
		Connecteur signal = new Lien("signal");
		Connecteur reset = new Lien("reset");
		this.bas = new BasculeD(en, clock,signal,reset);
		EntreesG = new ArrayList<>();
		SortiesG = new ArrayList<>();
		EntreesG.add(new BouttonEvenement(en,null));
		EntreesG.add(new BouttonEvenement(clock,this.bas));
		EntreesG.add(new BouttonEvenement(signal,null));
		EntreesG.add(new BouttonEvenement(reset,this.bas));
		SortiesG.add(this.bas.getConnecteurSortie(1));
		SortiesG.add(this.bas.getConnecteurSortie(2));
	}


	public int nbEntree(){
		return 4;
	}

	public int nbSorties(){
		return 2;
	}

	public String nomEntree(int i){
		return EntreesG.get(i-1).getNom(); //TODO
	}

	public String nomSortie(int i){
        return SortiesG.get(i-1).getNom(); //TODO
        }
	

	public int nbSlotEntree(int i) {
		EntreesG.get(i-1);
		return 1;
	}

	public int nbSlotSortie(int i) {
		SortiesG.get(i-1);
		return 1;
	}

	public BouttonEntree getEntrees(int i, int j)  {
		if (j != 1) System.out.println("tu es bouché, ma parole !");
		return EntreesG.get(i-1); //TODO
	}

	public Connecteur getSorties(int i, int j)  {
		if (j != 1) System.out.println("tu es bouché, ma parole !");
		return SortiesG.get(i-1); //TODO
	}


}
