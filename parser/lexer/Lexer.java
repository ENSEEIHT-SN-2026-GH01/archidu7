package parser.lexer;

import parser.automate.AutomateDeterministe;
import util.Pair;
import parser.regex.*;
import java.util.*;

/**
 * Le lexer principal pour archidu7
 */
public class Lexer {
  private AutomateDeterministe<Token> a;

  /**
   * Construit l'automate à partir des règles de la grammaire
   */
  public Lexer() {
    List<Pair<Regex, Lexem<Token>>> l = new LinkedList<>();
    
    // les règles lues
    l.add(Pair.pair(Builder.parseRegex("\\("), new Lexem<Token>(Token.LeftPar)));
    l.add(Pair.pair(Builder.parseRegex("\\["), new Lexem<Token>(Token.LeftSquareBrack)));
    l.add(Pair.pair(Builder.parseRegex("\\]"), new Lexem<Token>(Token.RightSquareBrack)));
    l.add(Pair.pair(Builder.parseRegex("\\)"), new Lexem<Token>(Token.RightPar)));
    l.add(Pair.pair(Builder.parseRegex("module" ), new Lexem<Token>(Token.ModuleKW)));
    l.add(Pair.pair(Builder.parseRegex("end"), new Lexem<Token>(Token.EndKW)));
    l.add(Pair.pair(Builder.parseRegex("output"), new Lexem<Token>(Token.OutputKW)));
    l.add(Pair.pair(Builder.parseRegex("enabled"), new Lexem<Token>(Token.EnabledKW)));
    l.add(Pair.pair(Builder.parseRegex("when"), new Lexem<Token>(Token.WhenKW)));
    l.add(Pair.pair(Builder.parseRegex("on"), new Lexem<Token>(Token.OnKW)));
    l.add(Pair.pair(Builder.parseRegex("reset"), new Lexem<Token>(Token.ResetKW)));
    l.add(Pair.pair(Builder.parseRegex("set"), new Lexem<Token>(Token.SetKW)));
    l.add(Pair.pair(Builder.parseRegex("&"), new Lexem<Token>(Token.ConcatOp)));
    l.add(Pair.pair(Builder.parseRegex("\\.\\."), new Lexem<Token>(Token.PointPoint)));
    l.add(Pair.pair(Builder.parseRegex(":"), new Lexem<Token>(Token.Colon)));
    l.add(Pair.pair(Builder.parseRegex("\\+"), new Lexem<Token>(Token.OrOp)));
    l.add(Pair.pair(Builder.parseRegex("\\*"), new Lexem<Token>(Token.Star)));
    l.add(Pair.pair(Builder.parseRegex("/"), new Lexem<Token>(Token.NotOp)));
    l.add(Pair.pair(Builder.parseRegex("="), new Lexem<Token>(Token.AssignOp)));
    l.add(Pair.pair(Builder.parseRegex(":="), new Lexem<Token>(Token.MemAssignOp)));
    l.add(Pair.pair(Builder.parseRegex(","), new Lexem<Token>(Token.Comma)));
    l.add(Pair.pair(Builder.parseRegex(";"), new Lexem<Token>(Token.Semicolon)));
    l.add(Pair.pair(Builder.parseRegex("$"), new Lexem<Token>(Token.Dollar)));
    // Lexème safe
    Lexem<Token> ident = new Lexem<Token>(Token.Identifiant);
    ident.setSafe(1);
    l.add(Pair.pair(Builder.parseRegex("[a-zA-Z_][a-zA-Z0-9_]*"), ident));
    l.add(Pair.pair(Builder.parseRegex("\\.[0-1]+"), new Lexem<Token>(Token.BitField)));
    l.add(Pair.pair(Builder.parseRegex("[0-9]+"), new Lexem<Token>(Token.NaturalInteger)));
    l.add(Pair.pair(Builder.parseRegex("[ \\t]"), new Lexem<Token>(Token.whiteSpace, true)));
    l.add(Pair.pair(Builder.parseRegex("[\\r\\n]"), new Lexem<Token>(Token.lineTerminator, true)));
    l.add(Pair.pair(Builder.parseRegex("((//)|#)(~[\\r\\n])*"), new Lexem<Token>(Token.Comment)));

    // Lexème Erreur => safe car il ne doit pas etre pris avant les autres => plus petite priorité
    Lexem<Token> error = new Lexem<Token>(Token.Error);
    error.setSafe(0);
    l.add(Pair.pair(Builder.parseRegex("."), error));

    // création de de l'automate
    a = AutomateDeterministe.fromList(l);
  }

  /**
   * Exécute le lexer sur la chaîne de caractères {@code s}
   * @param s la chaîne a tokenizer
   * @return la liste de lexemes résultante
   */
  public List<Lexem<Token>> tokenize(String s) {
    return a.exec(s);
  }
}