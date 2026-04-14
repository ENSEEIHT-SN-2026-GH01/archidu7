package simulateur;

import java.util.*;

public class FileSimulateur {

	private List<StructEntree> Entrees;
	private List<StructSortie> Sorties;
	private DicoConnecteur Dico;

	public FileSimulateur(){
		Dico = new DicoConnecteur();
		//TODO Creer les struct Entree et Sortie
		//TODO ajouter les signaux littéraux au dico
		//TODO generer les sigaux non littéraux et les enr au dico
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

	public BouttonEntree getEntrees(int i, int j)  {
		return new BouttonEntree(Entrees.get(i+1)j); //TODO
	}

	public Connecteur getSorties(int i, int j)  {
		return Sorties.get(i+1).getConnecteur(j); //TODO
	}



	//TODO ------------- ATTENTION ZONE CHANTIER !!! ------------- TODO//
	//								   //
	//TODO ---- INTERDITE A TOUTE PERSONNE NON AUTHORISEE !!! ---- TODO//
	//								   //
	//TODO ------------  PORT DU CASQUE OBLIGATOIRE : ------------ TODO//
	//								   //
	//TODO PETAGE DE CABLE ET CHUTE DE NEURONNES PREVUS A LA METEO TODO//

	
	
	private void AND() {
	}

	private void OR() {
	}

	private void NOT() {
	}

	private void RANGE() { // Correspond a plusieur assignation avec des liensVecteurs. Peux/doit appeller LITTERAL/CONSTANTE
	}

	private void LITTERAL() { //Permet de faire reference à une entree, ou un signal généré et nommé par l'utilisateur
	}

	private void CONSTANTE() {
	}

	private void MODULE() {
	}
}
