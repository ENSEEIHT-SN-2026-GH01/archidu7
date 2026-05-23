package util;

public class Triplet<S, T, U> {
  public S first;
  public T middle;
  public U last;

  public Triplet(S first, T middle, U last) {
    this.first = first;
    this.middle = middle;
    this.last = last;
  }

  public static <S, T, U> Triplet<S, T, U> triplet(S first, T middle, U last) {
    return new Triplet<>(first, middle, last);
  }

  @Override
  public String toString() {
    return "("+ first +", "+ middle +", "+ last +")";
  }
}
