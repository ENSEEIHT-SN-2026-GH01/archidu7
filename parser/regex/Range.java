package parser.regex;

public class Range implements Regex {

  public static final char OPENER = '[';
  public static final char CLOSER = ']';

  private char left;
  private char right;

  public Range(char left, char right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public String toString() {
    return "[" + left + "-" + right + "]";
  }

  public char getLeft() {
    return left;
  }

  public char getRight() {
    return right;
  }

  @Override
  public boolean equals(Regex other) {
    return other instanceof Range otherRange && left == otherRange.getLeft() && right == otherRange.getRight();
  }

}
