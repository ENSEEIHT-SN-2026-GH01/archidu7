import java.security.InvalidParameterException;
import javafx.scene.control.Button;

/*pour l'instant, il n'a qu'un nom, mais il faudrait qu'il ai un fichier associé */
public class FichierModuleBouton extends Button{

    public static double moduleBoutonLargeur = 200;
    public static double moduleBoutonHauteur = 70;

    /*Prend un fichier .shdl en argument */
    public FichierModuleBouton(String nomFichier) throws InvalidParameterException{

        //taille du nom sans le .shdl
        int tailleNom = nomFichier.length() - 5;


        if (tailleNom < 1 || !nomFichier.substring(tailleNom).equals(".shdl")){ //on verifie que c'est bien un module shdl
            throw new InvalidParameterException(nomFichier + "n'est pas un module shdl");
        }
        
        super(nomFichier.substring(0, tailleNom)); //on lui donne le bon nom (sans le .shdl à la fin)
        setPrefSize(FichierModuleBouton.moduleBoutonLargeur, FichierModuleBouton.moduleBoutonHauteur);
    }
}
