package editeur.autocompletion;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;

import editeur.EditeurTexte;
import parser.lexer.Lexem;
import parser.lexer.Lexer;
import parser.lexer.Token;
import sauvegarde.FileStorage;
import sauvegarde.TextFileStorage;
import simulateur.appel.GestionnaireModules;

public class Dictionary {
  private Set<String> storage;
  private FileStorage fileStorage;
  private EditeurTexte texte;

  public Dictionary(EditeurTexte texte) {
    fileStorage = new TextFileStorage();
    this.texte = texte;

    updateStorage();
  }

  public void updateStorage() {
    storage = new HashSet<>();
    String chemin = fileStorage.getChemin();

    // récupération des noms des modules accessibles depuis le dossier source
    try {
      storage.addAll(
          Files.list(Paths.get(chemin)).map((Path p) -> {
            if (Files.isRegularFile(p) && p.endsWith(".shdl")) {
              try {
                return GestionnaireModules.getNom(fileStorage.load(p.toString(), false));
              } catch (IOException _) {
                return null;
              }
            }
            return null;
          }).filter(new Predicate<String>() {
            public boolean test(String t) {
              return t != null;
            };
          }).toList());

    } catch (IOException e) {
    }

    // récupération de tous les noms de variables de la zone de texte
    List<Lexem<Token>> toks = Lexer.LEXER.tokenize(texte.getText());
    for (Lexem<Token> lexem : toks) {
      if (lexem.getToken() == Token.Identifiant) {
        storage.add(lexem.getText());
      }
    }

    ajouterMotsClefs();
  }

  private void ajouterMotsClefs() {
    storage.add("module");
    storage.add("output");
    storage.add("enabled");
    storage.add("when");
    storage.add("on");
    storage.add("reset");
    storage.add("set");
  }

  public List<String> getClosest(String text) { // TODO either this does not seem to be correct somehown, because it
                                                // returns the names that does not contains the word; or its use is not
                                                // good
    updateStorage();
    List<String> res = new LinkedList<>();

    for (String name : storage) {
      if (name.contains(text)) {
        res.add(name);
      }
    }

    res.sort(new Comparator<String>() {
      private int score(String s) {
        return -text.indexOf(s) - s.length();
      }

      @Override
      public int compare(String s1, String s2) {
        return score(s1) - score(s2);
      }
    });

    return res;
  }
}
