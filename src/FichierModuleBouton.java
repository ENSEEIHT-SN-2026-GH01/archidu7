import java.security.InvalidParameterException;
import javafx.scene.control.Button;

/* pour l'instant, il n'a qu'un nom, mais il faudrait qu'il ai un fichier associe */
public class FichierModuleBouton extends Button {

    public static double moduleBoutonLargeur = 200;
    public static double moduleBoutonHauteur = 70;

    /* Prend un fichier .shdl en argument */
    public FichierModuleBouton(String nomFichier) throws InvalidParameterException {
        super(nomSansExtension(nomFichier));
        setPrefSize(FichierModuleBouton.moduleBoutonLargeur, FichierModuleBouton.moduleBoutonHauteur);
    }

    private static String nomSansExtension(String nomFichier) {
        int tailleNom = nomFichier.length() - 5;
        if (tailleNom < 1 || !nomFichier.substring(tailleNom).equals(".shdl")) {
            throw new InvalidParameterException(nomFichier + " n'est pas un module shdl");
        }
        return nomFichier.substring(0, tailleNom);
    }
}
