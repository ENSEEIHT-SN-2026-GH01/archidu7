package editeur.coloration;
import editeur.EditeurTexte;
import javafx.scene.paint.*;
import parser.lexer.Lexem;
import parser.lexer.Token;

/**Class de colorateur pour les token qui seront toujours de la même couleur. */
public class ColorateurUnique implements ColorateurToken{

    private Lexem<Token> tok;

    public ColorateurUnique(Lexem<Token> tok){
        this.tok = tok;
    }

    /**
     * Indique une couleur associé au type du token.
     * @return La couleur associé.
     */
    private static Color choixCouleur(Lexem<Token> tok){
        switch (tok.getToken()) {
            case Identifiant:
                return Color.rgb(20, 20, 100);
            case NaturalInteger, PointPoint:
                return Color.ORANGE;
            case BitField:
                return Color.LIGHTGREEN;

            case ModuleKW,EndKW,OutputKW,EnabledKW,WhenKW,OnKW,ResetKW,SetKW:
                return Color.BLUE;

            case ConcatOp,Colon,OrOp,Star,NotOp,AssignOp,MemAssignOp,Comma,Semicolon,Dollar:
                 return Color.DARKGRAY;

            case Error:
                return Color.RED;

            case Comment:
                return Color.GREEN;
            default:
                return Color.DARKBLUE;
        }
    }

    @Override
    public Color getCouleur(){
        return choixCouleur(tok);
    }

    @Override
    public void appliqueCouleur(EditeurTexte editeur){
        Color couleur = choixCouleur(tok);
        editeur.colorier(tok.getIndexDepart(), tok.getIndexFin() - 1, couleur);
    }
}
