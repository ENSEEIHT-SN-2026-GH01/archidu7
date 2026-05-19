package editeur.coloration;
import editeur.EditeurTexte;
import parser.lexer.Lexem;
import parser.lexer.Token;

/**Class de colorateur pour les token qui seront toujours de la même couleur. */
public class ColorateurUnique implements ColorateurToken{

    private Lexem<Token> tok;

    public ColorateurUnique(Lexem<Token> tok){
        this.tok = tok;
    }

    /**
     * Indique la classe CSS associée au type du token.
     * @return Le nom de styleClass défini dans theme-e.css.
     */
    private static String choixClasse(Lexem<Token> tok){
        switch (tok.getToken()) {
            case Identifiant:
                return "sg";
            case NaturalInteger, PointPoint:
                return "num";
            case BitField:
                return "bit";
            case ModuleKW,EndKW,OutputKW,EnabledKW,WhenKW,OnKW,ResetKW,SetKW:
                return "kw";
            case ConcatOp,Colon,OrOp,Star,NotOp,AssignOp,MemAssignOp,Comma,Semicolon,Dollar:
                return "op";
            case Error:
                return "err";
            case Comment:
                return "cm";
            default:
                return "txt-defaut";
        }
    }

    @Override
    public void appliqueCouleur(EditeurTexte editeur){
        editeur.colorier(tok.getIndexDepart(), tok.getIndexFin() - 1, choixClasse(tok));
    }
}
