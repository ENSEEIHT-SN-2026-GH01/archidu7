import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TextFileStorage implements FileStorage {

    private final Charset charset;

    // Constructeur par défaut : UTF-8
    public TextFileStorage() {
        this.charset = StandardCharsets.UTF_8;
    }

    // Constructeur avec charset personnalisé
    public TextFileStorage(Charset charset) {
        this.charset = charset;
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

        System.out.println("Fichier sauvegardé : " + path.toAbsolutePath());
    }

    @Override
    public String load(String filename) throws IOException {
        Path path = Paths.get(filename);

        if (!Files.exists(path)) {
            throw new IOException("Fichier introuvable : " + path.toAbsolutePath());
        }

        String content = Files.readString(path, charset);
        System.out.println("Fichier chargé : " + path.toAbsolutePath());
        return content;
    }
}