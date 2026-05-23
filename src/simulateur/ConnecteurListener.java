package simulateur;

/**Listener pour un connecteur. 
 * Si vous ne savez plus ce qu'est un listener, revoir le cours.
*/
public interface ConnecteurListener {
    
    /**Appelé lorsque le signal du composant est modifié.
     * 
     * @param e Le nouveau siganl.
     */
    void signalModifie(Etat e);
}
