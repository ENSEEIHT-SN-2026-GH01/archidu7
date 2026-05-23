package util;

/**
 * A class representing a couple of values
 */
public record Pair<A, B>(A fst, B snd) {

  public A fst() {
    return fst;
  }

  public B snd() {
    return snd;
  }

  public static <A, B> Pair<A, B> pair(A a, B b) {
    return new Pair<>(a, b);
  }

  @Override
  public String toString() {
    return "(" + fst + ", " + snd + ")";
  }

}
