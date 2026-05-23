import editeur.EditeurTexte;
import editeur.NumeroteurLigne;
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
import editeur.coloration.Palette;

public class FenetrePrincipale extends Scene {
    
    private GestionnaireColorateur colorateur;
    private Lexer lexer;

    private VBox outils;
    private ListeModulePrincipale environnement;
    private EditeurTexte editeur;
    private MenuPrincipale menu;
    private BandeauOnglet onglets;
    private NumeroteurLigne numeroteurLigne;

    public FenetrePrincipale() { 

        super(new BorderPane(), 1000, 600);
        BorderPane root = (BorderPane) this.getRoot();

        this.editeur = new EditeurTexte();
        FileStorage stockage = new TextFileStorage(); 

        BoutonsPrincipale boutons = new BoutonsPrincipale(editeur, stockage);
        this.onglets = new BandeauOnglet(stockage, editeur);
        this.environnement = new ListeModulePrincipale(editeur, stockage, this.onglets);

        this.menu = new MenuPrincipale(editeur, stockage);
        this.menu.getModeSombre().setOnAction(event -> {
            boolean estSombre = this.menu.getModeSombre().isSelected();
            appliquerTheme(estSombre);
        });

        this.outils = new VBox(this.menu, boutons, this.onglets);
        this.outils.getStyleClass().add("bandeau");

        //ajuster la largeur du paneau de gauche
        SplitPane espaceDeTravail = new SplitPane();
        BorderPane zoneEditeur = new BorderPane();
        this.numeroteurLigne = new NumeroteurLigne(editeur);
        zoneEditeur.setLeft(this.numeroteurLigne);
        zoneEditeur.setCenter(editeur);
        espaceDeTravail.getItems().addAll(environnement, zoneEditeur);
        espaceDeTravail.setDividerPositions(0.2f);

        SplitPane.setResizableWithParent(editeur, true);
        espaceDeTravail.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        environnement.setMinWidth(150); // On permet de rétrécir la barre de gauche jusqu'à 150 pixels
        editeur.setMinWidth(300); // L'éditeur garde au moins 300 pixels d'espace

        root.setTop(outils);
        root.setCenter(espaceDeTravail);

        root.getStyleClass().add("racine");
        getStylesheets().add(getClass().getResource("/assets/theme-e.css").toExternalForm());

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

    /** Bascule clair/sombre. Le chrome (bandeau, panneau modules, éditeur,
     *  numéros de ligne) suit la classe « sombre » posée sur la racine, via le
     *  CSS. La coloration du code et le texte estompé, gérés en Java, sont
     *  réappliqués via Palette. */
    public void appliquerTheme(boolean sombre) {
        BorderPane root = (BorderPane) this.getRoot();

        if (sombre) root.getStyleClass().add("sombre");
        else        root.getStyleClass().remove("sombre");

        Palette.estModeSombre = sombre;
        this.editeur.rafraichirThemeEditeur();
        this.onglets.setModeSombre(sombre);

        if (colorateur != null) {
            colorateur.colorierAll();
        }
    }
}