package util;

/**
 * A class representing a couple of values
 */
public class Pair<A, B> {
  private A a;
  private B b;

  public Pair(A arg0, B arg1) {
    this.a = arg0;
    this.b = arg1;
  }

  public A fst() {
    return a;
  }

  public B snd() {
    return b;
  }

  public static <A, B> Pair<A, B> pair(A a, B b) {
    return new Pair<>(a, b);
  }

  @Override
  public String toString() {
    return "(" + a + ", " + b + ")";
  }

}
