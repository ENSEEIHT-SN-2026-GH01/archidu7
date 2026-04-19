package editeur;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CelluleTexte {
    
    private int debut; //indice du premier element.
    private int fin; //indice du dernier element.
    private Text morceau;
    private CelluleTexte suivant;

    private int indiceListeTexte; //indice dans la liste du TextFlow associé.
    private final ObservableList<Node> listeTexte; //La liste du TextFlow associé.

    public CelluleTexte(int deb, int f, String texte, int indice, ObservableList<Node> liste){
        debut = deb;
        fin = f;
        morceau = new Text(texte);
        suivant = null;

        indiceListeTexte = indice;
        listeTexte = liste;
        listeTexte.add(indiceListeTexte, morceau);
        morceau.setFont(Font.font("Monospaced", 14));
    }

    public int getDebut(){
        return debut;
    }

    public int getFin(){
        return fin;
    }

    /**Revoie la poignee exacte du Text qui est donc modifiable.
     * 
     * @return
     */
    public Text getMorceau(){
        return morceau;
    }

    /**Renvoie la cellule dont l'indice position est inclus dans [debut,fin].
     * 
     * @param position
     * @return
     */
    public CelluleTexte getMorceauAt(int position){
        if (debut  > position) return null;
        else if (fin <= position) return this;
        else if (suivant == null) return null;
        else return suivant.getMorceauAt(position);
    }

    /**Renvoie le texte entre les deux indices mentionné debut inclus, fin non inclus.
     * 
     * @param deb
     * @param fi
     * @return
     */
    public String getSubString(int deb, int fi){
        if (deb > fin) {
            if (suivant == null) return "";
            else return suivant.getSubString(deb, fi);
        }
        else {
            String res = morceau.getText();
            if (fin > fi){
                return res.substring(deb - debut, fi - debut);
            }
            else{
                res = res.substring(deb - debut);
                if (suivant == null) return res;
                else{
                    res = res.concat(suivant.getSubString(fin + 1, fi));
                    return res;
                }
            }
        }
    }

    /**La taille du texte.
     * 
     * @return
     */
    public int length(){
        if (suivant != null) return suivant.length();
        else return fin+1;
    }

    private void propagerChangementIndice(int nouveau){
        indiceListeTexte = nouveau;
        if (suivant != null) suivant.propagerChangementIndice(nouveau + 1);
    }

    /**Decoupe en deux morceaux.
     * 
     * @param milieu Dernier indice absolue dans la premiere liste.
     * @return La cellule après le point de découpe ou null si aucun decoupage
     * n'a pu être effectué.
     */
    public CelluleTexte decoupe(int milieu){
        if (milieu < fin && milieu > debut){
            String txt = morceau.getText();

            //nouvelle cellule
            CelluleTexte nouvelle = new CelluleTexte(milieu + 1, fin, txt.substring(milieu + 1 - debut, fin - debut + 1), indiceListeTexte +1, listeTexte); 
            nouvelle.suivant = suivant;
            nouvelle.propagerChangementIndice(indiceListeTexte + 1);

            //changement de la cellule actuelle
            morceau.setText(txt.substring(0, milieu + 1 - debut));
            fin = milieu;
            suivant = nouvelle;

            return nouvelle;
        }
        else if(suivant != null && milieu > debut){
            return suivant.decoupe(milieu);
        }
        else return null;
    }

    private void terminer(){
        listeTexte.remove(indiceListeTexte);
    }

    /**Recolle le texte entre les deux indices.
     * 
     * @param debutRecolle
     * @param finRecolle
     * @return La cellule affecté par le recollement.
     */
    public CelluleTexte recoller(int debutRecolle, int finRecolle){
        if (suivant != null){
            if (fin < debutRecolle){ //trouver le premier morceau concerné
                return suivant.recoller(debutRecolle, finRecolle);
            }
            else if(fin < finRecolle){
                //appel récurssif
                suivant.recoller(debutRecolle, finRecolle);

                //suppression du suivant dans la liste des textes
                suivant.terminer();

                //recollage
                fin = suivant.fin;
                morceau.setText(morceau.getText().concat(suivant.getMorceau().getText()));
                suivant = suivant.suivant;
                if (suivant != null && debutRecolle >= debut) suivant.propagerChangementIndice(indiceListeTexte + 1);
                return this;
            }
        }
        return this;
    }

    /**Creer un morceau non sur-découpé.
     * 
     * @param debutMorceau
     * @param finMorceau
     * @return Le morceau en question.
     */
    public CelluleTexte creerMorceau(int debutMorceau, int finMorceau){
        CelluleTexte partie = decoupe(debutMorceau - 1);
        if(partie != null) {
            partie.recoller(debutMorceau, finMorceau);
            partie.decoupe(finMorceau);
            return partie;
        }
        else {
            partie = recoller(debutMorceau, finMorceau);
            partie.decoupe(finMorceau);
            return partie;
        }
    }

    /**Fait les decoupe et les recollements necéssaires pour segmenter
     * le texte à des points spécifiques.
     * 
     * @param pointsDecoupe Les indices des points de decoupes du texte.
     */
    public void segmentation(int[] pointsDecoupe){
        //on recolle tous
        CelluleTexte actuel = recoller(0, pointsDecoupe[pointsDecoupe.length - 1] + 1);
        for (int i : pointsDecoupe) {
            actuel = actuel.decoupe(i);
        }
    }

    private void propagerDecalage(int delta){
        debut += delta;
        fin += delta;
        if (suivant != null) suivant.propagerDecalage(delta);
    }

    /**Inserre du texte ou l'ajoute à la fin.
     * 
     * @param apres L'indice du caractère après lequel il faut inserer,
     * s'il est trop grand, le texte est ajouté à la fin.
     * @param nouveau Le texte à inserrer.
     * @return La cellule concerné par ce changement.
     */
    public CelluleTexte appendText(int apres, String nouveau){
        int taille = nouveau.length();
        if (fin < apres){
            if (suivant == null) {
                morceau.setText(morceau.getText().concat(nouveau));
                fin += taille;
                return this;
            }
            else{
                return suivant.appendText(apres, nouveau);
            }
        }
        else{
            String actuel = morceau.getText();
            morceau.setText(actuel.substring(0, apres+1 - debut) + nouveau + actuel.substring(apres + 1 - debut));
            fin += taille;
            if (suivant != null) suivant.propagerDecalage(taille);
            return this;
        }
    }

    /**Supprime un bout de texte.
     * 
     * @param debutSuppr Indice du premier élement supprimé.
     * @param finSuppr Indice de l'élément après le dernier supprimé.
     */
    public CelluleTexte supprimerText(int debutSuppr, int finSuppr){
        if (debutSuppr > fin){ //recherche de la première cellule concernée
            if (suivant != null) return suivant.supprimerText(debutSuppr, finSuppr);
            else return this;
        }
        else if(finSuppr > fin +1){ //suppression sur plusieurs morceaux

            /*appel recurssif */
            if (suivant != null){
                suivant.supprimerText(fin + 1, finSuppr);
                if (suivant.getDebut() > suivant.getFin()) suivant = suivant.suivant; //supression des cellules vides
            }

            /*supression sur ce morceau */
            String actuel = morceau.getText();
             morceau.setText(actuel.substring(0, debutSuppr - debut));

            /*decalage */
            int delta = debutSuppr - fin - 1;
            fin += delta; //delta est negatif
            if (suivant != null) suivant.propagerDecalage(delta);

            return this;
        }
        else{
            String actuel = morceau.getText();
  
            morceau.setText(actuel.substring(0, debutSuppr - debut) + actuel.substring(finSuppr - debut, fin - debut + 1));
            int delta = debutSuppr - finSuppr ;
            fin += delta; //delta est negatif
            if (suivant != null) suivant.propagerDecalage(delta);

            return this;
        }
    }

    public void afficher(){
        System.out.print(";" + "i:" + indiceListeTexte + ";" + debut + "|");
        System.out.print(morceau.getText());
        System.out.print("|" + fin);
        if (suivant != null) suivant.afficher();
        else System.out.println();
    }
}
