package boutons;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import simulateur.affichage.ConfigurationSimulation;

public class MenuTailleSprite extends Menu{
    
    public MenuTailleSprite(){
        super("taille des sprites");

        ToggleGroup groupe = new ToggleGroup();

        RadioMenuItem fois1 = new RadioMenuItem("x1");
        fois1.setOnAction(new ActionTaille1());
        fois1.setToggleGroup(groupe);
        RadioMenuItem fois2 = new RadioMenuItem("x2");
        fois2.setOnAction(new ActionTaille2());
        fois2.setToggleGroup(groupe);
        RadioMenuItem fois3 = new RadioMenuItem("x3");
        fois3.setSelected(true);
        fois3.setOnAction(new ActionTaille3());
        fois3.setToggleGroup(groupe);

        getItems().addAll(fois1, fois2, fois3);
    }

    private class ActionTaille1 implements EventHandler<ActionEvent>{
        public void handle(ActionEvent evt){
            ConfigurationSimulation.chagerTailleSprite(1);
        }
    }

    private class ActionTaille2 implements EventHandler<ActionEvent>{
        public void handle(ActionEvent evt){
            ConfigurationSimulation.chagerTailleSprite(2);
        }
    }

    private class ActionTaille3 implements EventHandler<ActionEvent>{
        public void handle(ActionEvent evt){
            ConfigurationSimulation.chagerTailleSprite(3);
        }
    }
}
