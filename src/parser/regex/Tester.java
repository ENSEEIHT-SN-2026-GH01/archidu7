package parser.regex;

public class Tester {
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("A regex expression is needed");
      return;
    }

    Regex r = Builder.parseRegex(args[0]);
    System.out.println(r);
  }
}
