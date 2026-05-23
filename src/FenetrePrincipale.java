import editeur.EditeurTexte;
import editeur.coloration.GestionnaireColorateur;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.SplitPane;
import parser.lexer.Lexer;
import sauvegarde.FileStorage;
import sauvegarde.TextFileStorage;
import simulateur.appel.GestionnaireModules;
import boutons.BoutonsPrincipale;

public class FenetrePrincipale extends Scene {
    
    private GestionnaireColorateur colorateur;
    private Lexer lexer;

    private VBox outils;
    private ListeModulePrincipale environnement;
    private EditeurTexte editeur;
    private MenuPrincipale menu;
    private BandeauOnglet onglets;

    public FenetrePrincipale() { 

        super(new BorderPane(), 1000, 600);
        BorderPane root = (BorderPane) this.getRoot();

        this.editeur = new EditeurTexte();
        FileStorage stockage = new TextFileStorage(); 

        BoutonsPrincipale boutons = new BoutonsPrincipale(editeur, stockage);
        this.onglets = new BandeauOnglet(stockage, editeur);
        this.environnement = new ListeModulePrincipale(editeur, stockage, this.onglets);

        this.menu = new MenuPrincipale();
        this.menu.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
        this.menu.getModeSombre().setOnAction(event -> {
            boolean estSombre = this.menu.getModeSombre().isSelected();
            appliquerTheme(estSombre);
        });

        this.outils = new VBox(this.menu, boutons, this.onglets);

        //ajuster la largeur du paneau de gauche
        SplitPane espaceDeTravail = new SplitPane();
        espaceDeTravail.getItems().addAll(environnement, editeur);
        espaceDeTravail.setDividerPositions(0.2f);
        espaceDeTravail.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        environnement.setMinWidth(150); // On permet de rétrécir la barre de gauche jusqu'à 150 pixels
        editeur.setMinWidth(300); // L'éditeur garde au moins 300 pixels d'espace

        root.setTop(outils);
        root.setCenter(espaceDeTravail);
        
        colorateur = new GestionnaireColorateur(editeur);
        lexer = new Lexer();
        GestionnaireModules.lexer = lexer;
        editeur.addListener(new EditeurListener());

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
            this.outils.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 0 0 1px 0;");
            this.environnement.setStyle("-fx-background-color: #252526; -fx-border-color: #404040; -fx-border-width: 0 1px 0 0;");
            this.editeur.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #d4d4d4; -fx-prompt-text-fill: #6b6b6b; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0;");
        } 
        // THEME CLAIR
        else {
            root.setStyle("-fx-base: #f8fafc; -fx-background: #f8fafc; -fx-selection-bar: #e2e8f0;");
            this.outils.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1px 0;");
            this.environnement.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 1px 0 0;");
            this.editeur.setStyle("-fx-control-inner-background: white; -fx-text-fill: black; -fx-prompt-text-fill: gray; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0;");
        }
    }
}