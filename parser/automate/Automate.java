package parser.automate;

import java.util.Optional;

public interface Automate<T> {

  Optional<T> exec(String t);

}
