/**
 * Structure de données sortantes de l'éditeur de texte.
 * Immuable : une nouvelle instance est créée à chaque envoi aux autres composants.
 */
public class EditorContent {

    private final String nomModule;
    private final String sourceCode;
    private final String typeLangage;
    private final boolean estModifie;

    public EditorContent(String nomModule, String sourceCode, String typeLangage, boolean estModifie) {
        if (nomModule == null || nomModule.isBlank())
            throw new IllegalArgumentException("Le nom du module ne peut pas être vide.");
        if (sourceCode == null)
            throw new IllegalArgumentException("Le code source ne peut pas être null.");
        this.nomModule = nomModule;
        this.sourceCode = sourceCode;
        this.typeLangage = typeLangage != null ? typeLangage : "shdl";
        this.estModifie = estModifie;
    }

    public String getNomModule() {
        return nomModule;
    }
    
    public String getSourceCode() {
        return sourceCode;
    }
    
    public String getTypeLangage() {
        return typeLangage;
    }
    
    public boolean estModifie() {
        return estModifie;
    }

    @Override
    public String toString() {
        return "EditorContent{module='" + nomModule + "', langage='" + typeLangage
                + "', modified=" + estModifie + ", codeLength=" + sourceCode.length() + "}";
    }
}
