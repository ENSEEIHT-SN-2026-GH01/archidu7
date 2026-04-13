package parser.automate;

import util.*;

public interface Automate<T> {

  Pair<T, Integer> exec(String t);

}
