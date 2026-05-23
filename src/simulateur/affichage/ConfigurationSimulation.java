package simulateur.affichage;

import javafx.scene.image.Image;

/**Les variables de configuration lié à la simulation.
 * 
 */
public class ConfigurationSimulation {
    public static long periodeHorloge = 200;
    public static int agrandissement_sprites = 3;

    public static int largeurSprite = EntreeSimulateur.largeurSpriteBase * agrandissement_sprites;
    public static int hauteurSprite = EntreeSimulateur.hauteurSpriteBase * agrandissement_sprites;

    public static void chagerTailleSprite(int nouvelle){
        agrandissement_sprites = nouvelle;
        largeurSprite = EntreeSimulateur.largeurSpriteBase * agrandissement_sprites;
        hauteurSprite = EntreeSimulateur.hauteurSpriteBase * agrandissement_sprites;

        EntreeSimulateur.imageUndef = new Image("assets/interupteur_undef.png", ConfigurationSimulation.largeurSprite, ConfigurationSimulation.hauteurSprite, true, false);
        EntreeSimulateur.imageOff = new Image("assets/interupteur_off.png", ConfigurationSimulation.largeurSprite, ConfigurationSimulation.hauteurSprite, true, false);
        EntreeSimulateur.imageOn = new Image("assets/interupteur_on.png", ConfigurationSimulation.largeurSprite, ConfigurationSimulation.hauteurSprite, true, false);
        EntreeSimulateur.horlogeOff = new Image("assets/horloge_off.png", ConfigurationSimulation.largeurSprite, ConfigurationSimulation.hauteurSprite, true, false);
        EntreeSimulateur.horlogeOn = new Image("assets/horloge_on.png", ConfigurationSimulation.largeurSprite, ConfigurationSimulation.hauteurSprite, true, false);

        SortieSimulateur.imageUndef = new Image("assets/sortie_undef.png", ConfigurationSimulation.largeurSprite, ConfigurationSimulation.hauteurSprite, true, false);
        SortieSimulateur.imageOff = new Image("assets/sortie_off.png", ConfigurationSimulation.largeurSprite, ConfigurationSimulation.hauteurSprite, true, false);
        SortieSimulateur.imageOn = new Image("assets/sortie_on.png", ConfigurationSimulation.largeurSprite, ConfigurationSimulation.hauteurSprite, true, false);
    }
}
