import java.io.IOException;
import java.security.InvalidParameterException;
import editeur.EditeurTexte;
import javafx.scene.control.Button;

/* Bouton représentant un fichier .shdl de la liste de gauche.
 * Au clic, charge le contenu du fichier dans l'éditeur. */
public class FichierModuleBouton extends Button {

    public static double moduleBoutonLargeur = 200;
    public static double moduleBoutonHauteur = 70;

    private final String cheminFichier;

    /* Prend un nom de fichier .shdl et une référence vers l'éditeur. */
    public FichierModuleBouton(String nomFichier, EditeurTexte editeur) throws InvalidParameterException {

        int tailleNom = nomFichier.length() - 5;

        if (tailleNom < 1 || !nomFichier.substring(tailleNom).equals(".shdl")) {
            throw new InvalidParameterException(nomFichier + " n'est pas un module shdl");
        }

        super(nomFichier.substring(0, tailleNom));
        setPrefSize(moduleBoutonLargeur, moduleBoutonHauteur);

        cheminFichier = "./modules/" + nomFichier;

        setOnAction(event -> {
            TextFileStorage storage = new TextFileStorage();
            try {
                String contenu = storage.load(cheminFichier);
                editeur.setText(contenu);
            } catch (IOException e) {
                editeur.setText("// Erreur : impossible de charger " + nomFichier + "\n// " + e.getMessage());
            }
        });
    }
}
