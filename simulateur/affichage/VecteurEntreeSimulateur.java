package simulateur.affichage;

import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import simulateur.BouttonEntree;
import simulateur.StructEntree;

/**Les boutons d'une entrée. */
public class VecteurEntreeSimulateur extends FlowPane{

    private Text nom;

    public VecteurEntreeSimulateur(StructEntree composant){
        /*Ajout du texte*/
        nom = new Text(composant.getNom() + " : ");
        getChildren().add(nom);

        /*Ajout de chaques bits. */
        for (int i = 1; i <= composant.getNombre(); i++){
            getChildren().add(new EntreeSimulateur(new BouttonEntree(composant, i)));
        }
    }
}
