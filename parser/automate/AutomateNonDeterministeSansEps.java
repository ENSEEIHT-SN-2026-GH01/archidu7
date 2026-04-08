package parser.automate;

import java.util.*;

import parser.regex.Regex;

public abstract class AutomateNonDeterministeSansEps<T> extends AutomateNonDeterministe<T> {
  private Transitions<Integer, Integer> delta = new Transitions<>();
  private Set<Integer> entryPoints;
  private Transitions<Integer, OptionalInt> superDelta;

  public AutomateNonDeterministeSansEps(Regex r, T lexeme) {
    super(r, lexeme);

    entryPoints = getEntryPoint();

    superDelta = super.getDelta();

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
      int depart = transition.first;
      OptionalInt etiquette = transition.middle;
      int arrivee = transition.last;

      if (etiquette.isEmpty()) {
        deltaEntrant.forEach((transitionEntrante) -> {
          int departEntrant = transitionEntrante.first;
          OptionalInt etiquetteEntrante = transitionEntrante.middle;
          int arriveeEntrant = transitionEntrante.last;

          if (arriveeEntrant == depart) {
            if (etiquetteEntrante.isEmpty()) {
              superDelta.add(departEntrant, etiquetteEntrante, arrivee);
            } else {
              delta.add(departEntrant, etiquetteEntrante.getAsInt(), arrivee);
            }
          }
        });
      }
    });

    // rajouter les transitions de base
    superDelta.forEach((transition) -> {
      int depart = transition.first;
      OptionalInt etiquette = transition.middle;
      int arrivee = transition.last;

      if (!etiquette.isEmpty()){
        delta.add(depart, etiquette.getAsInt(), arrivee);
      }
    });

    // TODO Supprimer les etats non atteignables
  }

  protected Transitions<Integer, Integer> getDeltaSansEps() {
    return delta;
  }

  public Set<Integer> getEntryPoints() {
    return entryPoints;
  }

}
