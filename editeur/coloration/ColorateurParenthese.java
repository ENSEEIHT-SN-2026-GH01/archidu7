package editeur.coloration;

import editeur.EditeurTexte;
import parser.lexer.Lexem;
import parser.lexer.Token;

/**Colorateur pour les simili-parenthèses. */
public class ColorateurParenthese implements ColorateurToken{
    private String classe;
    private Lexem<Token> tok;

    public ColorateurParenthese(Lexem<Token> tok, EmpilieurParenthese empileur){
        this.tok = tok;
        classe = choixClasse(0);
        empileur.traiter(this);
    }

    private static String choixClasse(int profondeur){
        switch (profondeur) {
            case 1:  return "par-1";
            case 2:  return "par-2";
            case 3:  return "par-3";
            case 4:  return "par-4";
            case 5:  return "par-5";
            default: return "par-x";
        }
    }

    public void setCouleur(int profondeur){
        classe = choixClasse(profondeur);
    }

    public Lexem<Token> getToken(){
        return tok;
    }

    @Override
    public void appliqueCouleur(EditeurTexte editeur){
        editeur.colorier(tok.getIndexDepart(), tok.getIndexFin() - 1, classe);
    }
}
