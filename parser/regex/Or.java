package parser.regex;

public class Or implements Regex {

  private Regex leftOperand;
  private Regex rightOperand;

  public static final char OPERATOR = '|';

  public Or(Regex r1, Regex r2) {
    leftOperand = r1;
    rightOperand = r2;
  }

  public Regex getLeftOperand() {
    return leftOperand;
  }

  public Regex getRightOperand() {
    return rightOperand;
  }

  public void setRightOperand(Regex rightOperand) {
    this.rightOperand = rightOperand;
  }

  @Override
  public String toString() {
    return "(" + leftOperand + "|" + rightOperand + ")";
  }

  @Override
  public boolean equals(Regex other) {
    return other instanceof Or otherOr
        && leftOperand.equals(otherOr.getLeftOperand())
        && rightOperand.equals(otherOr.getRightOperand());
  }

  @Override
  public Regex simplify() {
    leftOperand = leftOperand.simplify();
    rightOperand = rightOperand.simplify();

    if (leftOperand instanceof Joker && (rightOperand instanceof Litteral || rightOperand instanceof Range)){
      return leftOperand;
    }

    if (rightOperand instanceof Joker && (leftOperand instanceof Litteral || leftOperand instanceof Range)){
      return rightOperand;
    }

    return this;
  }

  @Override
  public boolean isNotCompatible() {
    return leftOperand.isNotCompatible() && rightOperand.isNotCompatible();
  }

}
