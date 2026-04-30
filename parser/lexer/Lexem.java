package parser.lexer;

/**
 * La classe de lexèmes qui contient tou ce que l'on devrait avoir besoin:
 *  - le token associé
 *  - l'index de départ dans le texte
 *  - la chaîne de caractères associée
 * 
 * Le reste de ce qu'il utilise est seulement utile pour son utilisation dans l'automate:
 *  - est ce qu'il est ignoré => est-ce qu'après avoir été trouvé, il est ajouté à la liste des lexèmes
 *  - quelle est la priorité de safe => un token est safe si son résultat est ignoré par rapport à d'autres
 *    lexèmes lors du match
 *    par exemple, on prèfèrerais matcher un module pour "module" plutot qu'in identifiant
 *    En plus, une priorité est ajoutée pour classer l'importance des lexèmes safe entre eux
 *   
 */
public class Lexem<T> {
  private boolean ignore = false;
  private int safePriority = -1;
  private T token;
  private int indexDepart;
  private String text;

  /**
   * Construit un lexème par défaut à partir de son token
   * @param token le token associé au lexème
   */
  public Lexem(T token){
    this.token = token;
  }

  /**
   * Crée une copie de {@code other}
   * @param other le lexème à copier
   */
  public Lexem(Lexem<T> other){
    this.ignore = other.ignore;
    this.safePriority = other.safePriority;
    this.token = other.token;
  }

  /**
   * Construit un lexème par défaut à partir de son token, en spécifiant s'il doit être ignoré
   * @param token le token associé au lexème
   * @param ignore {@code true} s'il est ignoré {@code false} sinon
   */
  public Lexem(T token, boolean ignore){
    this.token = token;
    this.ignore = ignore;
  }

  /**
   * Indique au lexème qu'il est safe tout en spécifiant sa priorité
   * @param priority un nombre positif
   * @throws IllegalArgumentException si priority < 0
   */
  public void setSafe(int priority) {
    if (priority < 0) throw new IllegalArgumentException("priority doit être supérieur à 0");
    safePriority = priority;
  }

  /**
   * Retire La propriété safe du lexème
   */
  public void setNotSafe(){
    safePriority = -1;
  }

  /**
   * Stocke le texte trouvé pour le Lexème ainsi que son index dans le texte
   * @param indexDepart l'index de {@code text} dans le texte
   * @param text le text trouvé
   */
  public void storeMatched(int indexDepart, String text){
    this.text = text;
    this.indexDepart = indexDepart;
  }

  /**
   * Récupère le token associé au lexème
   * @return le token identifié
   */
  public T getToken() {
    return token;
  }

  /**
   * Teste si le lexème est ignoré lors de la construction de la liste de lexèmes
   * @return {@code true} si le lexème est ignoré {@code false} sinon
   */
  public boolean isIgnored() {
    return ignore;
  }

  /**
   * Teste si le lexème est safe
   * @return {@code true} si le lexème est safe {@code false} sinon
   */
  public boolean isSafe() {
    return safePriority >= 0;
  }

  /**
   * Récupère la priorité de safe du lexème
   * @return un entier qui donne la priorité
   */
  public int getSafePriority(){
    return safePriority;
  }

  /**
   * Récupère l'index dans le texte initial de {@code getText()}
   * @return l'index dans le texte 
   */
  public int getIndexDepart() {
    return indexDepart;
  }

  /**
   * Récupère le texte trouvé lorsque le texte a été matché
   * @return le texte trouvé
   */
  public String getText() {
    return text;
  }

  /**
   * Calcule l'index de fin dans le texte
   * @return l'index de fin du lexème dans le texte
   */
  public int getIndexFin(){
    return indexDepart + text.length();
  }

  @Override
  public String toString() {
    return getToken().toString();
  }
}
