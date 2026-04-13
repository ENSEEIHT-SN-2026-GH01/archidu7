package parser.regex;

public class Star implements Regex {
  private Regex insideRegex;

  public final static char OPERATOR = '*';

  public Star(Regex r) {
    insideRegex = r;
  }

  public Regex getInsideRegex() {
    return insideRegex;
  }

  @Override
  public String toString() {
    return "(" + insideRegex + ")*";
  }

  @Override
  public boolean equals(Regex other) {
    return other instanceof Star otherStar && insideRegex.equals(otherStar.getInsideRegex());
  }
}
