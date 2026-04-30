package parser.automate;

public class LexingException extends RuntimeException {
  private int position;

  public LexingException(String errMessage, int position) {
    this.position = position;
    super(errMessage);
  }

  public int getPosition() {
    return position;
  }
}
