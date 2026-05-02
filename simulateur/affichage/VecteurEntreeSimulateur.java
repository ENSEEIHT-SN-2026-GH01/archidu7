package simulateur.affichage;

import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import simulateur.Simulateur;

/**Les boutons d'une entrée. */
public class VecteurEntreeSimulateur extends FlowPane{

    private Text texte;

    public VecteurEntreeSimulateur(Simulateur composant, int numero){
        /*Ajout du texte*/
        texte = new Text(composant.nomEntree(numero) + " : ");
        getChildren().add(texte);

        /*Ajout de chaques bits. */
        for (int j = 1; j <= composant.nbSlotEntree(numero); j++){
            getChildren().add(new EntreeSimulateur(composant.getEntrees(numero, j)));
        }
    }
}
