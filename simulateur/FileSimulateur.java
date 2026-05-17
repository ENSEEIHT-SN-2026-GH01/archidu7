package simulateur;

import simulateur.Erwan.Erwan;
import java.util.*;
import simulateur.Erwan.*;

public class FileSimulateur implements Simulateur{

	private List<StructEntree> EntreesG;
	private List<StructSortie> SortiesG;
	private DicoConnecteur Dico;
	private List<Simulateur> ModulesAppeles;

	public FileSimulateur(List<Erwan> Plan){
		Dico = new DicoConnecteur();
		construction(Plan);
		//TODO Creer les struct Entree et Sortie
		//TODO ajouter les signaux littéraux au dico
		//TODO generer les sigaux non littéraux et les enr au dico
	}

	public FileSimulateur(Module M){
		this(M.Plan);
		List<Connecteur> SignauxParModule = new ArrayList<>();
		//Vérification des appels Modules
                if (M.Branchements != null) {
                        ModulesAppeles = new ArrayList<>();
                        for (AppelModule A : M.Branchements) {
                                //Vérification des formats entrées et sorties
                                if (A.DE.size() != A.module.Entrees.size()) throw new RuntimeException("Pas le bon nombre d'entrées durant l'appel du module" + A.module.Nom);
                                if (A.DS.size() != A.module.Sorties.size()) throw new RuntimeException("Pas le bon nombre de sortie lors de l'appel du module " + A.module.Nom);
                                //Il reste encore la vérification du nombre de signaux par entrée/sortie TODO.
                                for (int curs = 0; curs < A.DE.size(); curs ++) {
                                        Descripteur De1 = A.DE.get(curs);
                                        Descripteur De2 = A.module.Entrees.get(curs);
                                        if ((De1.nbSignaux() != De2.nbSignaux())) throw new RuntimeException("pas le bon nombre de signaux pour l'entree :" + De2.Nom() + ", du module :" + A.module.Nom);
                                }
                                for (int curs = 0; curs < A.DS.size(); curs ++) {
                                        Descripteur De1 = A.DS.get(curs);
                                        Descripteur De2 = A.module.Sorties.get(curs);
                                        if (De1.nbSignaux() != De2.nbSignaux()) throw new RuntimeException("pas le bon nombre de signaux pour la sortie :" + De2.Nom() + ", du module : " + A.module.Nom);
                                }
                                //On s'est assuré de la bonne formation de l'appel.
                        }
			//On s'occupe des sorties de notre module qui sont des entrées pour les modules appelés.
			//SignauxParModule = new ArrayList<>(); //Signaux générées par des modules, pas encores existants, pour des modules
			for (AppelModule A : M.Branchements) {
				Simulateur Smodule = new FileSimulateur(A.module);
				ModulesAppeles.add(Smodule);
				for (int curs = 0; curs < A.DE.size(); curs++){
					Descripteur DS = A.DE.get(curs);   //Il s'agit de descripteur de sortie de notre circuit.
					int curs2 = 0;
					for (Erwan signal : DS.Erwans()) {
						Connecteur CS = null;
						if (!Dico.existe(signal.Nom())) SignauxParModule.add(CS = Dico.getConnecteurE(signal));
						else CS = Dico.getConnecteurE(signal).getSignal(Dico);
						BouttonEntree CE = Smodule.getEntrees(curs+1,curs2+1);
						Composant CA = new EntreeModule(CS,CE);
						System.out.println("\n <>>> Nouveau Branchement : " + CS.getNom() + " de " + M.Nom + " avec " + CE.getNom() + "\n");
						//Verifier et retirer de SortieG au cas où 
						//Permet de verifier à la fin les signaux générés por riens.
						//TODO
						int curseur = 0;
            					boolean trouve = false;
						//System.out.println("Rech de : " + Nom);
                				while (!trouve && curseur < SortiesG.size()){
							//System.out.println(" >> Tentative de match : " + SortiesG.get(curseur).getNom());
							if (SortiesG.get(curseur).getNom().equals(signal.Nom())) {
               	   						SortiesG.remove(curseur);
               	 	    					trouve = true;
								//System.out.println("trouvé");
                					}
                					curseur += 1;
						}
						curs2 ++;
					}
				}
			}
		}
		List<StructEntree> EntreeUtilisateur = new ArrayList<>();
		List<StructEntree> entreeModule;
		List<StructSortie> SortieUtilisateur = new ArrayList<>();
		List<StructSortie> SortieModule;
		/* TODO 
		 * Avis à la population.
		 * Il faudrait faire un parcours des entres/sorties des appelmodule pour générer des liens et structentre correspondants.
		 */

		//Sorties avant les entrées car on rajoute un petit composant ...
		for (Descripteur DE : M.Sorties) {
            TableauConnecteur T  = new TableauConnecteur(DE.indiceFin() - DE.indiceDebut() + 1);
            int curseurSortie = 1;
            for (String Nom : DE.Noms()) {
                int curseur = 0;
            	boolean trouve = false;
				//System.out.println("Rech de : " + Nom);
                while (!trouve && curseur < SortiesG.size()){
				//System.out.println(" >> Tentative de match : " + SortiesG.get(curseur).getNom());
				if (SortiesG.get(curseur).getNom().equals(Nom)) {
                    SortiesG.remove(curseur);
                    trouve = true;
					//System.out.println("trouvé");
                }
                curseur += 1;
            }
            if (!Dico.existe(Nom)) throw new RuntimeException("Il manque une sortie : " + Nom +". \nVeuillez verifier que ce signal est  et n'est pas déjà généré par le circuit.");
			//System.out.println("On cherche : " + Nom);
				Connecteur CS = Dico.getConnecteur(Nom);
				Composant petitPlus = new EntreeModule(CS);
                T.brancher(CS,curseurSortie);
				//System.out.println("on recupère : " + Dico.getConnecteur(Nom).getNom());
                curseurSortie += 1;
            }
            SortieUtilisateur.add(new StructSortie(DE.Nom(),T));
			System.out.println("\nModule : " + M.Nom + "\nSortie ajouté pour l'utilisateur : " + DE.Nom() + " avec " + DE.nbSignaux() + ".\n");
        }
        SortieModule = SortiesG;
		SortiesG = SortieUtilisateur;

		if(this.ModulesAppeles != null){

			//Reparcourir les simulateur et faire pareil avec les entrées.
			//Verifier et retirer de EntreeG pour verifier que les signaux sont correctemrnt générés.
			//TODO
			for (int indexModule = 0; indexModule < M.Branchements.size(); indexModule ++) {
				AppelModule A = M.Branchements.get(indexModule);
				Simulateur Smodule = ModulesAppeles.get(indexModule);
				//TODO A finir !!!
				for (int curs = 0; curs < A.DS.size();curs ++) {  //On parcours les sorties des modules qui sont des entrées de notre circuit
					Descripteur DE = A.DS.get(curs);
					int curs2 = 0;
					for (Erwan signal : DE.Erwans()) {
						Connecteur CE = null;
						if (!Dico.existe(signal.Nom())) System.out.println("Entrée jamais lue ! : " + signal.Nom());
						else {
							CE = Dico.getConnecteurE(signal);
							if (CE.getOrigine() != null) throw new RuntimeException("Ce signal est lu et généré ! :" + signal.Nom());
							Connecteur CS = Smodule.getSorties(curs+1,curs2+1);
							Composant Csuite = CS.getComposant();
							if ((Csuite == null )|| !(Csuite instanceof EntreeModule) ){
								throw new RuntimeException("Pb de conception, c'est la faute de Mati.");
							}
							EntreeModule Em = (EntreeModule) Csuite;
							TableauConnecteur T = new TableauConnecteur(1);
							T.brancher(CE,1);
							Em.setEntree(new BouttonEntree(new StructEntree(signal.Nom(), T), 1));
							curs2 ++;
							//Chercher et retirer les signaux des listes SignauxParModule et EntreeG(entreeModule) pour determiner les signaux non générés.
						}
					}
				}
            }
		}

		for (Descripteur DE : M.Entrees) {
			TableauConnecteur T  = new TableauConnecteur(DE.indiceFin() - DE.indiceDebut() + 1);
			int curseurEntree = 1;
			for (String Nom : DE.Noms()) {
				int curseur = 0;
				boolean trouve = false;
	 			while (!trouve && curseur < EntreesG.size()){
					if (EntreesG.get(curseur).getNom().equals(Nom)) {
						EntreesG.remove(curseur);
						trouve = true;
					}
					curseur += 1;
				}
				curseur = 0;
				if (SignauxParModule != null) {
					while(!trouve && curseur < SignauxParModule.size()){
						if(SignauxParModule.get(curseur).getNom().equals(Nom)) {
							trouve = true;
							SignauxParModule.remove(curseur);
						}
						curseur += 1;
					}
				}
				if (!trouve) throw new RuntimeException("Il manque une entree : " + Nom +". \nVeuillez verifier que ce signal est lu et n'est pas déjà généré par le circuit.");		
				T.brancher(Dico.getConnecteur(Nom),curseurEntree);
				curseurEntree += 1;
			}
			EntreeUtilisateur.add(new StructEntree(DE.Nom(),T));
			System.out.println("\nModule : " + M.Nom + "\nEntree ajouté pour l'utilisateur : " + DE.Nom() + " avec " + DE.nbSignaux() + ".\n");
		}
		entreeModule = EntreesG;
		EntreesG = EntreeUtilisateur;

		//TODO Appel module !!! TODO
		System.out.print("Il reste ces entree reposant sur des modules : ");
		for (StructEntree SE : entreeModule ){
			System.out.print(SE.getNom() + ", ");
		}
		System.out.println();
		System.out.print("Il reste ces sorties à fournir à des modules : ");
                for (StructSortie SS : SortieModule ){
                        System.out.print(SS.getNom() + ", ");
                }
                System.out.println();
		/*
		for (AppelModule A : M.Branchements) {
			if(A.DE.size() != A.module.Entrees.size()) throw new RuntimeException("Erreur dans un appel module !\n Pb dans le nb d'entrées !");
			if(A.DS.size() != A.module.Sorties.size()) throw new RuntimeException("Erreur dans un appel module !\n Pb dans le nb de sorties !");
			Simulateur Sinterne = new FileSimulateur(A.module);
			ModulesAppeles.add(Sinterne);
			//TODO recup entrees et sorties + Branchements et tt.
			int curseurEntree1 = 1; //Selecteur de l'entrée.
									
			for (Descripteur DE : A.DS) {   //Attention, ici on s'occupe des EntresModules, qui sont des entrés 
							//vers ce circuit depuis un module interne.
							//Il s'agit donc de sortie de ces modules internes.

				int curseurEntree2 = 1; // Selecteur du signal dans l'entrée.
				if ((DE.indiceFin() - DE.indiceDebut() + 1) != Sinterne.nbSlotSortie(curseurEntree1)) {
					throw new RuntimeException("nb de signaux non correspodants en sortie d'un module");
				}
				for (String Nom : DE.Nom()) {
					int curs = 0;
					boolean trouve = false;
					while (!trouve && curs < EntreeModule.size()){
						if (EntreeModule.get(curs).getNom().equals(Nom)) {
							trouve = true;
							StructEntree SE = EntreeModule.remove(curs);
							Connecteur C = Sinterne.getSortie(curseurEntree1,curseurEntree2);
							BouttonEntree B = new BouttonEntree(SE,1);
							Composant EM = new EntreeModule(C,B);
							curseurEntree2 += 1;
						}
						curs += 1;
					}
					if (!trouve) {
						Connecteur C = Sinterne.getSortie(curseurEntree1,curseurEntree2);
						//TODO continuer ici
						//if (DE.unique())
						TableauConnecteur T = new TableauConnecteur(1);
						T.brancher(new Lien(Nom),1);
						StructEntree  SE = new StructEntree(Nom,T)
						BouttonEntree B = new BouttonEntree(SE,1);
						//TODO ca ne marchera pas.
					}
					curseurEntree1 += 1;
				}
			}
		}*/
		/*for (StructEntree SE : EntreesG){
			System.out.println("Entree retenue : " + SE.getNom() + " avec :" + SE.getNombre() + " signaux.");
		}
		for (StructSortie SS : SortiesG){
			System.out.println("Entree retenue : " + SS.getNom() + " avec :" + SS.getNombre() + " signaux.");
		}*/
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
		Set<String> SignauxLues = new HashSet<>();
		Set<String> SignauxGeneres = new HashSet<>();
		for (Erwan S : Plan) {
			if (S.Op != Operation.AFFECTATION) throw new RuntimeException("Pb de structure");
			SignauxGeneres.add(S.Nom());
			//System.out.println("Sortie détectée : " + S.Nom());
			construire(S,SignauxLues);
		}
		/*for (String s : SignauxLues) {
			System.out.println("Le signal " + s + " est lue");
		}
		for (String s : SignauxGeneres) {
            System.out.println("Le signal " + s + " est généré");
        }*/
		Set<String> inter = new HashSet<>(SignauxLues);
		inter.retainAll(SignauxGeneres);
		/*for (String s : inter) {
            System.out.println("Le signal " + s + " est lue et généré");
        }*/
		SignauxLues.removeAll(inter);
		SignauxGeneres.removeAll(inter);
		/*for (String s : SignauxLues) {
            System.out.println("Le signal " + s + " est conservé pour l'entrée");
        }
        for (String s : SignauxGeneres) {
            System.out.println("Le signal " + s + " est conservé pour la sortie");
        }*/
		//TODO 1ere version sans prendre en compte les vecteurs !!!
		//System.out.println("Fin construction ! \nDébut de créations des strctures d'entrées et de sorties !");
		this.EntreesG = new ArrayList<>();
		this.SortiesG = new ArrayList<>();
		for (String nom : SignauxLues) {
			TableauConnecteur T = new TableauConnecteur(1);
			T.brancher(Dico.getConnecteur(nom),1);
			this.EntreesG.add(new StructEntree(nom,T));
		}
		for (String nom : SignauxGeneres) {
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
