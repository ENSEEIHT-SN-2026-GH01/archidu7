package simulateur;

/** Facilite la manipulation d'un signal depuis la Simulation.
 * <p> Pour comprendre cette doc il est conseillé mais pas obligatoire d'avoir lu la doc de StructEntree. </p>
 * <p> Un BouttonEntree contient un StructEntree et un numero de signal. 
 * Ainsi lorsque l'on souhaitera modifier un signal en cliquant dessus, il suffira au programme de retrouver le BouttonEntree associé et d'utiliser la méthode set en précisant le nouvel Etat.
 * Cette class est utile pour celui qui gère l'interface graphique de la simulation.
 *
 * @author Mati Afriat -- archidu7.
 */
public class BouttonEntree {

	private StructEntree S;
	private int numero;

	/** Création d'un bouton associé à un signal d'entrée.
	 * @param S L'entrée à laquelle appartient le signal.
	 * @param numero l'indice du signal dans l'entrée.
	 * @return un bouton prêt à l'emploi !!!
	 */
	public BouttonEntree(StructEntree S, int numero) {
		this.S = S;
		this.numero = numero;
	}

	/** Changement de valeur d'un signal.
	 * Permet de modifier la valeur d'un signal et de repercuter les conséquences de cette modife sur l'ensemble du circuit.
	 * @param E le nouvel état pour le signal.
	 */
	public void set(Etat E) {
		S.setValeur(numero,E);
	}
	
}
