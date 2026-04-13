/**
 * État interne mutable d'un fichier ouvert dans l'éditeur.
 * Se convertit en EditorContent pour l'envoi aux autres composants.
 */
public class Document {

    private String nom;
    private String contenu;
    private String langage;
    private boolean modifie;

    public Document(String nom, String langage) {
        this(nom, "", langage);
    }

    public Document(String nom, String contenu, String langage) {
        this.nom = nom;
        this.contenu = contenu;
        this.langage = langage;
        this.modifie = false;
    }

    public String getNom() {
        return nom;
    }
    
    public String getContenu() {
        return contenu;
    }
    public boolean estModifie() {
        return modifie; 
    }

    public void setContenu(String contenu) {
        if (!this.contenu.equals(contenu)) {
            this.contenu = contenu;
            this.modifie = true;
        }
    }

    public void Sauvegarder() {
        this.modifie = false;
    }

    /** Produit la structure de données sortantes à partir du document. */
    public EditorContent versContenuEditeur() {
        return new EditorContent(nom, contenu, langage, modifie);
    }
}
