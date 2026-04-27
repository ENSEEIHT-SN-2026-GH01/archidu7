package simulateur;

import java.util.*;
import Erwan.*;

public class FileSimulateur implements Simulateur{

	private List<StructEntree> EntreesG;
	private List<StructSortie> SortiesG;
	private DicoConnecteur Dico;

	public FileSimulateur(List<Erwan> Plan){
		Dico = new DicoConnecteur();
		construction(Plan);
		//TODO Creer les struct Entree et Sortie
		//TODO ajouter les signaux littéraux au dico
		//TODO generer les sigaux non littéraux et les enr au dico
	}

	public int nbEntree(){
		return EntreesG.size();
	}

	public int nbSorties(){
		return SortiesG.size();
	}

	public String nomEntree(int i){
		return EntreesG.get(i-1).getNom(); //TODO
	}

	public String nomSortie(int i){
                return SortiesG.get(i-1).getNom(); //TODO
        }
	

	public int nbSlotEntree(int i) {
		return EntreesG.get(i-1).getNombre();//TODO
	}

	public int nbSlotSortie(int i) {
		return SortiesG.get(i-1).getNombre(); //TODO
	}

	public BouttonEntree getEntrees(int i, int j)  {
		return new BouttonEntree(EntreesG.get(i-1),j); //TODO
	}

	public Connecteur getSorties(int i, int j)  {
		return SortiesG.get(i-1).getConnecteur(j); //TODO
	}



	//TODO ------------- ATTENTION ZONE CHANTIER !!! ------------- TODO//
	//								   //
	//TODO ---- INTERDITE A TOUTE PERSONNE NON AUTHORISEE !!! ---- TODO//
	//								   //
	//TODO ------------  PORT DU CASQUE OBLIGATOIRE : ------------ TODO//
	//								   //
	//TODO PETAGE DE CABLE ET CHUTE DE NEURONNES PREVUS A LA METEO TODO//

	
	
	private void construction(List<Erwan> Plan) {
		Set<String> NomEntrees = new HashSet<>();
		Set<String> NomSorties = new HashSet<>();
		for (Erwan S : Plan) {
			if (S.Op != Operation.AFFECTATION) throw new RuntimeException("Pb de structure");
			NomSorties.add(S.Nom());
			construire(S.Entrees.get(0),NomEntrees);
		}
		Set<String> inter = new HashSet<>(NomEntrees);
		inter.retainAll(NomSorties);
		NomEntrees.removeAll(inter);
		NomSorties.removeAll(inter);
		//TODO 1ere version sans prendre en compte les vecteurs !!!
		this.EntreesG = new ArrayList<>();
		this.SortiesG = new ArrayList<>();
		for (String nom : NomEntrees) {
			TableauConnecteur T = new TableauConnecteur(1);
			T.brancher(Dico.getConnecteur(nom),1);
			this.EntreesG.add(new StructEntree(nom,T));
		}
		for (String nom : NomSorties) {
                        TableauConnecteur T = new TableauConnecteur(1);
                        T.brancher(Dico.getConnecteur(nom),1);
                        this.SortiesG.add(new StructSortie(nom,T));
                }
	}

	private void construire(Erwan Signal, Set<String> E) {
		if (Dico.existe(Signal.Nom())) throw new RuntimeException("Signal déjà attribué :" + Signal.Nom());
		Connecteur Sig = recupSignal(Signal.Entrees.get(0),E);
		//TODO à finir
		Dico.ajouter(Sig,Signal.Nom());
	}

	private Connecteur recupSignal(Erwan S, Set<String> E) {
		if (Dico.existe(S.Nom())) return Dico.getConnecteur(S.Nom()).getSignal(Dico);
		else {
			switch (S.Op) {
				case Operation.LITTERAL:
					E.add(S.Nom());
					return Dico.getConnecteur(S.Nom());
				case Operation.NOT:
					Connecteur Entree = recupSignal(S.Entrees.get(0),E);
					Connecteur Sortie = Dico.getConnecteur(S.Nom());
					Composant N = new Not(Entree,Sortie);
					return Sortie;
				case Operation.AND :
					List<Connecteur> LAEntrees = new ArrayList<>();
					Connecteur Sortie1 = Dico.getConnecteur(S.Nom());
					for (Erwan e : S.Entrees) {
						LAEntrees.add(recupSignal(e,E));
					}
					Composant A = new And(LAEntrees, Sortie1);
					return Sortie1;
				case Operation.OR :
					List<Connecteur> LEntrees = new ArrayList<>();
                                        Connecteur Sortie2 = Dico.getConnecteur(S.Nom());
                                        for (Erwan e : S.Entrees) {
                                                LEntrees.add(recupSignal(e,E));
                                        }
                                        Composant O = new Or(LEntrees, Sortie2);
					return Sortie2;
				default:
					throw new RuntimeException("Pb de création dans le Erwan : " + S.Nom());
			}
		}
	}

/*
	private Connecteur getSignal(){
		return null;
	}

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
	*/
}
