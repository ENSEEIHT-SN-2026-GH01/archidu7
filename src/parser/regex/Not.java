package parser.regex;

public class Not implements Regex {

  public static final char CHARACTER = '~';

  private Regex insideRegex;

  public Not() {
  }

  public Not(Regex r) {
    if (!r.isNotCompatible()) {
      throw new SyntaxError("Not operation can only have single length Regex");
    }

    insideRegex = r;
  }

  public Regex getInsideRegex() {
    return insideRegex;
  }

  public void setInsideRegex(Regex insideRegex) {
    this.insideRegex = insideRegex;
  }

  @Override
  public boolean equals(Regex other) {
    return other instanceof Not otherNot && insideRegex.equals(otherNot.getInsideRegex());
  }

  @Override
  public Regex simplify() {
    insideRegex = insideRegex.simplify();

    if (insideRegex instanceof Epsilon) {
      return new Joker();
    }

    if (insideRegex instanceof Joker) {
      return new Epsilon();
    }

    return this;
  }

  @Override
  public boolean isNotCompatible() {
    return false;
  }
}
