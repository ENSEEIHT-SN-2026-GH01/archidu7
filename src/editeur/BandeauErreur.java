package editeur;

import javafx.scene.control.Label;

/**Bandeau qui affiche les erreurs.
 * 
 */
public class BandeauErreur extends Label{


    public BandeauErreur(){
        setVisible(false);
        setStyle(
            "-fx-background-color: rgb(255, 80, 80);" + 
            "-fx-border-color: rgb(0,0,0);" + 
            "-fx-border-size: 30px;" +
            "-fx-border-radius: 5px;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: 700;" + 
            "-fx-text-fill: rgb(255,255,255);"
        );
    }    

    /**Affiche une erreur.
     * 
     * @param err L'erreur.
     */
    public void showErreur(String err){
        setText(err);
        setVisible(true);
    }
}
