package simulateur.affichage;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import simulateur.Simulateur;

/**Panneau de simulation : vecteurs d'entrée et de sortie d'un Simulateur,
 * destiné à être inséré dans la fenêtre principale (zone bottom du BorderPane).
 *
 * Le contenu est placé dans un {@link ScrollPane} plafonné à {@link #HAUTEUR_MAX} :
 * tant que les entrées/sorties tiennent dans cette hauteur le panneau s'ajuste au
 * contenu, au-delà une barre de défilement verticale apparaît.
 */
public class PanneauSimulateur extends VBox {

    /** Hauteur maximale (px) de la zone de simulation avant défilement. */
    private static final double HAUTEUR_MAX = 220;

    /**Construit le panneau à partir d'un simulateur déjà construit.
     *
     * @param sim Le simulateur.
     */
    public PanneauSimulateur(Simulateur sim){
        getStyleClass().add("panneau-simulation");

        VBox contenu = new VBox();
        contenu.getStyleClass().add("contenu-simulation");

        /* entrées */
        for (int i = 1; i <= sim.nbEntree(); i++){
            contenu.getChildren().add(new VecteurEntreeSimulateur(sim, i));
        }

        /* séparateur entre les entrées et les sorties */
        contenu.getChildren().add(new Text("---------------------"));

        /* sorties */
        for (int i = 1; i <= sim.nbSorties(); i++){
            contenu.getChildren().add(new VecteurSortieSimulateur(sim, i));
        }

        ScrollPane defilement = new ScrollPane(contenu);
        defilement.getStyleClass().add("simulation-scroll");
        defilement.setFitToWidth(true);
        defilement.setMaxHeight(HAUTEUR_MAX);
        defilement.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        defilement.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        getChildren().add(defilement);
    }
}
