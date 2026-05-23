import editeur.EditeurTexte;
import editeur.coloration.GestionnaireColorateur;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import parser.lexer.Lexer;
import sauvegarde.FileStorage;
import sauvegarde.TextFileStorage;
import boutons.BoutonsPrincipale;
import javafx.scene.control.SplitPane;

public class FenetrePrincipale extends Scene {
    
    private GestionnaireColorateur colorateur;
    private Lexer lexer;
    private BorderPane outils;
    private ListeModulePrincipale environnement;
    private EditeurTexte editeur;
    private BoutonsPrincipale boutons;
    private MenuPrincipale menu;

    public FenetrePrincipale() { 

        super(new BorderPane(), 1000, 600);
        BorderPane root = (BorderPane) this.getRoot();

        this.editeur = new EditeurTexte();

        FileStorage stockage = new TextFileStorage(); 

        this.boutons = new BoutonsPrincipale(editeur, stockage);
        boutons.setStyle("-fx-background-color: transparent;"); //on espace un peu les bouton

        this.environnement = new ListeModulePrincipale(editeur, stockage);
        
        this.menu = new MenuPrincipale();
        this.menu.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");

        this.menu.getModeSombre().setOnAction(event -> {
            boolean estSombre = this.menu.getModeSombre().isSelected();
            appliquerTheme(estSombre); 
        });

        this.outils = new BorderPane();
        outils.setLeft(this.menu);
        outils.setRight(this.boutons);

        BorderPane.setAlignment(this.menu, javafx.geometry.Pos.CENTER_LEFT);
        BorderPane.setAlignment(this.boutons, javafx.geometry.Pos.CENTER_RIGHT);

        root.setCenter(editeur);
        root.setTop(outils);
        root.setLeft(environnement);

        colorateur = new GestionnaireColorateur(editeur);
        lexer = new Lexer();
        SplitPane espaceDeTravail = new SplitPane();
        espaceDeTravail.getItems().addAll(environnement, editeur);
        espaceDeTravail.setDividerPositions(0.2f);
        espaceDeTravail.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        root.setCenter(espaceDeTravail);
        this.editeur.addListener(new EditeurListener());

        this.appliquerTheme(false);
    }

    public class EditeurListener implements ChangeListener<String>{
        public void changed(ObservableValue<? extends String> unused, String old, String nouvelle){
            colorateur.gerrerAll(lexer.tokenize(nouvelle));
        }
    }

    public void appliquerTheme(boolean sombre) {
        BorderPane root = (BorderPane) this.getRoot();
        // THEME SOMBRE
        if(sombre) {
            root.setStyle("-fx-base: #252526; -fx-background: #252526; -fx-selection-bar: #404040;");
        //bandeau supérieur
            this.outils.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 0 0 1px 0; -fx-padding: 4px 10px;");
            //bandeau de gauche
            this.environnement.setStyle("-fx-background-color: #252526; -fx-border-color: #404040; -fx-border-width: 0 1px 0 0;");
            //éditeur
            this.editeur.setStyle(
                    "-fx-control-inner-background: #1e1e1e; " + 
                    "-fx-text-fill: #d4d4d4; " + // Texte en gris clair
                    "-fx-focus-color: transparent; " +
                    "-fx-faint-focus-color: transparent; " +
                    "-fx-background-insets: 0;"
                );
        }        
        // THEME CLAIR
        else {
            root.setStyle("-fx-base: #f8fafc; -fx-background: #f8fafc; -fx-selection-bar: #e2e8f0;");

            this.outils.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1px 0; -fx-padding: 4px 10px;");
            this.environnement.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 1px 0 0;");
            this.editeur.setStyle(
                "-fx-control-inner-background: white; " + 
                "-fx-text-fill: black; " + 
                "-fx-focus-color: transparent; " +
                "-fx-faint-focus-color: transparent; " +
                "-fx-background-insets: 0;"
            );
        }
    }
}