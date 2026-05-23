package simulateur.affichage;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import simulateur.Simulateur;
import simulateur.scriptTest.SimulationTestEchecException;
import simulateur.scriptTest.SimulationTestException;

/**Fenêtre du simulateur.
 * Peut-être instencié avec n'importe quel Simulateur.
 */
public class FenetreSimulateur extends Scene{

    private Map<String,VecteurEntreeSimulateur> entrees = new HashMap<>();
    private Map<String,VecteurSortieSimulateur> sorties = new HashMap<>();

    /**Instencie la fenêtre à partir d'un simulateur.
     * 
     * @param sim Le simulateur déjà construit.
     */
    public FenetreSimulateur(Simulateur sim){
        VBox vecteurs = new VBox();
        super(vecteurs);

        /*entrees */
        for (int i = 1; i<=sim.nbEntree(); i++){
            VecteurEntreeSimulateur nouv = new VecteurEntreeSimulateur(sim, i);
            vecteurs.getChildren().add(nouv);
            entrees.put(sim.nomEntree(i), nouv);
        }

        /*separateur entre les entrees et les sorties. */
        vecteurs.getChildren().add(new Text("---------------------"));

        /*sorties */
        for (int i = 1; i<=sim.nbSorties(); i++){
            VecteurSortieSimulateur nouv = new VecteurSortieSimulateur(sim, i);
            vecteurs.getChildren().add(nouv);
            sorties.put(sim.nomSortie(i), nouv);
        }
    }

    /**Met une entree à val.
     * 
     * @param vecteur
     * @param i
     * @param val
     */
    public void set(String vecteur, int i, boolean val) throws SimulationTestException{
        VecteurEntreeSimulateur vect = entrees.get(vecteur);
        if (vect == null){
            throw new SimulationTestException();
        }
        else{
            vect.set(i, val);
       }
    }

    /**Verifie la valeur d'une sortie.
     * 
     * @param vecteur
     * @param i
     * @param val
     * @throws SimulationTestException problème sur l'appel.
     * @throws SimulationTestEchecException le check est faux.
     */
    public void check(String vecteur, int i, boolean val) throws SimulationTestException, SimulationTestEchecException{
        VecteurSortieSimulateur vect = sorties.get(vecteur);
        if (vect == null){
            throw new SimulationTestException();
        }
        else {
            vect.check(i, val);
        }
    }
}
