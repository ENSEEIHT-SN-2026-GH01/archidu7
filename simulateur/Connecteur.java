package simulateur;

/** Permet de transporter un signal logique entre composants.
 * La principale implémentation de cette interface est la classe Lien.
 *
 * @author Mati Afriat -- Archidu7.
 */
public interface Connecteur {

	/** Etablir la valeu transportée dans le connecteur.
	 * @param b la nouvelle valeur.
	 */
	public void setValeur(Etat b); 

	/** Récuperer la valeur dans le connecteur.
	 * @return la valeur stockée dans le connecteur.
	 */
	public Etat getValeur();

	/** Recuperer le nom du Connecteur.
	 * @return le nom.
	 */
	public String getNom();

	/** Recuperer le composant vers lequel pointe le connecteur.
	 * @return le composant suivant.
	 */
	public Composant getComposant();

	/** Etablir le composant suivant.
	 * @param c le composant.
	 */
        public void setComposant(Composant c)  ;

	/** Recuperer le composant duquel est originaire le connecteur.
	 * @return le composant.
	 */
        public Composant getOrigine();

	/** Etablir le composant duquel est originaire le connecteur.
	 * @param c le composant.
	 */
        public void setOrigine(Composant c)  ; 

	/** Mettre à null le composant suivant.
	 */
	public void unsetComposant();

	/** Mettre à null le composnat origine.
	 */
	public void unsetOrigine();

	/** Recuperer un lien sans composant suivant transoportant le même signal.
	 * Permet nottament de dupliquer un signal grace à un composant adéquat.
	 * @param D un dicoconnecteur contenant les liens déjà existant et permetant d'enrégistrer le nouveau.
	 * @return un connecteur.
	 */
	public Connecteur getSignal(DicoConnecteur D);

}
