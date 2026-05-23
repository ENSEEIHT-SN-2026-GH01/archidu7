package simulateur;

import java.util.ArrayList;

public class Lien implements Connecteur {

	private Etat etat;
	private String nom;
	private Composant origine, composantSuivant;

	private ArrayList<ConnecteurListener> listeners;

	public Lien(String nom) {
		etat = Etat.ND;
		this.nom = nom;
		composantSuivant = null;
		origine = null;
		listeners = new ArrayList<>();
	}

	public void setValeur(Etat b, Propageur prop) {
		//System.out.println("recalcule de :" + nom + "val: " + etat + "->" + b);
		boolean changed = (b != etat);
		etat = b;

		/*notifier les listeners */
		for (ConnecteurListener connecteurListener : listeners) {
			connecteurListener.signalModifie(b);
		}
		if ((b.getValeur() == 1) && this.composantSuivant != null && this.composantSuivant instanceof EntreeModule) {
			EntreeModule E = (EntreeModule) this.composantSuivant;
			if(E.B instanceof BouttonEvenement){
				BouttonEvenement B = (BouttonEvenement) E.B;
				B.sauv();
			}
		}

		if (changed && composantSuivant != null) composantSuivant.calculer(prop);
	}

	public Etat getValeur() {
		return etat;
	}

	public String getNom() {
		return new String(nom);
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public Composant getComposant(){
		return composantSuivant;
	}

	public void setComposant(Composant c)  {
		if (composantSuivant != null) {
			composantSuivant.debrancherEntree(this);
		}
		composantSuivant = c;
	}

	public void unsetComposant() {
                composantSuivant = null;
        }

	public Composant getOrigine(){
                return origine;
        }

        public void setOrigine(Composant c) {
		if (origine != null) {
			origine.debrancherSortie(this);
		}
                origine = c;
        }

	public void unsetOrigine() {
		origine = null;
	}

	public String NomNouveau(DicoConnecteur D) {
		int i = 1;
		while (D.existe(this.getNom() + " - " + i)) {
			i += 1;
		}
		return new String(this.getNom() + " - " + i);
	}


	public Connecteur getSignal(DicoConnecteur D) {
		if (this.composantSuivant == null) {
			return this;
		} else {
			if (this.composantSuivant instanceof Multiplicateur) {
				Multiplicateur M = (Multiplicateur) this.composantSuivant;
				String S = this.NomNouveau(D);
				Connecteur C = new Lien(S);
				M.ajouter(C);
				D.ajouter(C,S);
				return C;
			} else {
				Composant CS = this.composantSuivant;
				int i = CS.debrancherEntree(this);
				String S1 = new String(this.getNom() + " - " + 1);
				String S2 = new String(this.getNom() + " - " + 2);
				Connecteur C1 = new Lien(S1);
				Connecteur C2 = new Lien(S2);
				Multiplicateur M = new Multiplicateur(this, C1, C2);
				D.ajouter(C1,S1);
				D.ajouter(C2,S2);
			       	CS.brancherEntree(C1,i);
				return C2;
			}
		}
	}	

	public void addListener(ConnecteurListener listener){
		listeners.add(listener);
	}
}
