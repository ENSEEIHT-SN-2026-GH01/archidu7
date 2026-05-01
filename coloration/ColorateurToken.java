package coloration;
import editeur.EditeurTexte;
import javafx.scene.paint.*;

public interface ColorateurToken {
    
    /**
     * La couleur du token associé.
     * @return
     */
    Color getCouleur();

    /**
     * Colorie le token associé.
     * @param boite La boite de texte à colorier.
     */
    void appliqueCouleur(EditeurTexte editeur);
}
