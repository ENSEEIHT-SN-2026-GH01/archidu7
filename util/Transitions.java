package util;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**Utilitaires d'animation de l'interface.
 * Portion non-CSS du thème E (les transitions de visibilité ne sont pas
 * exprimables en CSS JavaFX).
 */
public final class Transitions {

    private Transitions(){}

    /**Fait apparaître un panneau par un fondu entrant de 220 ms.
     *
     * @param panneau Le nœud à faire apparaître.
     */
    public static void apparition(Node panneau){
        FadeTransition fondu = new FadeTransition(Duration.millis(220), panneau);
        fondu.setFromValue(0.0);
        fondu.setToValue(1.0);
        fondu.play();
    }
}
