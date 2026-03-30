package util;

/**
 * A class representing a couple of values
 */
public class Pair<A, B> extends javafx.util.Pair<A, B> {

  public Pair(A arg0, B arg1) {
    super(arg0, arg1);
  }

  public A fst() {
    return super.getKey();
  }

  public B snd() {
    return super.getValue();
  }

  public static <A, B> Pair<A, B> pair(A a, B b) {
    return new Pair<>(a, b);
  }

}
