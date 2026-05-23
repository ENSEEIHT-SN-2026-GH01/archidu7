package editeur.coloration;

import editeur.EditeurTexte;
import javafx.scene.paint.Color;
import parser.lexer.Lexem;
import parser.lexer.Token;

/**Colorateur pour les simili-parenthèses. */
public class ColorateurParenthese implements ColorateurToken{
    private Color couleur;
    private Lexem<Token> tok;

    public ColorateurParenthese(Lexem<Token> tok, EmpilieurParenthese empileur){
        this.tok = tok;
        couleur = choixCouleur(0);
        empileur.traiter(this);
    }

    private static Color choixCouleur(int profondeur){
        switch (profondeur) {
            case 1:
                return Color.PURPLE;
            case 2:
                return Palette.adapter(Color.BLUE, Color.LIGHTSKYBLUE);
            case 3:
                return Palette.adapter(Color.YELLOW, Color.LIGHTYELLOW);
            case 4:
                return Palette.adapter(Color.DARKGREEN, Color.LIMEGREEN);
            case 5:
                return Palette.adapter(Color.ORANGE, Color.LIGHTSALMON);
            default:
                return Color.RED;
        }
    }

    public void setCouleur(int profondeur){
        couleur = choixCouleur(profondeur);
    }

    public Lexem<Token> getToken(){
        return tok;
    }

    @Override
    public Color getCouleur(){
        return couleur;
    }

    @Override
    public void appliqueCouleur(EditeurTexte editeur){
        editeur.colorier(tok.getIndexDepart(), tok.getIndexFin() - 1, couleur);
    }
}
