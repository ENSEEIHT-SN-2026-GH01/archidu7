package simulateur;

import erwan.*;
import java.util.*;

public class BasculeDSimulateur implements Simulateur{	

	

	
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
		Composant C1 =new EntreeModule(this.bas.getConnecteurSortie(1));
		Composant C2 =new EntreeModule(this.bas.getConnecteurSortie(2));
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
