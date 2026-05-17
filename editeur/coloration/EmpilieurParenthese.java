package editeur.coloration;

import java.util.Stack;

public class EmpilieurParenthese {
    private Stack<ColorateurParenthese> parentheses;
    private Stack<ColorateurParenthese> crochets;

    public EmpilieurParenthese(){
        parentheses = new Stack<>();
        crochets = new Stack<>();
    }

    private void empilerParenthese(ColorateurParenthese appelant){
        parentheses.push(appelant);
    }

    private void empilerCrochet(ColorateurParenthese appelant){
        crochets.push(appelant);
    }

    private int consommerParenthese(){
        int res = parentheses.size();
        if (res > 0) {
            ColorateurParenthese ouvrante = parentheses.pop();
            ouvrante.setCouleur(res);
        }
        return res;
    }

    private int consommerCrochet(){
        int res = crochets.size();
        if (res > 0) {
            ColorateurParenthese ouvrante = crochets.pop();
            ouvrante.setCouleur(res);
        }
        return res;
    }

    /**Donne au colorateur la couleur qu'il doit donner.
     * 
     * @param appelant Le colorateur appelant.
     */
    public void traiter(ColorateurParenthese appelant){
        switch (appelant.getToken().getToken()) {
            case LeftPar:
                empilerParenthese(appelant);
                break;
            case RightPar:
                appelant.setCouleur(consommerParenthese());
                break;
            case LeftSquareBrack:
                empilerCrochet(appelant);
                break;
            case RightSquareBrack:
                appelant.setCouleur(consommerCrochet());
                break;
            default:
                break;
        }
    }

    /**Vide toutes les piles.
     * 
     */
    public void reset(){
        parentheses.clear();
        crochets.clear();
    }
}
