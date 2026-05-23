package parser.automate;

import java.util.*;
import util.*;
import parser.lexer.Lexem;
import parser.regex.Regex;

public class AutomateNonDeterministeSansEps<T> implements Automate<T> {
  private Transitions<Integer, Integer> delta = new Transitions<>();
  private Set<Integer> entryPoints;
  private Map<Integer, T> etatsTerminaux;
  private Transitions<Integer, OptionalInt> superDelta;

  public static <T> AutomateNonDeterministeSansEps<T> fromList(List<Pair<Regex, T>> l) {
    return new AutomateNonDeterministeSansEps<T>(AutomateNonDeterministe.fromList(l));
  }

  private AutomateNonDeterministeSansEps(AutomateNonDeterministe<T> super_) {

    entryPoints = super_.getEntryPoints();
    etatsTerminaux = super_.getEtatsTerminaux();

    superDelta = super_.getDelta();

    // supprimmer les boucles d'epsilon-transitions
    for (int v : superDelta.keySet()) {
      superDelta.remove(v, OptionalInt.empty(), v);
    }

    /* calcul des transitions entrantes */
    Transitions<Integer, OptionalInt> deltaEntrant = superDelta.inverserTransitions();

    /*
     * pour chaque transition
     * s'il l'etiquette n'est pas epslion, on l'ajoute
     * sinon:
     * - prendre les transitions entrantes
     * - pour chaque entrante, on la met sur l'eps sortante
     * - supprimer les eps => une file pour les stocker ?
     */
    superDelta.forEach((transition) -> {
      if (transition instanceof Triplet(Integer depart, OptionalInt etiquette, Integer arrivee)) {
        if (etiquette.isEmpty()) {
          deltaEntrant.forEach((transitionEntrante) -> {
            if (transitionEntrante instanceof Triplet(Integer departEntrant, OptionalInt etiquetteEntrante, Integer arriveeEntrant)) {
              if (arriveeEntrant == depart) {
                if (etiquetteEntrante.isEmpty()) {
                  superDelta.add(departEntrant, etiquetteEntrante, arrivee);
                } else {
                  delta.add(departEntrant, etiquetteEntrante.getAsInt(), arrivee);
                }
              }
            }
          });
        }
      }
    });

    // rajouter les transitions de base
    superDelta.forEach((transition) -> {
      OptionalInt etiquette = transition.middle();
      if (!etiquette.isEmpty()) {
        delta.add(transition.first(), etiquette.getAsInt(), transition.last());
      }
    });

    // TODO Supprimer les etats non atteignables
    // TODO ajouter les sortants si besoin
  }

  protected Transitions<Integer, Integer> getDeltaSansEps() {
    return delta;
  }

  public Set<Integer> getEntryPoints() {
    return entryPoints;
  }

  public Map<Integer, T> getEtatsTerminaux() {
    return etatsTerminaux;
  }

  @Override
  public List<Lexem<T>> exec(String t) {
    throw new NotExecutableError();
  }

  @Override
  public String toString() {
    return "Automate non déterministe sans epsilon-transitions:\n\tInitiaux: " + entryPoints + "\n\tFinaux: "
        + etatsTerminaux + "\n\tTransitions: \n" + delta;
  }

}
