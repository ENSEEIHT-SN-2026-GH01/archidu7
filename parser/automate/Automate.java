package parser.automate;

import java.util.*;

public interface Automate<T> {

  List<T> exec(String t) throws LexingException;

}
