package sauvegarde;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import simulateur.appel.GestionnaireModules;

public class TextFileStorage implements FileStorage {

    private final Charset charset;
    private String chemin;
    private String fichierOuvert;
    private List<SaveListener> ecouteurs;

    // Constructeur par défaut : UTF-8
    public TextFileStorage() {
        this.charset = StandardCharsets.UTF_8;
        ecouteurs = new ArrayList<>();
        chemin = System.getProperty("user.dir");

        GestionnaireModules.sauveur = this;
    }

    // Constructeur avec charset personnalisé
    public TextFileStorage(Charset charset) {
        this.charset = charset;
        ecouteurs = new ArrayList<>();
    }

    @Override
    public void save(String filename, String content) throws IOException {
        Path path = Paths.get(filename);

        // Crée les dossiers parents si nécessaire
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        Files.writeString(
            path,
            content,
            charset,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );

        fichierOuvert = filename;

        notifier();
    }

    @Override
    public String load(String filename) throws IOException {
        Path path = Paths.get(chemin + filename);

        if (!Files.exists(path)) {
            throw new IOException("Fichier introuvable : " + path.toAbsolutePath());
        }

        String content = Files.readString(path, charset);
        
        /*suppression de caractères problèmatiques */
        content = content.replace((char) 13, ' ');

        fichierOuvert = filename;
        return content;
    }

    @Override
    public String getActuel(){
        return fichierOuvert;
    }

    @Override
    public void setActuel(String chemin){
        this.fichierOuvert = chemin;
    }

    @Override
    public String getChemin(){
        return chemin;
    }

    @Override
    public void setChemin(String nouv){
        if (!chemin.contentEquals(nouv)){
            chemin = nouv;
            notifier();
            fichierOuvert = null;
        }
    }

    @Override
    public void addListener(SaveListener ecouteur){
        ecouteurs.add(ecouteur);
    }

    private void notifier(){
        for (SaveListener saveListener : ecouteurs) {
            saveListener.onSave();
        }
    }
}