import editeur.EditeurTexte;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import sauvegarde.FileStorage;
import sauvegarde.SaveListener;

import java.util.*;
import java.io.*;
import java.security.InvalidParameterException;

public class ListeModulePrincipale extends ScrollPane {

    private List<FichierModuleBouton> modules;
    private VBox liste;
    private EditeurTexte editeur;
    private FileStorage sauveur;
    private BandeauOnglet bandeau;

    public ListeModulePrincipale(EditeurTexte editeur, FileStorage sauveur, BandeauOnglet bandeau) {
        super();
        this.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        //Force le contenu à s'adapter à la largeur quand on redimensionne
        this.setFitToWidth(true);

        this.editeur = editeur;
        this.sauveur = sauveur;
        this.bandeau = bandeau;
        liste = new VBox();
        liste.setFillWidth(true);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setContent(liste);
        modules = new ArrayList<FichierModuleBouton>();

        //chargement de l'environnement
        rechargerEnvironnement();
        sauveur.addListener(new RechargerListener());
    }

    /** Recharge les modules de l'environnement.
     *
     * @param dir Le répertoire courant.
     */
    public void rechargerEnvironnement() {
        /*creation du dossier modules dans l'environnement s'il n'existe pas. */
        new File( sauveur.getChemin() + "/modules/").mkdir();

        String dir = sauveur.getChemin() + "/modules/";
        modules.clear();

        String[] repertoire = (new File(dir)).list();

        for (String nomFichier : repertoire) {
            try {
                modules.add(new FichierModuleBouton(nomFichier, editeur, sauveur, bandeau));
            } catch (InvalidParameterException e) {
                // si ce n'est pas un module shdl, on n'ajoute pas
            }
        }

        rechargerAffichage();
    }

    /** Rétablie l'affichage des boutons */
    private void rechargerAffichage() {
        liste.getChildren().clear();
        liste.getChildren().addAll(modules);
    }

    private class RechargerListener implements SaveListener{
        public void onSave(){
            rechargerEnvironnement();
        }
    }
}