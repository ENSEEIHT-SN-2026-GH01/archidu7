package editeur;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**Un text pouvant changer de couleur sun un morceau de celui-çi sans changer
 * la couleur du reste du texte.
 */
public class TextMultiColoriable extends TextDecoupable{
    
    public TextMultiColoriable(int fontSize){
        super(fontSize);
    }

    public TextMultiColoriable(String txt, int fontSize){
        super(txt, fontSize);
    }

    /**Colorie le morceau de texte entre les deux indices (inclus).
     * 
     * @param debut
     * @param fin
     * @param couleur La couleur à associer.
     */
    public void colorier(int debut, int fin, Color couleur){
        //recupperation du morceau
        decoupeMorceau(debut, fin);

        //coloration
        Text aColorier = cache.getMorceau(); //cache contient la cellule qui vient d'être découpé
        aColorier.setFill(couleur);
    }

    /**Applique une classe de style CSS au morceau de texte entre les
     * deux indices (inclus). La classe "text" est conservée pour garder
     * le style de base du nœud Text.
     *
     * @param debut
     * @param fin
     * @param classeCss Nom de la styleClass définie dans theme-e.css.
     */
    public void colorier(int debut, int fin, String classeCss){
        decoupeMorceau(debut, fin);
        Text aColorier = cache.getMorceau();
        aColorier.getStyleClass().setAll("text", classeCss);
    }
}
