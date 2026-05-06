package sauvegarde;

import java.io.IOException;


public interface FileStorage {

    /**
     * Sauvegarde du contenu dans un fichier texte.
     *
     * @param filename le chemin absolu/relatif de destination
     * @param content  le contenu textuel à sauvegarder
     * @throws IOException en cas d'erreur d'écriture
     */
    void save(String filename, String content) throws IOException;

    /**
     * Charge le contenu d'un fichier texte.
     *
     * @param filename le chemin absolu/relatif du fichier à lire
     * @return le contenu du fichier sous forme de String
     * @throws IOException en cas d'erreur de lecture ou fichier introuvable
     */
    String load(String filename) throws IOException;

    /**Donne le chemin du fichier actuellement ouvert.
     * 
     * @return un chemin vers un fichier ou null si aucun n'est ouvert.
     */
    String getPath();

    /**Change le chemin du fichier actuellemnt ouvert.
     * 
     * @param chemin Le chemin du nouveau fichier ouvert.
     */
    void setPath(String chemin);
}