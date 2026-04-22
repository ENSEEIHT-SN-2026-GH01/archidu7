import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import java.util.*;
import java.io.*;
import java.security.InvalidParameterException;

public class ListeModulePrincipale extends ScrollPane{
    
    private List<FichierModuleBouton> modules;
    private VBox liste;

    public ListeModulePrincipale(){
        super();
        liste = new VBox();
        liste.setPrefWidth(FichierModuleBouton.moduleBoutonLargeur);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setContent(liste);
        modules = new ArrayList<FichierModuleBouton>();
        rechargerEnvironnement("./modules/");
    }

    /**Recharge les modules de l'environnement.
     * 
     * @param dir Le repertoire courant.
     */
    public void rechargerEnvironnement(String dir){
        modules.clear();

        String[] repertoire = (new File(dir)).list();

        for (String nomFichier : repertoire) {
            try{
                modules.add(new FichierModuleBouton(nomFichier));
            }
            catch (InvalidParameterException e){
                //si ce n'est pas un module shdl, on n'ajoute pas
            }
        }

        rechargerAffichage();
    }

    /**Retablie l'affichage des boutons */
    private void rechargerAffichage(){
        liste.getChildren().clear();
        liste.getChildren().addAll(modules);
    }
}
