package simulateur;

import java.util.*;

public class FileSimulateur {

	private List<StructEntree> Entrees;
	private List<StructSortie> Sorties;
	private DicoConnecteur Dico;

	public FileSimulateur(){
		Dico = new DicoConnecteur();
	}

	public int nbEntree(){
		return Entrees.size();
	}

	public int nbSorties(){
		return Sorties.size();
	}

	public String nomEntree(int i){
		return Entrees.get(i+1).getNom(); //TODO
	}

	public String nomSortie(int i){
                return Sorties.get(i+1).getNom(); //TODO
        }
	

	public int nbSlotEntree(int i) {
		return Entrees.get(i+1).getNombre();//TODO
	}

	public int nbSlotSortie(int i) {
		return Sorties.get(i+1).getNombre(); //TODO
	}

	public Connecteur getEntrees(int i, int j) throws ErreurIndex {
		return Entrees.get(i+1).getConnecteur(j); //TODO
	}

	public Connecteur getSorties(int i, int j) throws ErreurIndex {
		return Sorties.get(i+1).getConnecteur(j); //TODO
	}

}
