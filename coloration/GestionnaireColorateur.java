package coloration;

import java.util.ArrayList;
import java.util.List;
import editeur.EditeurTexte;
import parser.lexer.Lexem;
import parser.lexer.Token;

/**Gere la coloration du texte. */
public class GestionnaireColorateur {
    
    private ArrayList<ColorateurToken> colorateurs;
    private final EditeurTexte zone;
    private final EmpilieurParenthese empileur;

    public GestionnaireColorateur(EditeurTexte editeur){
        zone = editeur;
        colorateurs = new ArrayList<>();
        empileur = new EmpilieurParenthese();
    }

    private ColorateurToken choixColorateur(Lexem<Token> tok){
        switch (tok.getToken()) {
            case LeftPar, LeftSquareBrack, RightSquareBrack, RightPar:
                return new ColorateurParenthese(tok, empileur);
            default:
                return new ColorateurUnique(tok);
        }
    }

    public void appendToken(Lexem<Token> tok){
        colorateurs.addLast(choixColorateur(tok));
    }

    public void colorierAll(){
        for (ColorateurToken colorateurToken : colorateurs) {
            colorateurToken.appliqueCouleur(zone);
        }
    }

    /**Applique tous le processus de colorisation à tous le texte.
     * 
     * @param toks Tous les tokens du textes dans l'ordre d'apparition.
     */
    public void gerrerAll(List<Lexem<Token>> toks){
        //reinitialisation
        empileur.reset();
        colorateurs.clear();

        //creation des colorateurs
        for (Lexem<Token> token : toks) {
            appendToken(token);
        }

        //colorisation
        colorierAll();
    }
}
