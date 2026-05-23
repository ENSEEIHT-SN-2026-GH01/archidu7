package boutons;

import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import simulateur.affichage.ConfigurationSimulation;

/**Le menu de configuration qui permet de changer la vitesse des horloges. */
public class MenuHorlogeVitesse extends Menu{

    public MenuHorlogeVitesse(){
        super("vitesse des horloges");
        getItems().add(new MenuHorlogeSlider());
    }

    private class MenuHorlogeSlider extends CustomMenuItem{
        public MenuHorlogeSlider(){
            Slider barre = new Slider(100, 1000, 50);
            barre.setShowTickLabels(true);
            barre.setShowTickMarks(true);
            barre.setMajorTickUnit(300);
            barre.setMinorTickCount(5);
            barre.setValue((double) ConfigurationSimulation.periodeHorloge);

            Text indication = new Text("période en ms");

            BorderPane contenue = new BorderPane(null, barre, null, indication, null);
            super(contenue);

            setOnHiding((e) -> {
                ConfigurationSimulation.periodeHorloge = (int) barre.getValue();
            });
        }
    }
}
