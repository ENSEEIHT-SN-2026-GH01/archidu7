# Plan d'implémentation — Conversion CST → Module (S1)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal :** Implémenter `parser.conversion.Conversion.convert(CstNode) → simulateur.Module` sur le scope S1 (combinatoire scalaire stricte), prêt à être consommé par `simulateur.FileSimulateur`.

**Architecture :** Visiteur top-down sur le CST produit par `CstParser`. Cinq fichiers Java dans `parser/conversion/` (`Conversion`, `ModuleBuilder`, `ExpressionBuilder`, `Names`, `ConversionException`). Aucun couplage circulaire. TDD strict, JUnit 4, tests dans `tests/parser/conversion/`.

**Tech Stack :** Java 25, JUnit 4.13.2, Hamcrest 1.3. Build/test via `./build.sh test` et `./run.sh`.

**Spec source :** `docs/specs/2026-05-06-cst-conversion-design.md`

---

## Conventions générales

**Notation SHDL** (rappel grammaire/lexer) :
- `=` AssignOp, `:=` MemAssignOp
- `*` AndOp (Star), `+` OrOp, `/` NotOp, `&` ConcatOp
- `module … end module` délimite un module ; `(...)` les paramètres ; `[N..M]`/`[N:M]` les ranges vecteur ; `.0` / `.1` les BitField

**Lancer un test isolé** :
```bash
./build.sh test \
  && java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
       org.junit.runner.JUnitCore tests.parser.conversion.<NomTest>
```

**Lancer toute la batterie** :
```bash
./run.sh
```

**Convention de commit** : `<type>(<scope>): <message>` cohérent avec l'historique. Co-authored par Claude.

---

## Structure des fichiers cibles

```
parser/conversion/
├── ConversionException.java   ~30 lignes
├── Names.java                  ~25 lignes
├── ExpressionBuilder.java      ~130 lignes
├── ModuleBuilder.java          ~80 lignes
└── Conversion.java             ~30 lignes

tests/parser/conversion/
├── ConversionExceptionTest.java
├── NamesTest.java
├── ExpressionBuilderTest.java
├── ModuleBuilderTest.java
├── ConversionErrorsTest.java
├── ConversionIntegrationTest.java
└── ConversionSmokeTest.java
```

---

## Task 1 — Pré-requis externes (côté Erwan)

**Files :**
- Modify (côté Erwan) : `simulateur/Erwan/Erwan.java:145`
- Modify (côté Erwan) : `simulateur/Module.java`

**Cette task ne produit PAS de commit dans la branche d'Alexis.** C'est un point de coordination Discord. Tant qu'elle n'est pas verte, ne pas démarrer Task 2.

- [ ] **Step 1 : envoyer le message Discord à Erwan**

Message à copier :

> Salut Erwan, j'ai 3 points pour démarrer la conversion :
> 1. Bug `Erwan.OR()` ligne 145 : tu retournes `new Erwan(Operation.AND, …)` alors que ça devrait être `Operation.OR`. Sinon tout `+` simulera comme un `*`.
> 2. `Module` ctor 2 (ligne 22-35) ne compile pas : `this(plans, Entrees, Sorties, branchements)` arrive après deux `for`, alors que Java exige `this(...)` en première instruction. Soit tu déplaces la logique dans une méthode privée `splitBranchements`, soit tu supprimes le ctor 2 si non utilisé.
> 3. Pour l'itération S1 je propose : `Signal_Subset_Opt` doit être ε strictement (pas de `[...]` ni en LHS, ni en RHS, ni en Param). Ça matche le scope que `FileSimulateur` sait simuler. OK pour toi ?
>
> Une fois tes points 1 et 2 mergés sur `interpretation`, je merge dans ma branche et j'implémente.

- [ ] **Step 2 : attendre le push Erwan**

Vérifier :
```bash
git fetch origin
git log origin/interpretation -10 --oneline
```

Attendre un commit dont le message mentionne le fix `OR()` ET le ctor `Module`. Sinon ne pas continuer.

- [ ] **Step 3 : noter l'accord d'Erwan sur la définition "scalaire = SS_OPT vide stricte"**

Capture d'écran ou copie du message dans `docs/specs/` au besoin. Pas obligatoire de versionner mais doit exister.

---

## Task 2 — Merge `origin/interpretation` et vérification non-régression parser

**Files :**
- Modify (résolution conflits) : `parser/ll1/tabledriven/cst/CstInternal.java`, `parser/ll1/tabledriven/cst/CstLeaf.java`

- [ ] **Step 1 : fetch + merge**

```bash
git fetch origin
git merge --no-ff origin/interpretation
```

Si les conflits ne touchent **que** `CstInternal.java` et `CstLeaf.java`, continuer. Sinon, abandonner (`git merge --abort`) et investiguer.

- [ ] **Step 2 : résoudre conflits CST en gardant la version locale**

```bash
git checkout --ours parser/ll1/tabledriven/cst/CstInternal.java
git checkout --ours parser/ll1/tabledriven/cst/CstLeaf.java
git add parser/ll1/tabledriven/cst/CstInternal.java parser/ll1/tabledriven/cst/CstLeaf.java
```

Vérifier qu'aucun autre fichier n'est en conflit :
```bash
git status
```

S'il y a d'autres conflits, **les résoudre manuellement** (lecture comparative + Edit). Ne pas valider de fichier qui ne compile pas.

- [ ] **Step 3 : finaliser le merge**

```bash
git commit --no-edit
```

- [ ] **Step 4 : compiler tout (parser + tests + code Erwan/Mati nouveau)**

```bash
rm -rf bin/* && ./build.sh test
```

Expected : `>> Build OK (N classes)` sans erreur. Si erreur de compil dans `simulateur/`, retour Task 1 — Erwan n'a pas fini son fix.

- [ ] **Step 5 : lancer la suite parser, vérifier 100% vert**

```bash
./run.sh 2>&1 | tail -40
```

Expected : aucune ligne `FAIL`. Si rouge, comparer avec `git log feature/parser-ll1-table-driven^^..feature/parser-ll1-table-driven` pour identifier le delta du merge.

- [ ] **Step 6 : aucun commit à créer (le merge est déjà committé par git merge)**

---

## Task 3 — `ConversionException`

**Files :**
- Create : `parser/conversion/ConversionException.java`
- Test : `tests/parser/conversion/ConversionExceptionTest.java`

- [ ] **Step 1 : écrire le test**

Créer `tests/parser/conversion/ConversionExceptionTest.java` :

```java
package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;

public class ConversionExceptionTest {

    @Test
    public void exposeOffsetNodeKindAndReason() {
        ConversionException ex = new ConversionException(
            42, "Signal", Reason.VECTOR_SUBSET_NOT_SUPPORTED,
            "Vecteur non supporte en S1 (offset 42)"
        );
        assertEquals(42, ex.offset());
        assertEquals("Signal", ex.nodeKind());
        assertEquals(Reason.VECTOR_SUBSET_NOT_SUPPORTED, ex.reason());
        assertEquals("Vecteur non supporte en S1 (offset 42)", ex.getMessage());
    }

    @Test
    public void isRuntimeException() {
        ConversionException ex = new ConversionException(0, "X", Reason.MALFORMED_CST, "x");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    public void allReasonsExposedByEnum() {
        Reason[] reasons = Reason.values();
        assertEquals(7, reasons.length);
    }
}
```

- [ ] **Step 2 : lancer le test, vérifier qu'il échoue à la compilation**

```bash
./build.sh test
```

Expected : `cannot find symbol: class ConversionException`.

- [ ] **Step 3 : implémenter `ConversionException`**

Créer `parser/conversion/ConversionException.java` :

```java
package parser.conversion;

public class ConversionException extends RuntimeException {

    public enum Reason {
        VECTOR_SUBSET_NOT_SUPPORTED,
        CONCAT_NOT_SUPPORTED,
        MEMORY_ASSIGNMENT_NOT_SUPPORTED,
        MODULE_CALL_NOT_SUPPORTED,
        LITERAL_IN_RHS_NOT_SUPPORTED,
        DUPLICATE_LHS,
        MALFORMED_CST
    }

    private final int offset;
    private final String nodeKind;
    private final Reason reason;

    public ConversionException(int offset, String nodeKind, Reason reason, String message) {
        super(message);
        this.offset = offset;
        this.nodeKind = nodeKind;
        this.reason = reason;
    }

    public int offset()        { return offset; }
    public String nodeKind()   { return nodeKind; }
    public Reason reason()     { return reason; }
}
```

- [ ] **Step 4 : compiler + lancer le test, vérifier vert**

```bash
./build.sh test \
  && java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
       org.junit.runner.JUnitCore tests.parser.conversion.ConversionExceptionTest
```

Expected : `OK (3 tests)`.

- [ ] **Step 5 : commit**

```bash
git add parser/conversion/ConversionException.java tests/parser/conversion/ConversionExceptionTest.java
git commit -m "feat(conversion): ConversionException avec offset/nodeKind/reason

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 4 — `Names` (helpers d'extraction scalaire)

**Files :**
- Create : `parser/conversion/Names.java`
- Test : `tests/parser/conversion/NamesTest.java`

`Names` exporte deux helpers statiques :
- `extractScalarFromSignalNT(CstNode signalNT) → String` : prend un `CstInternal(NonTerminal.Signal, …)`, renvoie le texte de l'`Identifiant` enfant. Lève `ConversionException(VECTOR_SUBSET_NOT_SUPPORTED)` si le `Signal_Subset_Opt` enfant n'est pas ε.
- `extractScalarFromIdAndSubset(CstNode idLeaf, CstNode subsetOpt) → String` : variante pour le LHS d'`Instance` où Identifiant et Signal_Subset_Opt sont des frères directs sous Operation. Vérifie scalarité, renvoie le texte d'`idLeaf`.

- [ ] **Step 1 : écrire le test**

Créer `tests/parser/conversion/NamesTest.java` :

```java
package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Names;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;

public class NamesTest {

    private static CstNode firstSignal(String src) {
        CstNode root = CstParser.parse(src);
        // Start -> Module -> ... -> Param -> Signal
        return root.first(NonTerminal.Module).orElseThrow()
                   .first(NonTerminal.Param).orElseThrow()
                   .first(NonTerminal.Signal).orElseThrow();
    }

    @Test
    public void extractScalarFromSignalNT_scalarSignal_returnsName() {
        CstNode sig = firstSignal("module m (alpha) c = alpha end module");
        assertEquals("alpha", Names.extractScalarFromSignalNT(sig));
    }

    @Test(expected = ConversionException.class)
    public void extractScalarFromSignalNT_vectorSignal_throws() {
        CstNode sig = firstSignal("module m (alpha[0..3]) c = alpha end module");
        Names.extractScalarFromSignalNT(sig);
    }

    @Test
    public void extractScalarFromSignalNT_vectorSignal_reasonIsVector() {
        CstNode sig = firstSignal("module m (alpha[0..3]) c = alpha end module");
        try {
            Names.extractScalarFromSignalNT(sig);
            fail("expected ConversionException");
        } catch (ConversionException ex) {
            assertEquals(Reason.VECTOR_SUBSET_NOT_SUPPORTED, ex.reason());
        }
    }
}
```

- [ ] **Step 2 : lancer le test, vérifier qu'il échoue à la compilation**

```bash
./build.sh test
```

Expected : `cannot find symbol: class Names`.

- [ ] **Step 3 : implémenter `Names`**

Créer `parser/conversion/Names.java` :

```java
package parser.conversion;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;

public final class Names {

    private Names() {}

    public static String extractScalarFromSignalNT(CstNode signalNT) {
        if (!(signalNT instanceof CstInternal sig) || sig.nt() != NonTerminal.Signal) {
            throw new ConversionException(
                signalNT.startOffset(), String.valueOf(signalNT.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Attendu un noeud NT Signal");
        }
        CstNode subset = sig.first(NonTerminal.Signal_Subset_Opt).orElseThrow(() ->
            new ConversionException(
                sig.startOffset(), "Signal",
                ConversionException.Reason.MALFORMED_CST,
                "Signal sans Signal_Subset_Opt enfant"));
        if (subset instanceof CstInternal sub && !sub.children().isEmpty()) {
            throw new ConversionException(
                sig.startOffset(), "Signal",
                ConversionException.Reason.VECTOR_SUBSET_NOT_SUPPORTED,
                "Vecteur non supporte en S1 (offset " + sig.startOffset() + ")");
        }
        CstNode id = sig.first(new Terminal(Token.Identifiant)).orElseThrow(() ->
            new ConversionException(
                sig.startOffset(), "Signal",
                ConversionException.Reason.MALFORMED_CST,
                "Signal sans Identifiant enfant"));
        return ((CstLeaf) id).lexem().getText();
    }

    public static String extractScalarFromIdAndSubset(CstNode idLeaf, CstNode subsetOpt) {
        if (subsetOpt instanceof CstInternal sub && !sub.children().isEmpty()) {
            throw new ConversionException(
                idLeaf.startOffset(), "Identifiant",
                ConversionException.Reason.VECTOR_SUBSET_NOT_SUPPORTED,
                "Vecteur non supporte en S1 (offset " + idLeaf.startOffset() + ")");
        }
        return ((CstLeaf) idLeaf).lexem().getText();
    }
}
```

- [ ] **Step 4 : compiler + lancer le test, vérifier vert**

```bash
./build.sh test \
  && java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
       org.junit.runner.JUnitCore tests.parser.conversion.NamesTest
```

Expected : `OK (3 tests)`.

- [ ] **Step 5 : commit**

```bash
git add parser/conversion/Names.java tests/parser/conversion/NamesTest.java
git commit -m "feat(conversion): Names.extractScalarFromSignalNT et variante LHS

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 5 — `ExpressionBuilder` (TDD construct par construct)

**Files :**
- Create : `parser/conversion/ExpressionBuilder.java`
- Test : `tests/parser/conversion/ExpressionBuilderTest.java`

L'API : `ExpressionBuilder.build(CstNode sumOfTermsCompoundNode) → Erwan`. La méthode dispatche vers les visiteurs internes par NT.

**Règles d'aplatissement (rappel spec §5.2 et §5.3)** :
- `Or_Operand_Star = ε` ou `And_Operand_Star = ε` ⇒ skip wrapper, retour direct.
- `a + b + c` ⇒ `OR([a, b, c])` n-aire (PAS `OR(a, OR(b, c))`).
- `a + b * c` ⇒ `OR(a, AND(b, c))` (précédence préservée).

### 5.1 — Squelette + premier construct (`Signal` simple)

- [ ] **Step 1 : écrire le test pour `a` seul**

Créer `tests/parser/conversion/ExpressionBuilderTest.java` :

```java
package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.ExpressionBuilder;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Erwan.Erwan;
import simulateur.Erwan.Operation;

public class ExpressionBuilderTest {

    /** Extrait le SumOfTermsCompound RHS de la 1re Instance d'un module. */
    private static CstNode rhs(String moduleSrc) {
        CstNode root = CstParser.parse(moduleSrc);
        CstNode module = root.first(NonTerminal.Module).orElseThrow();
        CstNode instancePlus = module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstNode instance = instancePlus.first(NonTerminal.Instance).orElseThrow();
        CstNode op = instance.first(NonTerminal.Operation).orElseThrow();
        CstNode assignment = op.first(NonTerminal.Assignment).orElseThrow();
        CstNode sigA = assignment.first(NonTerminal.SignalAssignment).orElseThrow();
        return sigA.first(NonTerminal.SumOfTermsCompound).orElseThrow();
    }

    @Test
    public void singleSignal_a() {
        Erwan e = ExpressionBuilder.build(rhs("module m (a) c = a end module"));
        assertEquals(Operation.LITTERAL, e.Op);
        assertEquals("a", e.Nom());
    }
}
```

- [ ] **Step 2 : lancer le test, échec à la compilation**

```bash
./build.sh test
```

Expected : `cannot find symbol: class ExpressionBuilder`.

- [ ] **Step 3 : implémenter le squelette `ExpressionBuilder` + cas Signal**

Créer `parser/conversion/ExpressionBuilder.java` :

```java
package parser.conversion;

import java.util.ArrayList;
import java.util.List;
import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Erwan.Erwan;

public final class ExpressionBuilder {

    private ExpressionBuilder() {}

    public static Erwan build(CstNode sumOfTermsCompound) {
        return buildSOTC(sumOfTermsCompound);
    }

    private static Erwan buildSOTC(CstNode node) {
        if (!(node instanceof CstInternal sotc) || sotc.nt() != NonTerminal.SumOfTermsCompound) {
            throw new ConversionException(node.startOffset(), String.valueOf(node.symbol()),
                ConversionException.Reason.MALFORMED_CST, "Attendu SumOfTermsCompound");
        }
        CstNode concat = sotc.first(NonTerminal.Concat_SumOfTerms_Star).orElseThrow();
        if (concat instanceof CstInternal cs && !cs.children().isEmpty()) {
            throw new ConversionException(concat.startOffset(), "Concat_SumOfTerms_Star",
                ConversionException.Reason.CONCAT_NOT_SUPPORTED,
                "Concatenation '&' non supportee en S1 (offset " + concat.startOffset() + ")");
        }
        CstNode sot = sotc.first(NonTerminal.SumOfTerms).orElseThrow();
        return buildSOT(sot);
    }

    private static Erwan buildSOT(CstNode node) {
        CstInternal sot = (CstInternal) node;
        Erwan first = buildTerm(sot.first(NonTerminal.Term).orElseThrow());
        List<Erwan> operands = collectOrOperands(sot.first(NonTerminal.Or_Operand_Star).orElseThrow());
        if (operands.isEmpty()) return first;
        operands.add(0, first);
        return Erwan.OR(operands);
    }

    private static List<Erwan> collectOrOperands(CstNode orStar) {
        List<Erwan> acc = new ArrayList<>();
        CstInternal node = (CstInternal) orStar;
        while (!node.children().isEmpty()) {
            // Or_Operand_Star -> OrOp Term Or_Operand_Star
            acc.add(buildTerm(node.first(NonTerminal.Term).orElseThrow()));
            node = (CstInternal) node.first(NonTerminal.Or_Operand_Star).orElseThrow();
        }
        return acc;
    }

    private static Erwan buildTerm(CstNode node) {
        CstInternal term = (CstInternal) node;
        Erwan first = buildFactor(term.first(NonTerminal.Factor).orElseThrow());
        List<Erwan> operands = collectAndOperands(term.first(NonTerminal.And_Operand_Star).orElseThrow());
        if (operands.isEmpty()) return first;
        operands.add(0, first);
        return Erwan.AND(operands);
    }

    private static List<Erwan> collectAndOperands(CstNode andStar) {
        List<Erwan> acc = new ArrayList<>();
        CstInternal node = (CstInternal) andStar;
        while (!node.children().isEmpty()) {
            // And_Operand_Star -> AndOp Factor And_Operand_Star
            acc.add(buildFactor(node.first(NonTerminal.Factor).orElseThrow()));
            node = (CstInternal) node.first(NonTerminal.And_Operand_Star).orElseThrow();
        }
        return acc;
    }

    private static Erwan buildFactor(CstNode node) {
        CstInternal factor = (CstInternal) node;
        // Factor -> '(' SOT ')' | LiteralValue | NotOp Signal | Signal
        if (factor.has(new Terminal(Token.LeftPar))) {
            return buildSOT(factor.first(NonTerminal.SumOfTerms).orElseThrow());
        }
        if (factor.has(NonTerminal.LiteralValue)) {
            throw new ConversionException(factor.startOffset(), "Factor",
                ConversionException.Reason.LITERAL_IN_RHS_NOT_SUPPORTED,
                "Constante (.0/.1) en RHS non supportee en S1 (offset " + factor.startOffset() + ")");
        }
        CstNode signal = factor.first(NonTerminal.Signal).orElseThrow();
        Erwan lit = Erwan.LITTERAL(Names.extractScalarFromSignalNT(signal));
        if (factor.has(new Terminal(Token.NotOp))) {
            return Erwan.NOT(lit);
        }
        return lit;
    }
}
```

- [ ] **Step 4 : compiler + lancer le test, vérifier vert**

```bash
./build.sh test \
  && java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
       org.junit.runner.JUnitCore tests.parser.conversion.ExpressionBuilderTest
```

Expected : `OK (1 test)`.

- [ ] **Step 5 : commit**

```bash
git add parser/conversion/ExpressionBuilder.java tests/parser/conversion/ExpressionBuilderTest.java
git commit -m "feat(conversion): ExpressionBuilder squelette + cas Signal scalaire

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

### 5.2 — Constructs additionnels (extension du même test file)

- [ ] **Step 1 : ajouter les tests**

Ajouter à `ExpressionBuilderTest.java` (après `singleSignal_a`) :

```java
@Test
public void notSignal_slashA() {
    Erwan e = ExpressionBuilder.build(rhs("module m (a) c = /a end module"));
    assertEquals(Operation.NOT, e.Op);
    assertEquals(1, e.Entrees.size());
    assertEquals(Operation.LITTERAL, e.Entrees.get(0).Op);
    assertEquals("a", e.Entrees.get(0).Nom());
}

@Test
public void andTwo_aTimesB() {
    Erwan e = ExpressionBuilder.build(rhs("module m (a, b) c = a * b end module"));
    assertEquals(Operation.AND, e.Op);
    assertEquals(2, e.Entrees.size());
    assertEquals("a", e.Entrees.get(0).Nom());
    assertEquals("b", e.Entrees.get(1).Nom());
}

@Test
public void orTwo_aPlusB_opIsOR() {
    // SENTINELLE bug Erwan.OR ligne 145 (qui retournait Operation.AND)
    Erwan e = ExpressionBuilder.build(rhs("module m (a, b) c = a + b end module"));
    assertEquals("OR doit etre Op.OR (sentinelle bug)", Operation.OR, e.Op);
    assertEquals(2, e.Entrees.size());
}

@Test
public void andThree_flat() {
    Erwan e = ExpressionBuilder.build(rhs("module m (a, b, c) d = a * b * c end module"));
    assertEquals(Operation.AND, e.Op);
    assertEquals("AND doit etre n-aire aplati (3 operandes)", 3, e.Entrees.size());
    assertEquals("a", e.Entrees.get(0).Nom());
    assertEquals("b", e.Entrees.get(1).Nom());
    assertEquals("c", e.Entrees.get(2).Nom());
}

@Test
public void orThree_flat() {
    Erwan e = ExpressionBuilder.build(rhs("module m (a, b, c) d = a + b + c end module"));
    assertEquals(Operation.OR, e.Op);
    assertEquals(3, e.Entrees.size());
}

@Test
public void precedence_aPlusBTimesC() {
    Erwan e = ExpressionBuilder.build(rhs("module m (a, b, c) d = a + b * c end module"));
    assertEquals(Operation.OR, e.Op);
    assertEquals(2, e.Entrees.size());
    assertEquals(Operation.LITTERAL, e.Entrees.get(0).Op);
    assertEquals("a", e.Entrees.get(0).Nom());
    assertEquals(Operation.AND, e.Entrees.get(1).Op);
    assertEquals(2, e.Entrees.get(1).Entrees.size());
}

@Test
public void parens_aTimesParenBPlusC() {
    Erwan e = ExpressionBuilder.build(rhs("module m (a, b, c) d = a * (b + c) end module"));
    assertEquals(Operation.AND, e.Op);
    assertEquals(2, e.Entrees.size());
    assertEquals(Operation.LITTERAL, e.Entrees.get(0).Op);
    assertEquals(Operation.OR, e.Entrees.get(1).Op);
}

@Test
public void parensDegenerate_parA() {
    Erwan e = ExpressionBuilder.build(rhs("module m (a) c = (a) end module"));
    assertEquals(Operation.LITTERAL, e.Op);
    assertEquals("a", e.Nom());
}

@Test
public void notTwo_slashATimesSlashB() {
    Erwan e = ExpressionBuilder.build(rhs("module m (a, b) c = /a * /b end module"));
    assertEquals(Operation.AND, e.Op);
    assertEquals(2, e.Entrees.size());
    assertEquals(Operation.NOT, e.Entrees.get(0).Op);
    assertEquals(Operation.NOT, e.Entrees.get(1).Op);
}

@Test
public void deepParens_noStackOverflow() {
    String src = "module m (a) c = ((((a)))) end module";
    Erwan e = ExpressionBuilder.build(rhs(src));
    assertEquals(Operation.LITTERAL, e.Op);
    assertEquals("a", e.Nom());
}
```

- [ ] **Step 2 : lancer les tests, vérifier vert sur les 11 tests**

```bash
./build.sh test \
  && java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
       org.junit.runner.JUnitCore tests.parser.conversion.ExpressionBuilderTest
```

Expected : `OK (11 tests)`.

Si certains échouent : ne **pas** modifier les tests pour qu'ils passent. Corriger l'implémentation `ExpressionBuilder` jusqu'à ce que tous passent.

- [ ] **Step 3 : commit**

```bash
git add tests/parser/conversion/ExpressionBuilderTest.java
git commit -m "test(conversion): ExpressionBuilder couverture complete S1

11 tests : Signal, NOT, AND/OR n-aire, precedence, parens, profondeur.
Inclut sentinelle bug Erwan.OR (Op doit etre OR, pas AND).

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 6 — `ModuleBuilder` (TDD)

**Files :**
- Create : `parser/conversion/ModuleBuilder.java`
- Test : `tests/parser/conversion/ModuleBuilderTest.java`

API : `ModuleBuilder.build(CstNode moduleNode) → Module` où `moduleNode` est un `CstInternal(NonTerminal.Module, …)`.

Logique :
1. Parcourt `Param` + `Separ_Param_Star` ; pour chaque `Signal`, valide la scalarité via `Names.extractScalarFromSignalNT` (lève si vectoriel).
2. Aplatit `Instance_Plus` + `Instance_Star` en boucle (récursion à droite).
3. Pour chaque `Instance` :
   - Si elle commence par `Dollar` → `MODULE_CALL_NOT_SUPPORTED`.
   - Sinon, c'est `Identifiant Operation`. Récupère le `Identifiant` (LHS scalaire via `Names.extractScalarFromIdAndSubset`).
   - `Operation` peut être `ModuleCall` (rejet) ou `SS_OPT Assignment` ; vérifier scalarité du SS_OPT, puis descendre dans `Assignment`.
   - `Assignment` peut être `MemoryAssignment` (rejet) ou `SignalAssignment`.
   - `SignalAssignment → AssignOp SumOfTermsCompound` ; déléguer le SOTC à `ExpressionBuilder.build`.
   - Vérifier non-redondance LHS dans un `Set<String>`. Si déjà vu : `DUPLICATE_LHS`.
   - Émettre `Erwan.AFFECTATION(lhs, rhs)`.
4. Renvoie `new Module(plan, Collections.emptyList(), Collections.emptyList(), Collections.emptyList())`.

- [ ] **Step 1 : écrire les tests**

Créer `tests/parser/conversion/ModuleBuilderTest.java` :

```java
package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import parser.conversion.ModuleBuilder;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Erwan.Erwan;
import simulateur.Erwan.Operation;
import simulateur.Module;

public class ModuleBuilderTest {

    private static Module build(String src) {
        CstNode root = CstParser.parse(src);
        CstNode mod = root.first(NonTerminal.Module).orElseThrow();
        return ModuleBuilder.build(mod);
    }

    @Test
    public void singleParam_singleInstance() {
        Module m = build("module m (a) c = a end module");
        assertEquals(1, m.Plan.size());
        Erwan aff = m.Plan.get(0);
        assertEquals(Operation.AFFECTATION, aff.Op);
        assertEquals("c", aff.Nom());
        assertTrue(m.Entrees.isEmpty());
        assertTrue(m.Sorties.isEmpty());
        assertTrue(m.Branchements.isEmpty());
    }

    @Test
    public void twoInstances_orderPreserved() {
        Module m = build("module m (a, b) c = a * b d = a + b end module");
        assertEquals(2, m.Plan.size());
        assertEquals("c", m.Plan.get(0).Nom());
        assertEquals("d", m.Plan.get(1).Nom());
    }

    @Test(expected = ConversionException.class)
    public void duplicateLhs_throws() {
        build("module m (a, b) c = a c = b end module");
    }

    @Test
    public void duplicateLhs_reasonIsDuplicate() {
        try {
            build("module m (a, b) c = a c = b end module");
            fail("expected ConversionException");
        } catch (ConversionException ex) {
            assertEquals(Reason.DUPLICATE_LHS, ex.reason());
        }
    }

    @Test(expected = ConversionException.class)
    public void vectorParam_throws() {
        build("module m (a[0..3]) c = a end module");
    }

    @Test(expected = ConversionException.class)
    public void vectorLhs_throws() {
        build("module m (a) c[0] = a end module");
    }
}
```

- [ ] **Step 2 : lancer, échec compilation**

```bash
./build.sh test
```

Expected : `cannot find symbol: class ModuleBuilder`.

- [ ] **Step 3 : implémenter `ModuleBuilder`**

Créer `parser/conversion/ModuleBuilder.java` :

```java
package parser.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Erwan.Erwan;
import simulateur.Module;

public final class ModuleBuilder {

    private ModuleBuilder() {}

    public static Module build(CstNode moduleNode) {
        CstInternal mod = (CstInternal) moduleNode;

        // Validation des parametres : tous scalaires
        validateParams(mod);

        // Plan : aplatir Instance_Plus + Instance_Star
        List<Erwan> plan = new ArrayList<>();
        Set<String> lhsSeen = new HashSet<>();

        CstNode instancePlus = mod.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ip = (CstInternal) instancePlus;
        // Instance_Plus -> Instance Instance_Star
        plan.add(buildInstance(ip.first(NonTerminal.Instance).orElseThrow(), lhsSeen));
        CstInternal star = (CstInternal) ip.first(NonTerminal.Instance_Star).orElseThrow();
        while (!star.children().isEmpty()) {
            plan.add(buildInstance(star.first(NonTerminal.Instance).orElseThrow(), lhsSeen));
            star = (CstInternal) star.first(NonTerminal.Instance_Star).orElseThrow();
        }

        return new Module(plan, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    private static void validateParams(CstInternal mod) {
        CstNode firstParam = mod.first(NonTerminal.Param).orElseThrow();
        Names.extractScalarFromSignalNT(((CstInternal) firstParam).first(NonTerminal.Signal).orElseThrow());
        CstInternal sps = (CstInternal) mod.first(NonTerminal.Separ_Param_Star).orElseThrow();
        while (!sps.children().isEmpty()) {
            CstNode p = sps.first(NonTerminal.Param).orElseThrow();
            Names.extractScalarFromSignalNT(((CstInternal) p).first(NonTerminal.Signal).orElseThrow());
            sps = (CstInternal) sps.first(NonTerminal.Separ_Param_Star).orElseThrow();
        }
    }

    private static Erwan buildInstance(CstNode instanceNode, Set<String> lhsSeen) {
        CstInternal inst = (CstInternal) instanceNode;
        // Instance -> Identifiant Operation | Dollar Identifiant ModuleCall
        if (inst.has(new Terminal(Token.Dollar))) {
            throw new ConversionException(inst.startOffset(), "Instance",
                ConversionException.Reason.MODULE_CALL_NOT_SUPPORTED,
                "Appel module ($nom(...)) non supporte en S1 (offset " + inst.startOffset() + ")");
        }
        CstNode id = inst.first(new Terminal(Token.Identifiant)).orElseThrow();
        CstInternal op = (CstInternal) inst.first(NonTerminal.Operation).orElseThrow();
        // Operation -> ModuleCall | Signal_Subset_Opt Assignment
        if (op.has(NonTerminal.ModuleCall)) {
            throw new ConversionException(op.startOffset(), "Operation",
                ConversionException.Reason.MODULE_CALL_NOT_SUPPORTED,
                "Appel module en RHS non supporte en S1 (offset " + op.startOffset() + ")");
        }
        CstNode subset = op.first(NonTerminal.Signal_Subset_Opt).orElseThrow();
        String lhs = Names.extractScalarFromIdAndSubset(id, subset);
        if (!lhsSeen.add(lhs)) {
            throw new ConversionException(id.startOffset(), "Identifiant",
                ConversionException.Reason.DUPLICATE_LHS,
                "Double assignation du signal '" + lhs + "' (offset " + id.startOffset() + ")");
        }
        CstInternal assignment = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();
        // Assignment -> SignalAssignment | MemoryAssignment
        if (assignment.has(NonTerminal.MemoryAssignment)) {
            throw new ConversionException(assignment.startOffset(), "Assignment",
                ConversionException.Reason.MEMORY_ASSIGNMENT_NOT_SUPPORTED,
                "Affectation memoire (:=) non supportee en S1 (offset " + assignment.startOffset() + ")");
        }
        CstNode sigA = assignment.first(NonTerminal.SignalAssignment).orElseThrow();
        CstNode sotc = ((CstInternal) sigA).first(NonTerminal.SumOfTermsCompound).orElseThrow();
        Erwan rhs = ExpressionBuilder.build(sotc);
        return Erwan.AFFECTATION(lhs, rhs);
    }
}
```

- [ ] **Step 4 : compiler + lancer le test, vérifier vert**

```bash
./build.sh test \
  && java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
       org.junit.runner.JUnitCore tests.parser.conversion.ModuleBuilderTest
```

Expected : `OK (6 tests)`.

- [ ] **Step 5 : commit**

```bash
git add parser/conversion/ModuleBuilder.java tests/parser/conversion/ModuleBuilderTest.java
git commit -m "feat(conversion): ModuleBuilder + validation Param + dedup LHS

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 7 — `Conversion` (driver public)

**Files :**
- Modify : `parser/conversion/Conversion.java` (remplace le stub d'Erwan)

- [ ] **Step 1 : remplacer le stub par l'implémentation finale**

Écraser `parser/conversion/Conversion.java` avec :

```java
package parser.conversion;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Module;

public final class Conversion {

    private Conversion() {}

    public static Module convert(CstNode tree) {
        if (!(tree instanceof CstInternal root)) {
            throw new ConversionException(tree.startOffset(), String.valueOf(tree.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "CST racine doit etre un CstInternal");
        }
        if (root.nt() != NonTerminal.Start) {
            throw new ConversionException(root.startOffset(), String.valueOf(root.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "CST racine doit etre Start");
        }
        CstNode module = root.first(NonTerminal.Module).orElseThrow(() ->
            new ConversionException(root.startOffset(), "Start",
                ConversionException.Reason.MALFORMED_CST,
                "Start sans Module enfant"));
        return ModuleBuilder.build(module);
    }
}
```

- [ ] **Step 2 : compiler, vérifier que tout build**

```bash
./build.sh test
```

Expected : Build OK.

- [ ] **Step 3 : commit**

```bash
git add parser/conversion/Conversion.java
git commit -m "feat(conversion): Conversion.convert delegue a ModuleBuilder

Remplace le stub d'Erwan par l'entree publique finale.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 8 — Smoke test e2e

**Files :**
- Create : `tests/parser/conversion/ConversionSmokeTest.java`

- [ ] **Step 1 : écrire le smoke test**

```java
package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.FileSimulateur;
import simulateur.Module;

public class ConversionSmokeTest {

    @Test
    public void parseConvertSimulate_etGate() {
        String src = "module et (a, b) c = a * b end module";
        CstNode root = CstParser.parse(src);
        Module module = Conversion.convert(root);

        assertEquals(1, module.Plan.size());
        assertEquals("c", module.Plan.get(0).Nom());

        // Le constructeur ne doit pas planter
        FileSimulateur fs = new FileSimulateur(module.Plan);
        assertNotNull(fs);
    }
}
```

- [ ] **Step 2 : compiler + lancer**

```bash
./build.sh test \
  && java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
       org.junit.runner.JUnitCore tests.parser.conversion.ConversionSmokeTest
```

Expected : `OK (1 test)`.

**Si rouge** : noter précisément la stack. Si l'erreur vient de `FileSimulateur` (Mati), STOP, ouvrir un ticket Mati avec le test minimal et la stack. La spec autorise replanifier sur ce cas.

- [ ] **Step 3 : commit**

```bash
git add tests/parser/conversion/ConversionSmokeTest.java
git commit -m "test(conversion): smoke test e2e parse->convert->FileSimulateur

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 9 — Tests d'erreur exhaustifs

**Files :**
- Create : `tests/parser/conversion/ConversionErrorsTest.java`

Couverture : un test par production hors S1 + vérification de l'offset.

- [ ] **Step 1 : écrire les tests**

```java
package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;

public class ConversionErrorsTest {

    private static ConversionException convertAndCatch(String src) {
        CstNode root = CstParser.parse(src);
        try {
            Conversion.convert(root);
            return null;
        } catch (ConversionException ex) {
            return ex;
        }
    }

    @Test
    public void concat_rejected() {
        ConversionException ex = convertAndCatch("module m (a, b) c = a & b end module");
        assertNotNull("attendu ConversionException", ex);
        assertEquals(Reason.CONCAT_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void memoryAssignment_rejected() {
        // Forme minimale legale grammaire : c := a on a , reset when a
        ConversionException ex = convertAndCatch(
            "module m (a) c := a on a , reset when a end module");
        assertNotNull(ex);
        assertEquals(Reason.MEMORY_ASSIGNMENT_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void moduleCall_rejected() {
        ConversionException ex = convertAndCatch("module m (a, b, c) $add(a, b, c) end module");
        assertNotNull(ex);
        assertEquals(Reason.MODULE_CALL_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void literalInRhs_rejected() {
        ConversionException ex = convertAndCatch("module m (a) c = .1 end module");
        assertNotNull(ex);
        assertEquals(Reason.LITERAL_IN_RHS_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void vectorLhs_rejected() {
        ConversionException ex = convertAndCatch("module m (a) c[0] = a end module");
        assertNotNull(ex);
        assertEquals(Reason.VECTOR_SUBSET_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void vectorRhs_rejected() {
        ConversionException ex = convertAndCatch("module m (a) c = a[3] end module");
        assertNotNull(ex);
        assertEquals(Reason.VECTOR_SUBSET_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void vectorParam_rejected() {
        ConversionException ex = convertAndCatch("module m (a[0..3]) c = a end module");
        assertNotNull(ex);
        assertEquals(Reason.VECTOR_SUBSET_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void offsetIsCoherent_concat() {
        // 'a & b' : le '&' est apres "module m (a, b) c = a " soit offset 22
        ConversionException ex = convertAndCatch("module m (a, b) c = a & b end module");
        assertNotNull(ex);
        assertTrue("offset doit pointer vers la zone du concat",
            ex.offset() >= 20 && ex.offset() <= 26);
    }
}
```

- [ ] **Step 2 : compiler + lancer**

```bash
./build.sh test \
  && java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
       org.junit.runner.JUnitCore tests.parser.conversion.ConversionErrorsTest
```

Expected : `OK (8 tests)`.

Si un test échoue car la source SHDL n'est pas grammaticalement valide (le parser lève `ParsingException` avant Conversion), ajuster la source pour rester syntaxiquement valide. Le test doit isoler un rejet **sémantique**, pas syntaxique.

- [ ] **Step 3 : commit**

```bash
git add tests/parser/conversion/ConversionErrorsTest.java
git commit -m "test(conversion): rejets exhaustifs hors S1 + verification offset

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 10 — Audit fixtures + tests d'intégration

**Files :**
- Create : `tests/parser/conversion/ConversionIntegrationTest.java`

- [ ] **Step 1 : auditer les 5 fixtures de `EndToEndExtendedTest`**

Lire `tests/parser/ll1/tabledriven/EndToEndExtendedTest.java` et noter pour chaque fixture (mux, demux, halfAdder, xor, trois-portes) :

- A-t-elle des `[...]` (vecteur) ?
- A-t-elle des `&` (concat) ?
- A-t-elle des `:=` (mémoire) ?
- A-t-elle des `$` (module call) ?
- A-t-elle des `.0`/`.1` (constantes en RHS) ?

Pour chaque fixture **propre S1**, prévoir un test positif (`Module` non-null + `Plan.size()` correct). Pour chaque fixture **non-S1**, prévoir un test négatif (la `Reason` attendue).

- [ ] **Step 2 : écrire les tests**

Créer `tests/parser/conversion/ConversionIntegrationTest.java`. Squelette à compléter selon les résultats du Step 1 :

```java
package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.conversion.ConversionException;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Module;

public class ConversionIntegrationTest {

    private static Module convert(String src) {
        return Conversion.convert(CstParser.parse(src));
    }

    @Test
    public void xor_isS1Valid() {
        // xor(a, b) o = a * /b + /a * b
        Module m = convert("module xor (a, b) o = a * /b + /a * b end module");
        assertEquals(1, m.Plan.size());
        assertEquals("o", m.Plan.get(0).Nom());
    }

    @Test
    public void halfAdder_isS1Valid() {
        // halfAdder(a, b) s = a + b   c = a * b   (PAS de vecteur ici, deux signaux scalaires)
        Module m = convert("module halfAdder (a, b) s = a + b c = a * b end module");
        assertEquals(2, m.Plan.size());
    }

    @Test
    public void troisPortes_isS1Valid() {
        Module m = convert("module trois (a, b, c) x = a * b y = b * c z = a * c end module");
        assertEquals(3, m.Plan.size());
    }

    @Test
    public void mux2to1_isS1Valid() {
        Module m = convert("module mux (a, b, sel) o = a * /sel + b * sel end module");
        assertEquals(1, m.Plan.size());
        assertEquals("o", m.Plan.get(0).Nom());
    }

    @Test
    public void demux_isS1Valid() {
        Module m = convert("module demux (a, sel) o0 = a * /sel o1 = a * sel end module");
        assertEquals(2, m.Plan.size());
    }
}
```

**Note** : si une fixture s'avère **non-S1** (vecteur/concat/etc.), remplacer le test positif par un test négatif :

```java
@Test(expected = ConversionException.class)
public void <fixture>_isS1Invalid() {
    convert("<source SHDL>");
}
```

Dans ce cas, ajouter en commentaire la `Reason` exacte attendue.

- [ ] **Step 3 : compiler + lancer**

```bash
./build.sh test \
  && java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
       org.junit.runner.JUnitCore tests.parser.conversion.ConversionIntegrationTest
```

Expected : tous les tests verts (positifs ou négatifs selon ce qui a été décidé Step 1).

- [ ] **Step 4 : commit**

```bash
git add tests/parser/conversion/ConversionIntegrationTest.java
git commit -m "test(conversion): integration sur fixtures EndToEndExtendedTest

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 11 — Test sentinelle `Erwan.OR()`

**Files :**
- Create : `tests/simulateur/Erwan/ErwanOrSentinelTest.java`

Test JUnit qui vérifie que `Erwan.OR(...)` retourne bien `Op == Operation.OR`. Empêche la régression du bug fixé en Task 1.

**Avertissement** : ce test est dans `tests/simulateur/Erwan/`, dossier qui ne fait pas partie du périmètre Alexis. Avant de le créer, **demander à Erwan via Discord** : *"Je rajoute un test sentinelle sur ton `Erwan.OR()` pour empêcher la régression future. OK ?"*. Si refus → skip cette task.

- [ ] **Step 1 : accord Erwan (Discord)**

- [ ] **Step 2 : créer le dossier si nécessaire**

```bash
mkdir -p tests/simulateur/Erwan
```

- [ ] **Step 3 : écrire le test**

```java
package tests.simulateur.Erwan;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import simulateur.Erwan.Erwan;
import simulateur.Erwan.Operation;

public class ErwanOrSentinelTest {

    @Test
    public void or_returnsOpOR_notAND() {
        List<Erwan> entrees = Arrays.asList(Erwan.LITTERAL("a"), Erwan.LITTERAL("b"));
        Erwan e = Erwan.OR(entrees);
        assertEquals("Erwan.OR doit retourner Operation.OR (regression du bug ligne 145)",
                     Operation.OR, e.Op);
    }

    @Test
    public void and_returnsOpAND() {
        List<Erwan> entrees = Arrays.asList(Erwan.LITTERAL("a"), Erwan.LITTERAL("b"));
        Erwan e = Erwan.AND(entrees);
        assertEquals(Operation.AND, e.Op);
    }
}
```

- [ ] **Step 4 : compiler + lancer**

```bash
./build.sh test \
  && java -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
       org.junit.runner.JUnitCore tests.simulateur.Erwan.ErwanOrSentinelTest
```

Expected : `OK (2 tests)`.

- [ ] **Step 5 : commit**

```bash
git add tests/simulateur/Erwan/ErwanOrSentinelTest.java
git commit -m "test(erwan): sentinelle anti-regression sur Erwan.OR()

Verifie que Erwan.OR retourne Op.OR et non Op.AND.
Bug ligne 145 fixe par Erwan en pre-requis de la conversion.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 12 — Vérification finale + push

- [ ] **Step 1 : compiler + lancer toute la batterie**

```bash
rm -rf bin/* && ./build.sh test && ./run.sh 2>&1 | tee /tmp/run.log | tail -50
```

Expected : aucune ligne `FAIL`, aucune `Tests run: ... Failures: <non-zero>`.

- [ ] **Step 2 : revue rapide des commits**

```bash
git log --oneline origin/feature/parser-ll1-table-driven..HEAD
```

Vérifier que la séquence est lisible (un message clair par task).

- [ ] **Step 3 : confirmer push avec Alexis**

**Ne pas push sans confirmation explicite d'Alexis** (mémoire utilisateur `feedback_push_confirmation.md`). Lui montrer le résumé des commits et demander.

- [ ] **Step 4 : push (uniquement après confirmation)**

```bash
git push origin feature/parser-ll1-table-driven
```

---

## Notes de fin de plan

- Si Task 8 (smoke e2e) est rouge à cause d'un bug Mati découvert, ouvrir un ticket Discord avec le test minimal repro, ne pas tenter de fixer côté Mati. Mémoire utilisateur : ne pas modifier le code des coéquipiers sans accord explicite.
- Si Task 9 fait apparaître que les sources SHDL d'erreur ne sont pas grammaticalement valides (parser lève avant Conversion), itérer sur les sources sans changer les `Reason` attendues. La grammaire `Grammar.java` est figée par `GrammarFreezeTest`.
- Si Task 10 montre que <2 fixtures sont S1-valides, signaler à Alexis : on devra peut-être créer de nouvelles fixtures S1 dédiées au lieu de réutiliser celles d'EndToEndExtendedTest.
- Le scope de cette branche est S1 uniquement. **Ne pas** ajouter le support vectoriel/concat/`:=`/ModuleCall même si "ça serait facile". Le scope figé est documenté dans la spec §11 (Non-objectifs).
