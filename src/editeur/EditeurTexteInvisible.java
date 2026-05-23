package editeur;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import editeur.coloration.Palette;

/**
 * Composant personnalisé représentant la zone d'édition de code.
 * Hérite de TextArea pour bénéficier de toutes les fonctionnalités natives d'édition de texte.
 */
public class EditeurTexteInvisible extends TextArea {

    public EditeurTexteInvisible(int fontSize) {
        // 1. Appel du constructeur de la classe parente (TextArea)
        super();

        // 2. Configuration d'un texte d'invite (optionnel mais ergonomique)
        this.setPromptText("Saisissez votre code SHDL ici...");

        // 3. Configuration de la police d'écriture
        // Police à chasse fixe : on utilise EXACTEMENT le même nom que la couche
        // colorée (TextDecoupable -> "monospace") pour que les deux couches
        // résolvent vers la même police physique sur une machine donnée.
        // "Monospaced" (logique JavaFX) et "monospace" (générique fontconfig)
        // peuvent résoudre vers des polices DIFFÉRENTES sous Linux ("Monospaced"
        // -> Noto Sans proportionnelle), d'où un désalignement cumulatif des deux
        // couches qui s'aggrave ligne par ligne (effet fantôme).
        this.setFont(Font.font("monospace", fontSize));

        // 4. Désactivation du retour à la ligne automatique
        // En programmation, on préfère généralement avoir une barre de défilement 
        // horizontale plutôt que des lignes de code coupées arbitrairement.
        //this.setWrapText(false);

        getStyleClass().add("editeur-invisible");

        // Couleur du texte de saisie estompé : gérée en Java (pas en CSS) pour
        // suivre le mode clair/sombre via Palette, comme la coloration syntaxique.
        appliquerTheme();
    }

    /** (Ré)applique la couleur du texte estompé selon le mode clair/sombre courant. */
    public void appliquerTheme(){
        String couleur = Palette.estModeSombre ? "rgba(255,255,255,0.32)" : "rgba(0,0,0,0.3)";
        setStyle("-fx-text-fill: " + couleur + ";");
    }
}