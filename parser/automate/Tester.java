package parser.automate;

import java.util.*;

import parser.regex.*;
import util.*;

public class Tester {
  public static void main(String[] args) {
    String resultat1 = "res1";
    String chaineTest = "abc";
    List<Pair<Regex, String>> l = new LinkedList<>();
    l.add(Pair.pair(Builder.parseRegex(chaineTest), ""));
    AutomateDeterministe<String> a = AutomateDeterministe.fromList(l);
    System.out.println(a.exec(chaineTest));
  }
}
