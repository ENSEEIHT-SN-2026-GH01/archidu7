# Token offset — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** Ajouter un champ `offset` (indice de position 0-indexé dans le texte source) aux `Token` et `Position`, en conservant `line`/`column` pour les messages d'erreur lisibles.

**Architecture:** Le parser ne construit aucun `Token` à partir du texte source — il reçoit les tokens d'un `Lexer` (actuellement juste une interface, implémentation à venir côté Erwan). L'offset est porté par le Token dès sa création par le lexer, puis propagé dans les `Position` de l'AST par le parser. Les 8 sites dans `Parser.java` qui construisent des `Position` à partir d'un Token sont les seuls points d'intégration.

**Tech Stack:** Java 25, JUnit 4.13.2. Tests existants : 107 verts. Scripts `build.sh` / `run.sh test`.

**Contrat demandé par Arthur (UI JavaFX)** : pouvoir repérer un token par un indice unique dans le texte source, pour surligner / positionner le curseur dans son éditeur.

**Décision** : ajouter `offset` en plus de `line`/`column`, pas à la place. Raison :
- `Parser.java:102-107` affiche la ligne source + caret en se basant sur `line`/`column`. Enlever ces champs forcerait à reconstruire une table des débuts de lignes à chaque erreur.
- Offset et (line, col) sont tous deux dérivables du source + position de lecture. Coût négligeable pour le lexer de les remplir simultanément.
- Arthur obtient exactement ce qu'il veut ; Erwan (lexer) et Mati (AST) gardent leurs handles existants.

---

## Fichiers touchés

| Fichier | Rôle | Modif |
|---|---|---|
| `parser/ll1/token/Token.java` | DTO token | +champ `offset`, constructeur 5-args, getter `getOffset()` |
| `parser/ll1/ast/Position.java` | Position AST | +champ `offset`, constructeur 3-args, getter `getOffset()` |
| `parser/ll1/parser/Parser.java` | Propage offset dans Position + EOF | 8 sites `new Position(t.getLine(), t.getColumn())` deviennent 3-args ; fallback EOF ligne 33 |
| `parser/ll1/parser/ParsingException.java` | Expose offset | +champ `offset`, +param constructeur, +getter |
| `tests/parser/ll1/token/TokenTest.java` | Tests Token | +test offset, MAJ des `new Token(...)` existants |
| `tests/parser/ll1/fixtures/TokenFixtures.java` | Fixtures tests | MAJ signatures `tok(...)` pour accepter offset |
| `tests/parser/ll1/parser/ParsingExceptionTest.java` | Tests exception | +assert offset |
| `README.md` | Doc équipe | Section "Pour Arthur" : documenter `getOffset()` |

**Pas touché** : l'interface `Lexer` (elle retourne déjà `List<Token>` — le contrat d'appel change juste sur le constructeur de Token, pas sur la signature de `tokenize`).

---

## Convention offset

- Type : `int`
- 0-indexé (offset 0 = premier caractère du source)
- Pointe sur le **premier caractère** du lexème
- Pour EOF construit par le parser en fallback (tokens vide ou dernier ≠ EOF) : offset = `source.length()` si source fourni, sinon offset du dernier token (ou 0 si liste vide).

---

## Task 1 : Token — champ offset

**Files:**
- Modify: `parser/ll1/token/Token.java` (entier)
- Test: `tests/parser/ll1/token/TokenTest.java`

- [ ] **Step 1 : Écrire le test qui échoue**

Modifier `tests/parser/ll1/token/TokenTest.java` — remplacer le contenu par :

```java
package tests.parser.ll1.token;

import org.junit.Test;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import static org.junit.Assert.*;

public class TokenTest {
    @Test
    public void gettersRestituentLesChamps() {
        Token t = new Token(TokenType.IDENTIFIER, "abc", 3, 12, 42);
        assertEquals(TokenType.IDENTIFIER, t.getType());
        assertEquals("abc", t.getValue());
        assertEquals(3, t.getLine());
        assertEquals(12, t.getColumn());
        assertEquals(42, t.getOffset());
    }

    @Test(expected = NullPointerException.class)
    public void typeNullInterdit() { new Token(null, "x", 1, 1, 0); }

    @Test
    public void toStringContientType() {
        assertTrue(new Token(TokenType.MODULE, "module", 1, 1, 0).toString().contains("MODULE"));
    }
}
```

- [ ] **Step 2 : Vérifier que la compilation échoue**

```bash
./build.sh test 2>&1 | tail -5
```
Expected : erreur de compilation (`constructor Token cannot be applied`, `cannot find symbol getOffset`).

- [ ] **Step 3 : Implémenter Token avec offset**

Réécrire `parser/ll1/token/Token.java` :

```java
package parser.ll1.token;

import java.util.Objects;

public final class Token {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;
    private final int offset;

    public Token(TokenType type, String value, int line, int column, int offset) {
        this.type = Objects.requireNonNull(type, "type");
        this.value = value;   // peut etre null pour EOF
        this.line = line;
        this.column = column;
        this.offset = offset;
    }

    public TokenType getType()   { return type; }
    public String    getValue()  { return value; }
    public int       getLine()   { return line; }
    public int       getColumn() { return column; }
    public int       getOffset() { return offset; }

    @Override public String toString() {
        return type + "(" + value + ")@" + line + ":" + column + "#" + offset;
    }
}
```

- [ ] **Step 4 : Le parser et les fixtures ne compilent pas encore — on les met à jour dans les Tasks 2-3-4. Commit Token + son test seulement :**

```bash
git add parser/ll1/token/Token.java tests/parser/ll1/token/TokenTest.java
git commit -m "feat(token): ajoute champ offset (indice dans le source) au Token"
```

---

## Task 2 : Fixtures de tests

**Files:**
- Modify: `tests/parser/ll1/fixtures/TokenFixtures.java`

- [ ] **Step 1 : Mettre à jour les 3 helpers pour accepter offset**

Remplacer le contenu de `tests/parser/ll1/fixtures/TokenFixtures.java` :

```java
package tests.parser.ll1.fixtures;

import parser.ll1.token.Token;
import parser.ll1.token.TokenType;

public final class TokenFixtures {
    public static Token tok(TokenType t) { return new Token(t, null, 1, 1, 0); }
    public static Token tok(TokenType t, String v) { return new Token(t, v, 1, 1, 0); }
    public static Token tok(TokenType t, String v, int line, int col) { return new Token(t, v, line, col, 0); }
    public static Token tok(TokenType t, String v, int line, int col, int offset) { return new Token(t, v, line, col, offset); }
}
```

Note : on garde les 3 signatures existantes (offset=0 par défaut) pour ne pas casser les appels existants, et on ajoute une 4e signature pour les futurs tests qui voudront vérifier l'offset.

- [ ] **Step 2 : Vérifier que les tests compilent (le parser reste à mettre à jour)**

```bash
javac -d /tmp/fixcheck -cp bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar tests/parser/ll1/fixtures/TokenFixtures.java 2>&1
```
Expected : compilation OK (fichier isolé).

- [ ] **Step 3 : Commit**

```bash
git add tests/parser/ll1/fixtures/TokenFixtures.java
git commit -m "test(fixtures): TokenFixtures accepte offset (defaut 0 pour retrocompat)"
```

---

## Task 3 : Position — champ offset

**Files:**
- Modify: `parser/ll1/ast/Position.java`
- Test: à créer `tests/parser/ll1/ast/PositionTest.java`

- [ ] **Step 1 : Créer le test**

Créer `tests/parser/ll1/ast/PositionTest.java` :

```java
package tests.parser.ll1.ast;

import org.junit.Test;
import parser.ll1.ast.Position;
import static org.junit.Assert.*;

public class PositionTest {
    @Test
    public void gettersRestituentLesChamps() {
        Position p = new Position(3, 12, 42);
        assertEquals(3, p.getLine());
        assertEquals(12, p.getColumn());
        assertEquals(42, p.getOffset());
    }

    @Test
    public void equalsComparelesTroisChamps() {
        assertEquals(new Position(1, 2, 3), new Position(1, 2, 3));
        assertNotEquals(new Position(1, 2, 3), new Position(1, 2, 4));
    }

    @Test
    public void toStringContientLesTroisChamps() {
        assertEquals("1:2#3", new Position(1, 2, 3).toString());
    }
}
```

- [ ] **Step 2 : Vérifier que la compilation échoue**

```bash
./build.sh test 2>&1 | tail -5
```
Expected : erreur (`constructor Position cannot be applied`, `cannot find symbol getOffset`).

- [ ] **Step 3 : Implémenter Position avec offset**

Réécrire `parser/ll1/ast/Position.java` :

```java
package parser.ll1.ast;
import java.util.Objects;

public final class Position {
    private final int line, column, offset;
    public Position(int line, int column, int offset) {
        this.line = line; this.column = column; this.offset = offset;
    }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public int getOffset() { return offset; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position) o; return p.line == line && p.column == column && p.offset == offset;
    }
    @Override public int hashCode() { return Objects.hash(line, column, offset); }
    @Override public String toString() { return line + ":" + column + "#" + offset; }
}
```

- [ ] **Step 4 : Le parser ne compile toujours pas (utilisation 2-args de Position). On le fera dans Task 4. Commit Position + son test :**

```bash
git add parser/ll1/ast/Position.java tests/parser/ll1/ast/PositionTest.java
git commit -m "feat(ast): ajoute champ offset a Position"
```

---

## Task 4 : Parser — propager l'offset

**Files:**
- Modify: `parser/ll1/parser/Parser.java`

8 sites à modifier (tous des `new Position(t.getLine(), t.getColumn())`) + 1 site EOF (ligne 33 actuelle) + 1 fabrique `error()` qui passe line/column à `ParsingException` (à voir après Task 5).

- [ ] **Step 1 : Ajouter offset à EOF fallback**

Modifier `parser/ll1/parser/Parser.java` lignes ~29-34 — remplacer :

```java
        if (ts.isEmpty() || ts.get(ts.size() - 1).getType() != TokenType.EOF) {
            Token last = ts.isEmpty() ? null : ts.get(ts.size() - 1);
            int l = last == null ? 1 : last.getLine();
            int c = last == null ? 1 : last.getColumn();
            ts.add(new Token(TokenType.EOF, null, l, c));
        }
```

par :

```java
        if (ts.isEmpty() || ts.get(ts.size() - 1).getType() != TokenType.EOF) {
            Token last = ts.isEmpty() ? null : ts.get(ts.size() - 1);
            int l = last == null ? 1 : last.getLine();
            int c = last == null ? 1 : last.getColumn();
            int off = source != null ? source.length()
                    : (last == null ? 0 : last.getOffset() + (last.getValue() == null ? 0 : last.getValue().length()));
            ts.add(new Token(TokenType.EOF, null, l, c, off));
        }
```

Note : cette construction d'EOF se fait dans le constructeur, qui reçoit `source`. Donc `source` est déjà connu ici — vérifier que `this.source = source;` est fait APRÈS la construction des tokens. Si non, réordonner ou utiliser `source` (param local) directement — oui, c'est le param local, donc OK.

- [ ] **Step 2 : Remplacer les 8 `new Position(t.getLine(), t.getColumn())` par la forme 3-args**

Dans `Parser.java`, rechercher chaque `new Position(` et ajouter l'offset du token correspondant.

Sites exacts (lignes dans le fichier actuel — vérifier avec `grep -n "new Position" parser/ll1/parser/Parser.java` avant édition) :

```java
// Ligne ~123
Position p = new Position(id.getLine(), id.getColumn(), id.getOffset());

// Ligne ~158
Position p = new Position(t.getLine(), t.getColumn(), t.getOffset());

// Ligne ~286
Position pos = new Position(mod.getLine(), mod.getColumn(), mod.getOffset());

// Ligne ~316
Position pos = new Position(id.getLine(), id.getColumn(), id.getOffset());

// Ligne ~330
Position p = new Position(t.getLine(), t.getColumn(), t.getOffset());

// Ligne ~348
Position p = new Position(mk.getLine(), mk.getColumn(), mk.getOffset());

// Ligne ~357
Position ep = new Position(b1.getLine(), b1.getColumn(), b1.getOffset());

// Ligne ~369
Position p = new Position(fk.getLine(), fk.getColumn(), fk.getOffset());

// Ligne ~386
Position hp = new Position(startTok.getLine(), startTok.getColumn(), startTok.getOffset());

// Ligne ~417
Position p = new Position(start.getLine(), start.getColumn(), start.getOffset());
```

Commande de vérification exhaustive avant de commit :

```bash
grep -n "new Position(" parser/ll1/parser/Parser.java
```
Expected : chaque occurrence doit avoir 3 arguments (pas 2).

- [ ] **Step 3 : Compiler**

```bash
./build.sh
```
Expected : succès (ParsingException reste 2-args pour l'instant, le `throw error(...)` ne passe pas encore d'offset — OK, on le fera dans Task 5).

- [ ] **Step 4 : Commit**

```bash
git add parser/ll1/parser/Parser.java
git commit -m "feat(parser): propage l offset du Token vers Position et EOF fallback"
```

---

## Task 5 : ParsingException — champ offset

**Files:**
- Modify: `parser/ll1/parser/ParsingException.java`
- Modify: `parser/ll1/parser/Parser.java` (sites `error(...)`)
- Test: `tests/parser/ll1/parser/ParsingExceptionTest.java`

- [ ] **Step 1 : MAJ le test**

Modifier `tests/parser/ll1/parser/ParsingExceptionTest.java` — localiser le test qui instancie `ParsingException` directement et ajouter l'assert offset. Exemple de forme attendue :

```java
@Test
public void getters() {
    ParsingException e = new ParsingException(
        ErrorCode.UNEXPECTED_TOKEN, 3, 12, 42,
        java.util.Set.of(TokenType.SEMICOLON), TokenType.IDENTIFIER,
        new java.util.ArrayDeque<>(), "", null);
    assertEquals(ErrorCode.UNEXPECTED_TOKEN, e.getCode());
    assertEquals(3, e.getLine());
    assertEquals(12, e.getColumn());
    assertEquals(42, e.getOffset());
}
```

Ajuster les autres tests du fichier qui instancient `ParsingException` pour passer `0` comme offset si la valeur importe peu.

- [ ] **Step 2 : Vérifier que ça casse**

```bash
./build.sh test 2>&1 | tail -10
```
Expected : erreur `constructor ParsingException cannot be applied` ou `cannot find symbol getOffset`.

- [ ] **Step 3 : Implémenter**

Modifier `parser/ll1/parser/ParsingException.java` — ajouter `offset` :

```java
package parser.ll1.parser;

import parser.ll1.token.TokenType;
import java.util.*;

public class ParsingException extends RuntimeException {
    private final ErrorCode code;
    private final int line, column, offset;
    private final Set<TokenType> expected;
    private final TokenType actual;
    private final List<String> grammarContext;
    private final String sourceSnippet;
    private final String suggestion;

    public ParsingException(ErrorCode code, int line, int column, int offset,
                            Set<TokenType> expected, TokenType actual,
                            Deque<String> grammarStack, String sourceSnippet, String suggestion) {
        super(build(code, line, column, expected, actual, grammarStack, sourceSnippet, suggestion));
        this.code = code;
        this.line = line;
        this.column = column;
        this.offset = offset;
        this.expected = expected == null ? Set.of()
            : Collections.unmodifiableSet(new LinkedHashSet<>(expected));
        this.actual = actual;
        this.grammarContext = grammarStack == null ? List.of() : List.copyOf(grammarStack);
        this.sourceSnippet = sourceSnippet == null ? "" : sourceSnippet;
        this.suggestion = suggestion;
    }

    public ErrorCode getCode() { return code; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public int getOffset() { return offset; }
    public Set<TokenType> getExpected() { return expected; }
    public TokenType getActual() { return actual; }
    public List<String> getGrammarContext() { return grammarContext; }
    public String getSourceSnippet() { return sourceSnippet; }
    public Optional<String> getSuggestion() { return Optional.ofNullable(suggestion); }

    private static String build(ErrorCode c, int l, int col, Set<TokenType> exp, TokenType act,
                                Deque<String> stack, String snip, String sug) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ligne ").append(l).append(", colonne ").append(col)
          .append(" [").append(c).append("]");
        if (exp != null && !exp.isEmpty()) sb.append(" : attendu ").append(exp);
        if (act != null) sb.append(", recu ").append(act);
        if (snip != null && !snip.isEmpty()) sb.append("\n  ").append(snip);
        if (stack != null && !stack.isEmpty()) sb.append("\nContexte : ").append(String.join(" > ", stack));
        if (sug != null) sb.append("\nSuggestion : ").append(sug);
        return sb.toString();
    }
}
```

- [ ] **Step 4 : MAJ les appelants de `new ParsingException(...)` dans Parser.java**

Rechercher les 4 sites dans `Parser.java` :

```bash
grep -n "new ParsingException\|throw new ParsingException" parser/ll1/parser/Parser.java
```

Pour chacun, insérer `t.getOffset()` (ou équivalent suivant le nom du token local) en 4ᵉ argument, après `column`. Exemple — ligne ~96 :

```java
return new ParsingException(code, t.getLine(), t.getColumn(), t.getOffset(),
    expected, t.getType(), new ArrayDeque<>(grammarStack), snippet(t), null);
```

Et ligne ~171 / ~335 (BIT_OUT_OF_RANGE) :

```java
throw new ParsingException(ErrorCode.BIT_OUT_OF_RANGE, t.getLine(), t.getColumn(), t.getOffset(),
    ... );
```

Lister via `grep` avant édition pour être exhaustif.

- [ ] **Step 5 : Compiler + lancer tests**

```bash
./build.sh test && ./run.sh
```
Expected : 107 tests verts + les nouveaux tests (Token +1 : getOffset ; Position +3 ; ParsingException +1).

- [ ] **Step 6 : Commit**

```bash
git add parser/ll1/parser/ParsingException.java parser/ll1/parser/Parser.java tests/parser/ll1/parser/ParsingExceptionTest.java
git commit -m "feat(parser): ajoute offset a ParsingException (propagation depuis Token)"
```

---

## Task 6 : Documenter dans README pour Arthur

**Files:**
- Modify: `README.md`

- [ ] **Step 1 : Localiser la section "Pour Arthur"**

```bash
grep -n "Pour Arthur\|ParsingException\|getLine\|getColumn" README.md
```

- [ ] **Step 2 : Ajouter un paragraphe "Offset source"**

Dans la section "Pour Arthur", après le bloc sur ParsingException, ajouter :

```markdown
### Localisation des tokens et nœuds AST

Chaque `Token` et chaque `Position` d'AST expose trois repères complémentaires :

- `getLine()` / `getColumn()` — 1-indexés, pour afficher un message à l'utilisateur.
- `getOffset()` — indice 0-indexé dans la chaîne source (premier caractère du lexème).
  Directement utilisable pour surligner dans un `TextArea` / `CodeArea` JavaFX
  via `selectRange(offset, offset + length)`.

`ParsingException` expose également `getOffset()` pour positionner précisément
le curseur sur le token fautif.
```

- [ ] **Step 3 : Commit**

```bash
git add README.md
git commit -m "docs(readme): documente getOffset() des Token/Position/ParsingException"
```

---

## Task 7 : Vérification finale

- [ ] **Step 1 : Build + tests complets**

```bash
./build.sh test
./run.sh 2>&1 | tail -20
```
Expected : toutes les suites JUnit en OK. Compter : au moins 107 tests initiaux + les nouveaux (≈111 total).

- [ ] **Step 2 : Grep de contrôle — aucune Position 2-args résiduelle**

```bash
grep -rn "new Position(" parser/ tests/ | grep -v "new Position([^,]*,[^,]*,[^)]*)"
```
Expected : sortie vide.

- [ ] **Step 3 : Grep de contrôle — aucun Token 4-args résiduel**

```bash
grep -rn "new Token(" parser/ tests/ | grep -v "new Token([^,]*,[^,]*,[^,]*,[^,]*,[^)]*)"
```
Expected : sortie vide.

- [ ] **Step 4 : Résumé des commits**

```bash
git log --oneline main..HEAD
```
Expected : 6 commits (un par Task 1-6).

---

## Self-review notes

- **Spec coverage** : Token offset ✅ (Task 1), Position offset ✅ (Task 3), Parser propagation ✅ (Task 4), ParsingException offset ✅ (Task 5), doc ✅ (Task 6), vérif ✅ (Task 7).
- **YAGNI** : on ne touche pas à `Lexer.tokenize` (interface inchangée — le contrat c'est « les Tokens retournés portent l'offset »). On ne rajoute pas de méthode utilitaire offset↔line:col (Arthur ou Erwan le feront s'ils en ont besoin).
- **Pas de retrocompat sur le constructeur** : `new Token(type, val, l, c)` devient `new Token(type, val, l, c, offset)`. Impact : `TokenFixtures` (Task 2) + tous les tests qui instancient directement (rares). Aucun lexer concret n'existe dans la branche, donc pas d'impact externe. Erwan sera prévenu.
- **Breaking change intentionnel** : signalé dans le README pour coordination équipe.
