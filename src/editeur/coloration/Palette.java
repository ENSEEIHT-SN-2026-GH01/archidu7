package editeur.coloration;

import javafx.scene.paint.Color;

public class Palette {
    public static boolean estModeSombre = false;

    // Couleurs "Adaptées" : si sombre, on renvoie une version lumineuse
    public static Color adapter(Color claire, Color sombre) {
        return estModeSombre ? sombre : claire;
    }
}