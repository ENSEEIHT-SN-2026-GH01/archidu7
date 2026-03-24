package parser.regex;

public class Epsilon implements Regex {

  @Override
  public String toString() {
    return "#";
  }

  @Override
  public boolean equals(Regex other) {
    return other instanceof Epsilon;
  }

}
