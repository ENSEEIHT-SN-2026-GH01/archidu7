package editeur;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;

/**Class de texte decoupable en morceau. Abstraite car innutile tant que l'on
 * ne peut pas appliquer un traitement specifique à chaques morceaux.
 */
public abstract class TextDecoupable extends TextFlow{

    protected CelluleTexte morceaux; //tete de liste
    protected CelluleTexte cache; //dernière cellule qui a été renvoyé par une des méthode de CelluleText
    protected Font font;

    public TextDecoupable(int fontSize){
        font = Font.font("monospace", fontSize);
        morceaux = new CelluleTexte(0, -1, "", 0, getChildren(), font);
        corrigeLineSpace(fontSize);
        cache = morceaux;
    }

    public TextDecoupable(String txt, int fontSize){
        font = Font.font("monospace", fontSize);
        morceaux = new CelluleTexte(0, txt.length() - 1, txt, 0, getChildren(), font);
        corrigeLineSpace(fontSize);
        cache = morceaux;
    }

    /**Recuppérrer tous le texte.
     * 
     * @return Le texte entier sous forme de chaine de caractère.
     */
    public String getText(){
        return morceaux.getSubString(0, Integer.MAX_VALUE);
    }
    
    /**Remplace tous le texte.
     * 
     * @param txt Le nouveau texte.
     */
    public void setText(String txt){
        getChildren().clear();
        morceaux = new CelluleTexte(0, txt.length() + 1, txt, 0, getChildren(), font);
        cache = morceaux;
    }

    /**Recupère un morceau du texte entre deux indices
     * sans changer le decoupage du texte.
     * 
     * @param debut 
     * @param fin
     * @return Un bout du texte.
     */
    public String getSubstring(int debut, int fin){
        if (cache.getDebut() <= debut){
            return cache.getSubString(debut, fin);
        }
        else return morceaux.getSubString(debut, fin);
    }

    /**La taille du texte.
     * 
     * @return
     */
    public int length(){
        if (cache != null) return cache.length();
        else return morceaux.length();
    }

    /**Inserre du texte ou l'ajoute à la fin.
     * 
     * @param apres L'indice du caractère après lequel il faut inserer,
     * s'il est trop grand, le texte est ajouté à la fin.
     * @param nouveauTexte Le texte à inserrer.
     */
    public void inserrer(int apres, String nouveauTexte){
        if (cache.getDebut() <= apres - 1){
            cache = cache.appendText(apres, nouveauTexte);
        }
        else cache = morceaux.appendText(apres, nouveauTexte);
    }

    /**Supprime un bout de texte.
     * 
     * @param debutSuppr Indice du premier élement supprimé.
     * @param finSuppr Indice de l'élément après le dernier supprimé.
     */
    public void supprimer(int debutSuppr, int finSuppr){
        if (cache.getDebut() <= debutSuppr - 1){
            cache = cache.supprimerText(debutSuppr, finSuppr);
        }
        else cache = morceaux.supprimerText(debutSuppr, finSuppr);
    }

    /**Decoupe le morceau du texte entre deux indices inclus,
     * Le morceau crée est en un seul morceau.
     * 
     * @param debut
     * @param fin
     */
    public void decoupeMorceau(int debut, int fin){
        if (cache.getDebut() <= debut){
            cache = cache.creerMorceau(debut, fin);
        }
        else cache = morceaux.creerMorceau(debut, fin);
    }

    /**Recolle entre les deux indices.
     * 
     * @param debut
     * @param fin
     */
    public void recollerEntre(int debut, int fin){
        if (cache.getDebut() <= debut){
            cache = cache.recoller(debut, fin);
        }
        else cache = morceaux.recoller(debut, fin);
    }

    /**Fait les decoupe et les recollements necéssaires pour segmenter
     * le texte à des points spécifiques.
     * 
     * @param pointsDecoupe Les indices des points de decoupes du texte.
     */
    public void segmentation(int[] pointsDecoupe){
        morceaux.segmentation(pointsDecoupe);
    }



    /*méthodes liés au style. */

    private void corrigeLineSpace(double fontSize){
        setLineSpacing(0.11875 * fontSize - 1.02);
    }

    /**Change taille de la police.
     * 
     * @param nouvelle
     */
    public void setFont(double nouvelle){
        font = Font.font(font.getFamily(), nouvelle);
        morceaux.setFont(nouvelle);
        corrigeLineSpace(nouvelle);
    }

    /**Change la police.
     * 
     * @param nouvelle
     */
    public void setFont(Font nouvelle){
        font = nouvelle;
        morceaux.setFont(nouvelle);
        corrigeLineSpace(nouvelle.getSize());
    }
}
