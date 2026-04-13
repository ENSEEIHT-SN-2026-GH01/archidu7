package simulateur;
import java.util.*;

public class StructEntree {

	private String nom;
	private TableauConnecteur T;
	private Map<Integer,List<Composant>> D;

	public StructEntree(String nom, TableauConnecteur T)  {
		this.nom = nom;
		this.T = T;
		this.D = new HashMap<>();
		for (int i = 1; i <= T.getTaille(); i++) {
			initialiserListe(i);
		}
	}

	private void initialiserListe(int j)  { //TODO Attention ! ne converge pas en cas de rebouclage !!!
		Connecteur C = T.getConnecteur(j);
		List<Composant> Suivant = new ArrayList<>();
		List<Composant> Calcul = new ArrayList<>();
		C.getComposant().ajouter(Suivant);
		while (!Suivant.isEmpty()) {
			List<Composant> Courant = Suivant;
			Suivant = new ArrayList<>();
			for(Composant Com : Courant) {
				for (int i = 1; i <= Com.getNbSortie(); i++) {
					if (Com.getConnecteurSortie(i) != null &&  Com.getConnecteurSortie(i).getComposant() != null) {
						Com.getConnecteurSortie(i).getComposant().ajouter(Suivant);
					}
				}
			}
			Calcul.addAll(Courant);
		}
		D.put(j,Calcul);
	}

	private static void calculer(List<Composant> L)  {
		for (Composant C : L) {
			C.calculer();
		}
	}

	public String getNom() {
		return nom;
	}

	public int getNombre(){
		return T.getTaille();
	}

	public Etat getValeur(int i)  {
		return T.get(i);
	}

	public Connecteur getConnecteur(int i)  {
		return T.getConnecteur(i);
	}

	public void setValeur(int i, Etat e)  {
		T.set(i,e);
		calculer(D.get(i));
	}
}
