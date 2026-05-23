package simulateur.affichage;

import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import simulateur.Simulateur;
import simulateur.scriptTest.SimulationTestException;

/**Tous boutons d'une entrée. */
public class VecteurEntreeSimulateur extends FlowPane{

    private Text texte;

    /**Construit avec un simulateur et un numero d'entree;
     * 
     * @param composant Le simulateur.
     * @param numero Numero d'entree dans le simulateur.
     */
    public VecteurEntreeSimulateur(Simulateur composant, int numero){
        /*Ajout du texte*/
        texte = new Text(composant.nomEntree(numero) + " : ");
        getChildren().add(texte);

        /*Ajout de chaques bits. */
        for (int j = composant.nbSlotEntree(numero); j >= 1; j--){
            getChildren().add(new EntreeSimulateur(composant.getEntrees(numero, j)));
        }
    }

    /**Change la valeur d'une entree.
     * 
     * @param i indice du bit de gauche à droite.
     * @param val
     */
    public void set(int i, boolean val) throws SimulationTestException{
        try {
            ((EntreeSimulateur) getChildren().get(i + 1)).setSelected(val);
        } catch (Exception e){
            throw new SimulationTestException();
        }
    }
}
