package parser.regex;

public class SyntaxError extends RuntimeException {

  private String problem;
  private int pos;

  public SyntaxError(int pos, String problem, String line) {
    this.pos = pos;
    this.problem = problem;
    super("Syntax error at character '" + line.charAt(pos) + "' at pos " + pos + " : " + problem);
  }

  public String getProblem() {
    return problem;
  }

  public int getPos() {
    return pos;
  }
}
