package simulateur;
import java.util.*;

/** StructEntree modélise et facilite l'interaction d'une entree.
 * <p> Chaque objet StructEntree représente une seule entree. 
 * Cette classe a été créé pour differentes raisons. 
 * Par exemple pour prendre en compte le fait que certaines entrées plusieurs signaux, les "vecteurs". 
 * De plus cette class a un aspect pratique notamment pour le simulateur : 
 * c'est elle qui se charge de générer les listes d'execution ... </p>
 * <p> Pour etre construit, un StructEntree ne necessite que du nom de l'entree (qui sera afficher dans le fenetre simulation) et d'un TableauConnecteur dans lequel on aura préalablement mis le(s) signal(aux) composant l'entree.
 * ! Attention ! Il faudra créer un tableau connecteur même pour un signal simple ! 
 * (Ou alors il faut faire un nouveau constructeur qui s'en charge ...).
 * Toujours est il qu'il est IMPERATIF que le circuit soit généré dans son intégralité avant de générer le StructEntree !
 * En effet celui-ci, lors de sa création, se charge automatiquement de créer les listes d'executions selon la topologie déjà présente.
 * Un futur viendra ou l'on pourra refaire cette initialisation après construction, surtout dans un cas de modification graphique, mais on en est encore bien loin... </p>
 * <p> Une liste d'execution c'est quoi ? <br>
 * Un des pricipales enjeux d'une simulation ... c'est de simuler. 
 * Dans notre cas, la simulation est assurée par des objets qui calculent des états en fonction des entrees et qui les activent en sorties et d'autres qui les transportent jusqu'à l'objet suivant. 
 * Le problème pricipal est pouvoir mettre à jour l'état d'un objet qui calcul quand une de ses entrées change.
 * Une solution serait de faire une boucle qui fait recalculer l'état de tous les objet de manière cyclique, mais c'est loin d'être idéal.
 * On a retenu l'idée de l'entree qui prévient l'objet. 
 * Et puisque la "propagation" des recalcul est toujours la même, on a décider de ne la calculer qu'une seule fois.
 * Puisque les modifications se propagent à partir des entrées, on peut stocker une liste contenant l'ensemble des objet à mettre à jour, que l'on consultera quand cette entrée à été modifiée. 
 * Le mot entrée de juste avant étant au sens signal, dans un StructEntree, il y aura autant de liste d'execution que de signaux.</p>
 *
 * @author Mati Afriat -- archidu7.
 */ 
public class StructEntree {

	private String nom;
	private TableauConnecteur T;
	private Map<Integer,List<Composant>> D;

	/** Création d'un StructEntree.
	 * Comme indiqué en introduction, il ne nécessite que de son nom qui sera afficher et d'un TableauConnecteur contenant ... des Connecteur.
	 * Ce Constructeur se charge également de générer les listes d'execution (voir intro).
	 * Il faut que le circuit soit déjà généré quand on y fait appel.
	 * @param nom C'est le nom qui sera affiché dans le simulateur.
	 * @param T C'est le TableauConnecteur qui contient tous les Connecteur des signaux présent dans cette entree.
	 * @return Le StructEntree, comme prévu.
	 */
	public StructEntree(String nom, TableauConnecteur T)  {
		this.nom = nom;
		this.T = T;
		this.D = new HashMap<>();
		for (int i = 1; i <= T.getTaille(); i++) {
			initialiserListe(i);
		}
	}

	private void initialiserListe(int j)  { //TODO Attention ! ne converge pas en cas de rebouclage !!!
		//System.out.println("j'ai été appelé !");
		Connecteur C = T.getConnecteur(j);
		List<Composant> Suivant = new ArrayList<>();
		List<Composant> Calcul = new ArrayList<>();
		Map<Composant,Integer> DicoCompo = new HashMap<>();
		C.getComposant().ajouter(Suivant);
		DicoCompo.put(C.getComposant(),1);
		//int iteration = 0;
		while (!Suivant.isEmpty() /*&& iteration < 10*/) {
			List<Composant> Courant = Suivant;
			Suivant = new ArrayList<>();
			for(Composant Com : Courant) {
				for (int i = 1; i <= Com.getNbSortie(); i++) {
					if (Com.getConnecteurSortie(i) != null &&  Com.getConnecteurSortie(i).getComposant() != null && !(Com instanceof Multiplicateur)) {
						Composant CI = Com.getConnecteurSortie(i).getComposant();
						if (DicoCompo.get(CI) == null) {
							CI.ajouter(Suivant);
							DicoCompo.put(CI,1);
						} else {
							if (DicoCompo.get(CI) < 3) {
								DicoCompo.put(CI,DicoCompo.get(CI)+1);
								CI.ajouter(Suivant);
							}
						}
					}
				}
			}
			Calcul.addAll(Courant);
			//iteration ++;
		}
		System.out.println("Début liste Calcul " + this.nom);
		for (Composant Compo : Calcul) {
			System.out.print("Type : " + Compo.getNom() + ",Nombre de Sorties : " + Compo.getNbSortie() + ", Sorties : ");
			for (int ii = 1; ii <= Compo.getNbSortie() ; ii++) {
				System.out.print(Compo.getConnecteurSortie(ii).getNom() + ", ");
			}
			System.out.println();
		}
		System.out.println("Fin liste Calcul");
		D.put(j,Calcul);
	}

	private static void calculer(List<Composant> L)  {
		for (Composant C : L) {
			C.calculer();
		}
	}

	/** Permet de récuperer le nom de l'entree.
	 * Pratique, notamment pour celui qui fait la Simulation.
	 * @return le nom de l'entrée.
	 */
	public String getNom() {
		return nom;
	}

	public String getNom(int i) {
		return T.getConnecteur(i).getNom();
	}

	/** Nombre de signaux dans l'entree.
	 * @return le nombre de signaux dans l'entree.
	 */
	public int getNombre(){
		return T.getTaille();
	}

	/** Obtenir l'état d'un signal.
	 * Cette methode a le mérite d'exister, on la remercie pour cette effort mais ne sera probalement pas utilisée.
	 * @param i l'indice du signal dans l'entree dont on veut connaitre l'état.
	 * @return l'état du signal selectionné.
	 */ 
	public Etat getValeur(int i)  {
		return T.get(i);
	}

	/** Obtenir le connecteur d'un signal dans l'entrée.
	 * Usage déconseillé car il pourrait mener au mauvais fonctionnement de l'ensemble mais peut être utile pour les test et les debug.
	 * @param i l'indice du signal dont on veut récuperer le Connecteur.
	 * @return le Connecteur du signal selectionnée.
	 */
	public Connecteur getConnecteur(int i)  {
		return T.getConnecteur(i);
	}

	/** Mettre à jour la valeur d'un signal.
	 * Cette méthode fait appel à une méthode calculer qui permet de repercuter les modifications dans l'ensemble du circuit.
	 * Il est conseillé de passer par cette méthode.
	 * C'est cette methode qui est utilisé dans BouttonEntree.
	 * @param i l'indice du signal dont on souhaite modifier l'état.
	 * @param e l'état dans lequel on souhaite que le signal soit modifié.
	 */
	public void setValeur(int i, Etat e)  {
		T.set(i,e);
		calculer(D.get(i));
	}
}
