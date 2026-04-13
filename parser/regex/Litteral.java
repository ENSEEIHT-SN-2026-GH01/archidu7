package parser.regex;

public class Litteral implements Regex {
  private char character;

  public Litteral(char c) {
    character = c;
  }

  public char getCharacter() {
    return character;
  }

  @Override
  public String toString() {
    if (character == '\n') {
      return "\\n";
    }
    if (character == '\t') {
      return "\\t";
    }
    if (character == '\r') {
      return "\\r";
    }
    if (character == '*') {
      return "\\*";
    }
    if (character == '#') {
      return "\\#";
    }
    if (character == '(') {
      return "\\(";
    }
    if (character == ')') {
      return "\\)";
    }
    if (character == '[') {
      return "\\[";
    }
    if (character == ']') {
      return "\\]";
    }
    if (character == '\\') {
      return "\\\\";
    }

    return "" + character;
  }

  @Override
  public boolean equals(Regex other) {
    return other instanceof Litteral otherLitteral && character == otherLitteral.getCharacter();
  }
}
