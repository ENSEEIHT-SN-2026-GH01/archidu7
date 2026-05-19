package simulateur.affichage;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import simulateur.Simulateur;

/**Panneau de simulation : vecteurs d'entrée et de sortie d'un Simulateur,
 * destiné à être inséré dans la fenêtre principale (zone bottom du BorderPane).
 */
public class PanneauSimulateur extends VBox {

    /**Construit le panneau à partir d'un simulateur déjà construit.
     *
     * @param sim Le simulateur.
     */
    public PanneauSimulateur(Simulateur sim){
        getStyleClass().add("panneau-simulation");

        /* entrées */
        for (int i = 1; i <= sim.nbEntree(); i++){
            getChildren().add(new VecteurEntreeSimulateur(sim, i));
        }

        /* séparateur entre les entrées et les sorties */
        getChildren().add(new Text("---------------------"));

        /* sorties */
        for (int i = 1; i <= sim.nbSorties(); i++){
            getChildren().add(new VecteurSortieSimulateur(sim, i));
        }
    }
}
