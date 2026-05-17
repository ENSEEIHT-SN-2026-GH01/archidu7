package parser.regex;

public class Joker implements Regex {
  public static final char CHARCTER = '.';

  @Override
  public String toString() {
    return ".";
  }

  @Override
  public boolean equals(Regex other) {
    return other instanceof Joker;
  }

  @Override
  public Regex simplify() {
    return this;
  }

  @Override
  public boolean isNotCompatible() {
    return true;
  }
}
