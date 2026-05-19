package editeur.coloration;
import editeur.EditeurTexte;

public interface ColorateurToken {

    /**
     * Colorie le token associé.
     * @param boite La boite de texte à colorier.
     */
    void appliqueCouleur(EditeurTexte editeur);
}
