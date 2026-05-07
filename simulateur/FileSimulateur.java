package simulateur;

import java.util.*;
import erwan.*;

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

	public FileSimulateur(erwan.Module M){
		this(M.Plan);
		List<StructEntree> EntreeUtilisateur = new ArrayList<>();
		List<StructEntree> EntreeModule;
		List<StructSortie> SortieUtilisateur = new ArrayList<>();
		List<StructSortie> SortieModule = new ArrayList<>();
		for (Descripteur DE : M.Entrees) {
			boolean trouve = false;
			TableauConnecteur T  = new TableauConnecteur(DE.indiceFin() - DE.indiceDebut() + 1);
			int curseurEntree = 1;
			for (String Nom : DE.Noms()) {
				int curseur = 0;
	 			while (!trouve && curseur < EntreesG.size()){
					if (EntreesG.get(curseur).getNom().equals(Nom)) {
						EntreesG.remove(curseur);
						trouve = true;
					}
					curseur += 1;
				}
				if (!trouve) throw new RuntimeException("Il manque une entree : " + Nom +". \nVeuillez verifier que ce signal est lu et n'est pas déjà généré par le circuit.");		
				T.brancher(Dico.getConnecteur(Nom),curseurEntree);
				curseurEntree += 1;
			}
			EntreeUtilisateur.add(new StructEntree(DE.Nom(),T));
		}
		EntreeModule = EntreesG;
		EntreesG = EntreeUtilisateur;

		for (Descripteur DE : M.Sorties) {
                        boolean trouve = false;
                        TableauConnecteur T  = new TableauConnecteur(DE.indiceFin() - DE.indiceDebut() + 1);
                        int curseurSortie = 1;
                        for (String Nom : DE.Noms()) {
                                int curseur = 0;
                                while (!trouve && curseur < EntreesG.size()){
                                        if (SortiesG.get(curseur).getNom().equals(Nom)) {
                                                SortiesG.remove(curseur);
                                                trouve = true;
                                        }
                                        curseur += 1;
                                }
                                if (!Dico.existe(Nom)) throw new RuntimeException("Il manque une sortie : " + Nom +". \nVeuillez verifier que ce signal est  et n'est pas déjà généré par le circuit.");
				//System.out.println("On cherche : " + Nom);
                                T.brancher(Dico.getConnecteur(Nom),curseurSortie);
				//System.out.println("on recupère : " + Dico.getConnecteur(Nom).getNom());
                                curseurSortie += 1;
                        }
                        SortieUtilisateur.add(new StructSortie(DE.Nom(),T));
                }
                SortieModule = SortiesG;
		SortiesG = SortieUtilisateur;

		//TODO Appel module !!! TODO



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
			//System.out.println("Sortie détectée : " + S.Nom());
			construire(S,NomEntrees);
		}
		/*for (String s : NomEntrees) {
			System.out.println("Le signal " + s + " est lue");
		}
		for (String s : NomSorties) {
                        System.out.println("Le signal " + s + " est généré");
                }*/
		Set<String> inter = new HashSet<>(NomEntrees);
		inter.retainAll(NomSorties);
		/*for (String s : inter) {
                        System.out.println("Le signal " + s + " est lue et généré");
                }*/
		NomEntrees.removeAll(inter);
		NomSorties.removeAll(inter);
		/*for (String s : NomEntrees) {
                        System.out.println("Le signal " + s + " est conservé pour l'entrée");
                }
                for (String s : NomSorties) {
                        System.out.println("Le signal " + s + " est conservé pour la sortie");
                }*/
		//TODO 1ere version sans prendre en compte les vecteurs !!!
		//System.out.println("Fin construction ! \nDébut de créations des strctures d'entrées et de sorties !");
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
		//System.out.println("La Construction s'est bien passée !");
	}

	private void construire(Erwan Signal, Set<String> E) {
		//if (Dico.existe(Signal.Nom())) throw new RuntimeException("Signal déjà attribué :" + Signal.Nom());
		//System.out.println(" >>> Création du signal <<< : " + Signal.Nom());
		Connecteur Sig = recupSignal(Signal.Entrees.get(0),E);
		//TODO à finir
		Connecteur SN = Dico.getConnecteurE(Signal);  // <-- modif ici TODO
		Multiplicateur M;
		if (Sig.getComposant() == null) {
			M = new Multiplicateur(Sig,SN);
		} else {
			if (Sig.getComposant() instanceof Multiplicateur) {
				M = (Multiplicateur) Sig.getComposant();
				M.ajouter(SN);
			} else {
				Sig.getSignal(Dico);
				M = (Multiplicateur) Sig.getComposant();
				M.ajouter(SN);
			}
		}
		Dico.ajouter(SN,Signal.Nom());
	}

	private Connecteur recupSignal(Erwan S, Set<String> E) {
		if (Dico.existe(S.Nom())) {
			//System.out.println("Lien déjà Connu : " + S.Nom());
			if (S.Op == Operation.LITTERAL) E.add(S.Nom());
			/*else {
				if (Dico.getConnecteur//TODO(S.Nom()).getOrigine() == null) { 
					System.out.println(S.Nom() + " < INVISIBLE");
				}
			}*/
			return Dico.getConnecteurE(S).getSignal(Dico); // <-- modif ici TODO
		} else {
			//System.out.println("Lien pas connu : " + S.Nom());
			switch (S.Op) {
				case Operation.LITTERAL:
					E.add(S.Nom());
					//System.out.println("Entree détectée : " + S.Nom());
					return Dico.getConnecteurE(S); // <-- modif ici TODO
				case Operation.NOT:
					Connecteur Entree = recupSignal(S.Entrees.get(0),E);
					Connecteur Sortie = Dico.getConnecteurE(S); // <-- midif ici TODO
					//System.out.println("NOT détectée : " + S.Nom());
					Composant N = new Not(Entree,Sortie);
					return Sortie;
				case Operation.AND :
					//System.out.println("AND détectée : " + S.Nom());
					List<Connecteur> LAEntrees = new ArrayList<>();
					Connecteur Sortie1 = Dico.getConnecteurE(S); // <-- mofif ici TODO
					for (Erwan e : S.Entrees) {
						LAEntrees.add(recupSignal(e,E));
					}
					Composant A = new And(LAEntrees, Sortie1);
					return Sortie1;
				case Operation.OR :
					//System.out.println("OR détectée : " + S.Nom());
					List<Connecteur> LEntrees = new ArrayList<>();
                                        Connecteur Sortie2 = Dico.getConnecteurE(S); // <- mofi ici TODO
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
