package parser.regex;

public class Plus implements Regex {
  private Regex insideRegex;

  public final static char OPERATOR = '+';

  public Plus(Regex r) {
    insideRegex = r;
  }

  public Regex getInsideRegex() {
    return insideRegex;
  }

  @Override
  public String toString() {
    return "(" + insideRegex + ")+";
  }

  @Override
  public boolean equals(Regex other) {
    return other instanceof Plus otherPlus && insideRegex.equals(otherPlus.getInsideRegex());
  }

  @Override
  public Regex simplify() {
    insideRegex = insideRegex.simplify();

    if (insideRegex instanceof Epsilon){
      return insideRegex;
    }
    
    return this;
  }

  @Override
  public boolean isNotCompatible() {
    return false;
  }
}
