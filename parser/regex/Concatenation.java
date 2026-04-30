package parser.regex;

public class Concatenation implements Regex {
  private Regex leftRegex;
  private Regex rightRegex;

  public Concatenation(Regex r1, Regex r2) {
    leftRegex = r1;
    rightRegex = r2;
  }

  public Regex fromString(String s) {
    return new Concatenation(new Litteral(s.charAt(0)), fromString(s.substring(1)));
  }

  public Regex getLeftRegex() {
    return leftRegex;
  }

  public Regex getRightRegex() {
    return rightRegex;
  }

  @Override
  public String toString() {
    return "" + leftRegex + rightRegex;
  }

  @Override
  public boolean equals(Regex other) {
    return other instanceof Concatenation otherConcatenation
        && leftRegex.equals(otherConcatenation.getLeftRegex())
        && rightRegex.equals(otherConcatenation.getRightRegex());
  }
}
