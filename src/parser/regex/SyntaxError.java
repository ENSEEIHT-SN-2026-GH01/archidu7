package parser.regex;

/**
 * Erreur de syntaxe lors de la construction d'expressions régulières
 */
public class SyntaxError extends RuntimeException {

  private String problem;
  private int pos = -1;

  public SyntaxError(String problem){
    super("Syntax error at undefined character : " + problem);
    this.problem = problem;
  }

  public SyntaxError(int pos, String problem, String line) {
    super("Syntax error at character '" + line.charAt(pos) + "' at pos " + pos + " : " + problem);
    this.pos = pos;
    this.problem = problem;
  }

  public String getProblem() {
    return problem;
  }

  public int getPos() {
    return pos;
  }
}
