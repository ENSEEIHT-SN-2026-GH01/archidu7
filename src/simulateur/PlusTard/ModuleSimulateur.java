package simulateur.PlusTard;

import java.util.*;

import simulateur.*;
import simulateur.erwan.*;

public class ModuleSimulateur implements Simulateur {

	private List<StructEntree> EntreesG;
	private List<StructSortie> SortiesG;
	private DicoConnecteur Dico;
	private List<Simulateur> ModulesAppeles;

	public ModuleSimulateur(simulateur.erwan.Module M) {

		construction(M.Plan);

		// Vérification des appels Modules
		if (M.Branchements != null) {
			ModulesAppeles = new ArrayList<>();
			for (AppelModule A : M.Branchements) {
				// Vérification des formats entrées et sorties
				if (A.DE.size() != A.module.Entrees.size())
					throw new RuntimeException("Pas le bon nombre d'entrées durant un appel module");
				if (A.DS.size() != A.module.Sorties.size())
					throw new RuntimeException("Pas le bon nombre de sortie lors d'un apppel module");
				// Il reste encore la vérification du nombre de signaux par entrée/sortie TODO.
				for (int curs = 0; curs < A.DE.size(); curs++) {
					Descripteur De1 = A.DE.get(curs);
					Descripteur De2 = A.module.Entrees.get(curs);
					if (De1.nbSignaux() != De2.nbSignaux())
						throw new RuntimeException("pas le bon nombre de signaux pour l'entree :" + De2.Nom() + ".");
				}
				for (int curs = 0; curs < A.DS.size(); curs++) {
					Descripteur De1 = A.DS.get(curs);
					Descripteur De2 = A.module.Sorties.get(curs);
					if (De1.nbSignaux() != De2.nbSignaux())
						throw new RuntimeException("pas le bon nombre de signaux pour la sortie :" + De2.Nom() + ".");
				}
				// On s'est assuré de la bonne formation de l'appel.
			}
		}

		// Construction des signaux entrant des appels Modules

	}

	public int nbEntree() {
		return EntreesG.size();
	}

	public int nbSorties() {
		return SortiesG.size();
	}

	public String nomEntree(int i) {
		return EntreesG.get(i - 1).getNom(); // TODO
	}

	public String nomSortie(int i) {
		return SortiesG.get(i - 1).getNom(); // TODO
	}

	public int nbSlotEntree(int i) {
		return EntreesG.get(i - 1).getNombre();// TODO
	}

	public int nbSlotSortie(int i) {
		return SortiesG.get(i - 1).getNombre(); // TODO
	}

	public BouttonEntree getEntrees(int i, int j) {
		return new BouttonEntree(EntreesG.get(i - 1), j); // TODO
	}

	public Connecteur getSorties(int i, int j) {
		return SortiesG.get(i - 1).getConnecteur(j); // TODO
	}

	// TODO ------------- ATTENTION ZONE CHANTIER !!! ------------- TODO//
	// //
	// TODO ---- INTERDITE A TOUTE PERSONNE NON AUTHORISEE !!! ---- TODO//
	// //
	// TODO ------------ PORT DU CASQUE OBLIGATOIRE : ------------ TODO//
	// //
	// TODO PETAGE DE CABLE ET CHUTE DE NEURONNES PREVUS A LA METEO TODO//

	private void construction(List<Erwan> Plan) {
		Set<String> NomEntrees = new HashSet<>();
		Set<String> NomSorties = new HashSet<>();
		for (Erwan S : Plan) {
			if (S.Op != Operation.AFFECTATION)
				throw new RuntimeException("Pb de structure");
			NomSorties.add(S.Nom());
			// System.out.println("Sortie détectée : " + S.Nom());
			construire(S, NomEntrees);
		}

		Set<String> inter = new HashSet<>(NomEntrees);
		inter.retainAll(NomSorties);
		NomEntrees.removeAll(inter);
		NomSorties.removeAll(inter);
		this.EntreesG = new ArrayList<>();
		this.SortiesG = new ArrayList<>();
		for (String nom : NomEntrees) {
			TableauConnecteur T = new TableauConnecteur(1);
			T.brancher(Dico.getConnecteur(nom), 1);
			this.EntreesG.add(new StructEntree(nom, T));
		}
		for (String nom : NomSorties) {
			TableauConnecteur T = new TableauConnecteur(1);
			T.brancher(Dico.getConnecteur(nom), 1);
			this.SortiesG.add(new StructSortie(nom, T));
		}
	}

	private void construire(Erwan Signal, Set<String> E) {
		// if (Dico.existe(Signal.Nom())) throw new RuntimeException("Signal déjà
		// attribué :" + Signal.Nom());
		// System.out.println(" >>> Création du signal <<< : " + Signal.Nom());
		Connecteur Sig = recupSignal(Signal.Entrees.get(0), E);
		// TODO à finir
		Connecteur SN = Dico.getConnecteurE(Signal); // <-- modif ici TODO
		Multiplicateur M;
		if (Sig.getComposant() == null) {
			M = new Multiplicateur(Sig, SN);
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
		Dico.ajouter(SN, Signal.Nom());
	}

	private Connecteur recupSignal(Erwan S, Set<String> E) {
		if (Dico.existe(S.Nom())) {
			// System.out.println("Lien déjà Connu : " + S.Nom());
			if (S.Op == Operation.LITTERAL)
				E.add(S.Nom());
			/*
			 * else {
			 * if (Dico.getConnecteur//TODO(S.Nom()).getOrigine() == null) {
			 * System.out.println(S.Nom() + " < INVISIBLE");
			 * }
			 * }
			 */
			return Dico.getConnecteurE(S).getSignal(Dico); // <-- modif ici TODO
		} else {
			// System.out.println("Lien pas connu : " + S.Nom());
			switch (S.Op) {
				case Operation.LITTERAL:
					E.add(S.Nom());
					// System.out.println("Entree détectée : " + S.Nom());
					return Dico.getConnecteurE(S); // <-- modif ici TODO
				case Operation.NOT:
					Connecteur Entree = recupSignal(S.Entrees.get(0), E);
					Connecteur Sortie = Dico.getConnecteurE(S); // <-- midif ici TODO
					// System.out.println("NOT détectée : " + S.Nom());
					Composant N = new Not(Entree, Sortie);
					return Sortie;
				case Operation.AND:
					// System.out.println("AND détectée : " + S.Nom());
					List<Connecteur> LAEntrees = new ArrayList<>();
					Connecteur Sortie1 = Dico.getConnecteurE(S); // <-- mofif ici TODO
					for (Erwan e : S.Entrees) {
						LAEntrees.add(recupSignal(e, E));
					}
					Composant A = new And(LAEntrees, Sortie1);
					return Sortie1;
				case Operation.OR:
					// System.out.println("OR détectée : " + S.Nom());
					List<Connecteur> LEntrees = new ArrayList<>();
					Connecteur Sortie2 = Dico.getConnecteurE(S); // <- mofi ici TODO
					for (Erwan e : S.Entrees) {
						LEntrees.add(recupSignal(e, E));
					}
					Composant O = new Or(LEntrees, Sortie2);
					return Sortie2;
				default:
					throw new RuntimeException("Pb de création dans le Erwan : " + S.Nom());
			}
		}
	}

	/*
	 * private Connecteur getSignal(){
	 * return null;
	 * }
	 * 
	 * private void AND() {
	 * }
	 * 
	 * private void OR() {
	 * }
	 * 
	 * private void NOT() {
	 * }
	 * 
	 * private void RANGE() { // Correspond a plusieur assignation avec des
	 * liensVecteurs. Peux/doit appeller LITTERAL/CONSTANTE
	 * }
	 * 
	 * private void LITTERAL() { //Permet de faire reference à une entree, ou un
	 * signal généré et nommé par l'utilisateur
	 * }
	 * 
	 * private void CONSTANTE() {
	 * }
	 * 
	 * private void MODULE() {
	 * }
	 */
}
