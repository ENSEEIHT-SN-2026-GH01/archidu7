package simulateur;

public class ErreurIndex extends RuntimeException {

  public ErreurIndex(int indexMax, int indexDemande, String cause) {
    super("Impossible d'acceder à l'index " + indexDemande + ". Les indexs sont compris entre 1 et " + indexMax
        + " inclus.");
  }

  public ErreurIndex(int index) {
    super("Impossible d'acceder à l'index " + index + ". Le lien n'a pas d'objet initialisé.");
  }
}
