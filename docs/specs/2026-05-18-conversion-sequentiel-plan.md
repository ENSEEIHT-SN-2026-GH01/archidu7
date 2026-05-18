# Conversion séquentielle (`:=`) — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convertir l'affectation mémoire SHDL `:=` en bascule D maître-esclave combinatoire dans `parser/conversion/`, sans toucher l'IR `erwan` ni le simulateur.

**Architecture:** Desugar. `ModuleBuilder`, au lieu de rejeter `MemoryAssignment`, délègue à une classe neuve `MemoryAssignmentBuilder` qui émet, par bit du LHS, ~12 opérations `erwan` (`AND`/`OR`/`NOT`/`AFFECTATION`) réalisant un maître-esclave avec set/reset asynchrone et enable optionnel. Les noms de signaux internes sont rendus uniques par un générateur de préfixes frais (`FreshNames`) alimenté par un pré-passage sur le CST du module.

**Tech Stack:** Java 25, JUnit 4.13.2. Build : `./build.sh test`. Tests : `java -cp bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore <classe>`.

**Spec de référence :** `docs/specs/2026-05-18-conversion-sequentiel-design.md`.

---

## Structure des fichiers

- **Créer** `parser/conversion/FreshNames.java` — générateur de préfixes de signaux internes garantis frais.
- **Créer** `parser/conversion/MemoryAssignmentBuilder.java` — le desugar maître-esclave.
- **Modifier** `parser/conversion/ExpressionBuilder.java` — rendre `buildSOT` public (les bus `clk`/`sr`/`en` sont des `SumOfTerms`).
- **Modifier** `parser/conversion/ModuleBuilder.java` — pré-passage de collecte de noms ; remplacer le `throw MEMORY_ASSIGNMENT_NOT_SUPPORTED` par l'appel au builder.
- **Créer** `tests/parser/conversion/FreshNamesTest.java` — tests unitaires du générateur.
- **Créer** `tests/parser/conversion/MemoryAssignmentTest.java` — tests e2e via `FileSimulateur`.

---

## Task 1 : Exposer `ExpressionBuilder.buildSOT`

Les expressions `clk`, `sr`, `en` d'un `:=` sont des `SumOfTerms` (grammaire `Grammar.java:169`), pas des `SumOfTermsCompound`. `ExpressionBuilder.build` n'accepte qu'un `SumOfTermsCompound` ; `buildSOT` (qui traite un `SumOfTerms`) existe déjà mais est `private`.

**Files:**
- Modify: `parser/conversion/ExpressionBuilder.java:42`

- [ ] **Step 1 : Rendre `buildSOT` public**

Dans `parser/conversion/ExpressionBuilder.java`, remplacer la signature ligne 42 :

```java
    private static Bus buildSOT(CstNode node) {
```

par :

```java
    /**
     * Construit le bus d'un noeud {@code SumOfTerms} (expression sans
     * concatenation). Utilise pour les operandes clk/sr/en d'un {@code :=}.
     */
    public static Bus buildSOT(CstNode node) {
```

- [ ] **Step 2 : Compiler**

Run : `./build.sh test`
Expected : `BUILD` réussit, aucune erreur de compilation.

- [ ] **Step 3 : Commit**

```bash
git add parser/conversion/ExpressionBuilder.java
git commit -m "refactor(conversion): exposer ExpressionBuilder.buildSOT en public"
```

---

## Task 2 : `FreshNames` — générateur de préfixes frais

Les signaux internes d'une bascule (`mS`, `qm`, …) ne doivent jamais entrer en collision avec un signal utilisateur. `FreshNames` produit des préfixes `__ff<N>__` tels qu'aucun nom utilisé ne commence par ce préfixe.

**Files:**
- Create: `parser/conversion/FreshNames.java`
- Test: `tests/parser/conversion/FreshNamesTest.java`

- [ ] **Step 1 : Écrire le test**

Créer `tests/parser/conversion/FreshNamesTest.java` :

```java
package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;
import parser.conversion.FreshNames;

public class FreshNamesTest {

    @Test
    public void prefixesSontDistincts() {
        FreshNames g = new FreshNames(Set.of());
        String a = g.fresh();
        String b = g.fresh();
        assertNotEquals(a, b);
    }

    @Test
    public void evitePrefixeEnCollision() {
        // un signal utilisateur commence par "__ff0__" -> le generateur doit
        // sauter ce prefixe.
        FreshNames g = new FreshNames(Set.of("__ff0__qm", "__ff1__nclk"));
        String p = g.fresh();
        assertFalse("aucun nom utilise ne doit commencer par le prefixe",
            "__ff0__qm".startsWith(p) || "__ff1__nclk".startsWith(p));
    }

    @Test
    public void prefixeUtilisableEnPrefixe() {
        FreshNames g = new FreshNames(Set.of());
        String p = g.fresh();
        assertTrue(p.startsWith("__ff"));
        assertTrue(p.endsWith("__"));
    }
}
```

- [ ] **Step 2 : Lancer le test, vérifier l'échec**

Run : `./build.sh test` puis
`java -cp bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore tests.parser.conversion.FreshNamesTest`
Expected : échec de compilation (`FreshNames` n'existe pas).

- [ ] **Step 3 : Écrire `FreshNames`**

Créer `parser/conversion/FreshNames.java` :

```java
package parser.conversion;

import java.util.Set;

/**
 * Generateur de prefixes de signaux internes garantis frais dans un module.
 *
 * <p>Chaque appel a {@link #fresh()} retourne un prefixe {@code __ff<N>__}
 * tel qu'aucun nom de la collection fournie ne commence par ce prefixe : tous
 * les signaux {@code <prefixe>xxx} generes ensuite sont donc libres.
 */
public final class FreshNames {

    private final Set<String> used;
    private int counter = 0;

    /**
     * @param used ensemble de tous les noms de signaux deja presents dans le
     *             module (params, LHS, signaux references en RHS)
     */
    public FreshNames(Set<String> used) {
        this.used = used;
    }

    /** Retourne un prefixe frais, distinct de tous les precedents. */
    public String fresh() {
        while (true) {
            String prefix = "__ff" + counter + "__";
            counter++;
            boolean clash = false;
            for (String u : used) {
                if (u.startsWith(prefix)) {
                    clash = true;
                    break;
                }
            }
            if (!clash) {
                return prefix;
            }
        }
    }
}
```

- [ ] **Step 4 : Lancer le test, vérifier le succès**

Run : `./build.sh test` puis
`java -cp bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore tests.parser.conversion.FreshNamesTest`
Expected : `OK (3 tests)`.

- [ ] **Step 5 : Commit**

```bash
git add parser/conversion/FreshNames.java tests/parser/conversion/FreshNamesTest.java
git commit -m "feat(conversion): FreshNames, generateur de prefixes de signaux internes"
```

---

## Task 3 : `MemoryAssignmentBuilder` — le desugar

Classe neuve : navigue le `MemoryAssignment`, construit les bus, vérifie les largeurs, émet le maître-esclave par bit. Voir spec §3.

Rappel grammaire (`Grammar.java:169`) :
```
MemoryAssignment ::= MemAssignOp SumOfTermsCompound OnKW SumOfTerms
                     Comma_Opt Set_Or_Reset WhenKW SumOfTerms
                     Enabled_Operand_Opt Semicolon_Opt
Set_Or_Reset ::= ResetKW | SetKW
Enabled_Operand_Opt ::= Comma_Opt EnabledKW WhenKW SumOfTerms | ε
```
Sous `MemoryAssignment` : `data` = l'unique `SumOfTermsCompound` ; les deux `SumOfTerms` directs sont `clk` (premier) et `sr` (second) ; le `SumOfTerms` de l'enable est imbriqué dans `Enabled_Operand_Opt`.

**Files:**
- Create: `parser/conversion/MemoryAssignmentBuilder.java`

- [ ] **Step 1 : Écrire `MemoryAssignmentBuilder`**

Créer `parser/conversion/MemoryAssignmentBuilder.java` :

```java
package parser.conversion;

import java.util.ArrayList;
import java.util.List;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Erwan;

/**
 * Desugar d'une affectation memoire SHDL ({@code :=}) en bascule D
 * maitre-esclave combinatoire.
 *
 * <p>Voir {@code docs/specs/2026-05-18-conversion-sequentiel-design.md}.
 * Chaque bit du LHS produit une bascule : signaux partages (clk/nclk/sr/en)
 * emis une fois, puis le maitre-esclave par bit.
 */
public final class MemoryAssignmentBuilder {

    private MemoryAssignmentBuilder() {}

    /** Mode de forcage asynchrone (clause {@code set when} / {@code reset when}). */
    private enum Mode { SET, RESET }

    /**
     * Convertit un noeud {@code MemoryAssignment} en operations erwan.
     *
     * @param ma        noeud {@code CstInternal(MemoryAssignment)}
     * @param lhsName   nom du signal LHS
     * @param lhsSubset sous-ensemble du LHS (scalaire, index unique, ou plage)
     * @param names     generateur de prefixes frais (unicite dans le module)
     * @return la liste des operations realisant la/les bascule(s)
     */
    public static List<Erwan> build(CstNode ma, String lhsName, Subset lhsSubset,
            FreshNames names) {
        if (!(ma instanceof CstInternal m) || m.nt() != NonTerminal.MemoryAssignment) {
            throw new ConversionException(ma.startOffset(), String.valueOf(ma.symbol()),
                ConversionException.Reason.MALFORMED_CST, "Attendu MemoryAssignment");
        }

        // --- Extraction des noeuds CST ---
        CstNode dataNode = m.first(NonTerminal.SumOfTermsCompound).orElseThrow(() ->
            malformed(m, "MemoryAssignment sans SumOfTermsCompound (data)"));
        List<CstNode> sots = m.allOf(NonTerminal.SumOfTerms);
        if (sots.size() < 2) {
            throw malformed(m, "MemoryAssignment attend 2 SumOfTerms directs (clk, sr)");
        }
        CstNode clkNode = sots.get(0);
        CstNode srNode = sots.get(1);
        CstNode sorNode = m.first(NonTerminal.Set_Or_Reset).orElseThrow(() ->
            malformed(m, "MemoryAssignment sans Set_Or_Reset"));
        Mode mode = m_mode(sorNode);
        CstNode enaOpt = m.first(NonTerminal.Enabled_Operand_Opt).orElseThrow(() ->
            malformed(m, "MemoryAssignment sans Enabled_Operand_Opt"));
        CstNode enNode = null;
        if (enaOpt instanceof CstInternal ena && !ena.children().isEmpty()) {
            enNode = ena.first(NonTerminal.SumOfTerms).orElseThrow(() ->
                malformed(ena, "Enabled_Operand_Opt non-epsilon sans SumOfTerms"));
        }

        // --- Construction des bus ---
        Bus data = ExpressionBuilder.build(dataNode);
        Bus clk = ExpressionBuilder.buildSOT(clkNode);
        Bus sr = ExpressionBuilder.buildSOT(srNode);
        Bus en = (enNode != null) ? ExpressionBuilder.buildSOT(enNode) : null;

        // --- Verifications de largeur ---
        requireWidth1(clk, clkNode, "horloge ('on ...')");
        requireWidth1(sr, srNode, "set/reset ('when ...')");
        if (en != null) {
            requireWidth1(en, enNode, "enable ('enabled when ...')");
        }
        int width = lhsSubset.isVector() ? lhsSubset.width() : 1;
        if (data.width() != width) {
            throw new ConversionException(dataNode.startOffset(), "SumOfTermsCompound",
                ConversionException.Reason.VECTOR_WIDTH_MISMATCH,
                "LHS de largeur " + width + " mais data de largeur " + data.width());
        }

        // --- Signaux partages : clk, nclk, sr, (en) ---
        List<Erwan> plan = new ArrayList<>();
        String sp = names.fresh();
        String clkSig = sp + "clk";
        String nclkSig = sp + "nclk";
        String srSig = sp + "sr";
        String enSig = sp + "en";
        plan.add(Erwan.AFFECTATION(clkSig, clk.bits().get(0)));
        plan.add(Erwan.AFFECTATION(nclkSig, Erwan.NOT(Erwan.LITTERAL(clkSig))));
        plan.add(Erwan.AFFECTATION(srSig, sr.bits().get(0)));
        if (en != null) {
            plan.add(Erwan.AFFECTATION(enSig, en.bits().get(0)));
        }

        // --- Pre-generation des prefixes par bit ---
        // Les noms qs doivent etre connus AVANT de reecrire les auto-references
        // de data (cf. spec section 3.4).
        String[] bitPrefix = new String[width];
        String[] qsByBit = new String[width];
        for (int k = 0; k < width; k++) {
            bitPrefix[k] = names.fresh();
            qsByBit[k] = bitPrefix[k] + "qs";
        }

        // --- Une bascule maitre-esclave par bit du LHS ---
        for (int k = 0; k < width; k++) {
            String bp = bitPrefix[k];
            String d = bp + "d";
            String mS = bp + "mS";
            String mR = bp + "mR";
            String qm = bp + "qm";
            String nqm = bp + "nqm";
            String sS = bp + "sS";
            String sR = bp + "sR";
            String qs = bp + "qs";
            String nqs = bp + "nqs";

            // d = data[k], avec reecriture des auto-references du LHS vers qs
            // (cf. spec section 3.4) : la sortie reste uniquement generee.
            Erwan dataBit = rewriteSelfRef(data.bits().get(k), lhsName, lhsSubset, qsByBit);
            plan.add(Erwan.AFFECTATION(d, dataBit));

            // gating du maitre : mS = d * nclk [* en], mR = /d * nclk [* en]
            List<Erwan> mSin = new ArrayList<>();
            mSin.add(Erwan.LITTERAL(d));
            mSin.add(Erwan.LITTERAL(nclkSig));
            List<Erwan> mRin = new ArrayList<>();
            mRin.add(Erwan.NOT(Erwan.LITTERAL(d)));
            mRin.add(Erwan.LITTERAL(nclkSig));
            if (en != null) {
                mSin.add(Erwan.LITTERAL(enSig));
                mRin.add(Erwan.LITTERAL(enSig));
            }
            plan.add(Erwan.AFFECTATION(mS, Erwan.AND(mSin)));
            plan.add(Erwan.AFFECTATION(mR, Erwan.AND(mRin)));

            // latch maitre avec forcage set/reset
            plan.add(Erwan.AFFECTATION(qm, latchQ(nqm, mR, srSig, mode)));
            plan.add(Erwan.AFFECTATION(nqm, latchNQ(qm, mS, srSig, mode)));

            // gating de l'esclave : sS = qm * clk, sR = /qm * clk
            plan.add(Erwan.AFFECTATION(sS, Erwan.AND(List.of(
                Erwan.LITTERAL(qm), Erwan.LITTERAL(clkSig)))));
            plan.add(Erwan.AFFECTATION(sR, Erwan.AND(List.of(
                Erwan.NOT(Erwan.LITTERAL(qm)), Erwan.LITTERAL(clkSig)))));

            // latch esclave avec forcage set/reset
            plan.add(Erwan.AFFECTATION(qs, latchQ(nqs, sR, srSig, mode)));
            plan.add(Erwan.AFFECTATION(nqs, latchNQ(qs, sS, srSig, mode)));

            // sortie : Q_i = qs
            if (!lhsSubset.isVector()) {
                plan.add(Erwan.AFFECTATION(lhsName, Erwan.LITTERAL(qs)));
            } else {
                int idx = lhsSubset.minIndex() + k;
                plan.add(Erwan.AFFECTATION(lhsName, idx, Erwan.LITTERAL(qs)));
            }
        }
        return plan;
    }

    /**
     * Reecrit, dans une expression {@code data}, les references (LITTERAL) au
     * signal LHS vers le noeud esclave interne {@code qs} du bit correspondant.
     *
     * <p>Garantit que la sortie du module reste uniquement generee (jamais
     * relue) : {@code FileSimulateur(Module.Plan)} la conserve alors en sortie.
     * Voir spec section 3.4. Une expression qui ne mentionne pas le LHS est
     * laissee intacte.
     *
     * @param qsByBit noms des noeuds qs, indexes par bit du LHS (bit 0 = LSB)
     */
    private static Erwan rewriteSelfRef(Erwan node, String lhsName, Subset lhsSubset,
            String[] qsByBit) {
        switch (node.Op) {
            case LITTERAL:
                if (lhsName.equals(node.Nom)) {
                    if (!lhsSubset.isVector() && node.Numero == null) {
                        return Erwan.LITTERAL(qsByBit[0]);
                    }
                    if (lhsSubset.isVector() && node.Numero != null) {
                        int bit = node.Numero - lhsSubset.minIndex();
                        if (bit >= 0 && bit < qsByBit.length) {
                            return Erwan.LITTERAL(qsByBit[bit]);
                        }
                    }
                }
                return node;
            case NOT:
                return Erwan.NOT(rewriteSelfRef(node.Entrees.get(0),
                    lhsName, lhsSubset, qsByBit));
            case AND: {
                List<Erwan> r = new ArrayList<>();
                for (Erwan e : node.Entrees) {
                    r.add(rewriteSelfRef(e, lhsName, lhsSubset, qsByBit));
                }
                return Erwan.AND(r);
            }
            case OR: {
                List<Erwan> r = new ArrayList<>();
                for (Erwan e : node.Entrees) {
                    r.add(rewriteSelfRef(e, lhsName, lhsSubset, qsByBit));
                }
                return Erwan.OR(r);
            }
            default:
                return node;
        }
    }

    /**
     * Sortie {@code q} d'un latch NOR avec forcage.
     * reset : {@code q = /nq * /r * /sr} ; set : {@code q = /nq * /r + sr}.
     */
    private static Erwan latchQ(String nq, String r, String sr, Mode mode) {
        if (mode == Mode.RESET) {
            return Erwan.AND(List.of(
                Erwan.NOT(Erwan.LITTERAL(nq)),
                Erwan.NOT(Erwan.LITTERAL(r)),
                Erwan.NOT(Erwan.LITTERAL(sr))));
        }
        return Erwan.OR(List.of(
            Erwan.AND(List.of(Erwan.NOT(Erwan.LITTERAL(nq)), Erwan.NOT(Erwan.LITTERAL(r)))),
            Erwan.LITTERAL(sr)));
    }

    /**
     * Sortie complementee {@code nq} d'un latch NOR avec forcage.
     * reset : {@code nq = /q * /s + sr} ; set : {@code nq = /q * /s * /sr}.
     */
    private static Erwan latchNQ(String q, String s, String sr, Mode mode) {
        if (mode == Mode.RESET) {
            return Erwan.OR(List.of(
                Erwan.AND(List.of(Erwan.NOT(Erwan.LITTERAL(q)), Erwan.NOT(Erwan.LITTERAL(s)))),
                Erwan.LITTERAL(sr)));
        }
        return Erwan.AND(List.of(
            Erwan.NOT(Erwan.LITTERAL(q)),
            Erwan.NOT(Erwan.LITTERAL(s)),
            Erwan.NOT(Erwan.LITTERAL(sr))));
    }

    /** ResetKW -> RESET, SetKW -> SET. */
    private static Mode m_mode(CstNode sorNode) {
        if (!(sorNode instanceof CstInternal sor) || sor.nt() != NonTerminal.Set_Or_Reset) {
            throw malformed(sorNode, "Attendu Set_Or_Reset");
        }
        return sor.has(new Terminal(Token.SetKW)) ? Mode.SET : Mode.RESET;
    }

    private static void requireWidth1(Bus bus, CstNode node, String role) {
        if (bus.width() != 1) {
            throw new ConversionException(node.startOffset(), "SumOfTerms",
                ConversionException.Reason.VECTOR_WIDTH_MISMATCH,
                "L'expression " + role + " doit etre scalaire mais est de largeur "
                    + bus.width());
        }
    }

    private static ConversionException malformed(CstNode node, String message) {
        return new ConversionException(node.startOffset(), String.valueOf(node.symbol()),
            ConversionException.Reason.MALFORMED_CST, message);
    }
}
```

- [ ] **Step 2 : Compiler**

Run : `./build.sh test`
Expected : `BUILD` réussit (la classe n'est pas encore appelée, mais doit compiler).

- [ ] **Step 3 : Commit**

```bash
git add parser/conversion/MemoryAssignmentBuilder.java
git commit -m "feat(conversion): MemoryAssignmentBuilder, desugar := en maitre-esclave"
```

---

## Task 4 : Câbler `MemoryAssignmentBuilder` dans `ModuleBuilder`

Ajoute le pré-passage de collecte de noms et remplace le rejet `MEMORY_ASSIGNMENT_NOT_SUPPORTED` par l'appel au builder.

**Files:**
- Modify: `parser/conversion/ModuleBuilder.java` (imports, `build`, `buildInstance`)

- [ ] **Step 1 : Ajouter le pré-passage de collecte de noms**

Aucun nouvel import n'est nécessaire : `HashSet`, `Set`, `CstLeaf`, `CstInternal`, `CstNode` sont déjà importés dans `ModuleBuilder.java`.

Dans `parser/conversion/ModuleBuilder.java`, juste avant la méthode `buildSignature` (ligne 103, `private static Signature buildSignature`), insérer :

```java
    /**
     * Pre-passage : collecte le texte de toutes les feuilles du sous-arbre du
     * module. Sur-collecte volontairement (mots-cles, operateurs inclus) — un
     * sur-ensemble ne fait que rendre la generation de noms frais plus stricte,
     * jamais incorrecte.
     */
    private static Set<String> collectLeafTexts(CstNode node) {
        Set<String> acc = new HashSet<>();
        collectLeafTexts(node, acc);
        return acc;
    }

    private static void collectLeafTexts(CstNode node, Set<String> acc) {
        if (node instanceof CstLeaf leaf) {
            acc.add(leaf.lexem().getText());
        } else if (node instanceof CstInternal inter) {
            for (CstNode child : inter.children()) {
                collectLeafTexts(child, acc);
            }
        }
    }
```

- [ ] **Step 1 : Créer le `FreshNames` dans `build` et le propager**

Dans `parser/conversion/ModuleBuilder.java`, méthode `build`, après la ligne 44 (`Set<String> lhsSeen = new HashSet<>();`), ajouter :

```java
        FreshNames freshNames = new FreshNames(collectLeafTexts(mod));
```

Puis modifier les **deux** appels à `buildInstance` (lignes 56 et 72) pour passer `freshNames` en dernier argument. L'appel ligne 56 devient :

```java
        plan.addAll(buildInstance(ip.first(NonTerminal.Instance).orElseThrow(() ->
            new ConversionException(ip.startOffset(), "Instance_Plus",
                ConversionException.Reason.MALFORMED_CST,
                "Instance_Plus sans enfant Instance")), lhsSeen, resolver, branchements,
            freshNames));
```

L'appel ligne 72 devient :

```java
            plan.addAll(buildInstance(star.first(NonTerminal.Instance).orElseThrow(() ->
                new ConversionException(starOffset, "Instance_Star",
                    ConversionException.Reason.MALFORMED_CST,
                    "Instance_Star non-epsilon sans enfant Instance")), lhsSeen, resolver,
                branchements, freshNames));
```

- [ ] **Step 1 : Ajouter le paramètre `FreshNames` à `buildInstance`**

Dans `parser/conversion/ModuleBuilder.java`, modifier la signature de `buildInstance` (ligne 197) :

```java
    private static List<Erwan> buildInstance(CstNode instanceNode, Set<String> lhsSeen,
            ModuleResolver resolver, List<AppelModule> branchements, FreshNames freshNames) {
```

- [ ] **Step 1 : Remplacer le rejet `MEMORY_ASSIGNMENT_NOT_SUPPORTED`**

Dans `parser/conversion/ModuleBuilder.java`, remplacer le bloc lignes 290-294 :

```java
        // Assignment -> SignalAssignment | MemoryAssignment
        if (assignment.has(NonTerminal.MemoryAssignment)) {
            throw new ConversionException(assignment.startOffset(), "Assignment",
                ConversionException.Reason.MEMORY_ASSIGNMENT_NOT_SUPPORTED,
                "Affectation memoire (:=) non supportee en S1 (offset " + assignment.startOffset() + ")");
        }
```

par :

```java
        // Assignment -> SignalAssignment | MemoryAssignment
        if (assignment.has(NonTerminal.MemoryAssignment)) {
            CstNode memNode = assignment.first(NonTerminal.MemoryAssignment).orElseThrow(() ->
                new ConversionException(assignment.startOffset(), "Assignment",
                    ConversionException.Reason.MALFORMED_CST,
                    "Assignment sans enfant MemoryAssignment"));
            return MemoryAssignmentBuilder.build(memNode, nom, lhsSubset, freshNames);
        }
```

(Le LHS a déjà été enregistré dans `lhsSeen` aux lignes 272-278 — la déduplication `DUPLICATE_LHS` reste donc active pour un `:=`.)

- [ ] **Step 1 : Compiler**

Run : `./build.sh test`
Expected : `BUILD` réussit.

- [ ] **Step 1 : Vérifier la non-régression**

Run : `./run.sh`
Expected : tous les tests existants passent (aucune régression — la batterie actuelle ne contient aucun `:=` valide).

- [ ] **Step 1 : Commit**

```bash
git add parser/conversion/ModuleBuilder.java
git commit -m "feat(conversion): cabler MemoryAssignmentBuilder, lever le rejet :="
```

---

## Task 5 : Tests e2e de simulation

Valide le comportement séquentiel via `FileSimulateur` : capture, maintien, reset, set, enable, toggle/compteur, vecteur, sous-module, et les rejets typés.

**Files:**
- Create: `tests/parser/conversion/MemoryAssignmentTest.java`

- [ ] **Step 1 : Écrire la classe de test complète**

Créer `tests/parser/conversion/MemoryAssignmentTest.java` :

```java
package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.conversion.ConversionException;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Etat;
import simulateur.FileSimulateur;
import erwan.Module;

/**
 * Tests e2e de l'affectation memoire := : pipeline String SHDL ->
 * FileSimulateur. Voir docs/specs/2026-05-18-conversion-sequentiel-design.md.
 */
public class MemoryAssignmentTest {

    // --- Harnais : resolution des E/S par nom ---

    private static FileSimulateur build(String src) throws Exception {
        CstNode cst = CstParser.parse(src);
        Module m = Conversion.convert(cst);
        return new FileSimulateur(m.Plan);
    }

    private static int idxE(FileSimulateur fs, String n) {
        for (int i = 1; i <= fs.nbEntree(); i++) {
            if (n.equals(fs.nomEntree(i))) return i;
        }
        fail("entree introuvable : " + n);
        return -1;
    }

    private static int idxS(FileSimulateur fs, String n) {
        for (int i = 1; i <= fs.nbSorties(); i++) {
            if (n.equals(fs.nomSortie(i))) return i;
        }
        fail("sortie introuvable : " + n);
        return -1;
    }

    private static void set(FileSimulateur fs, int idx, int v) {
        fs.getEntrees(idx, 1).set(v == 1 ? Etat.UP : Etat.DW);
    }

    private static int read(FileSimulateur fs, int idx) {
        Etat e = fs.getSorties(idx, 1).getValeur();
        return e == Etat.UP ? 1 : e == Etat.DW ? 0 : -1; // -1 = ND
    }

    /** Bascule D nue avec reset asynchrone. */
    private static final String DFF =
        "module dff (D, clk, rst : Q) "
        + "Q := D on clk, reset when rst "
        + "end module";

    /** Bascule D avec enable. */
    private static final String DFFE =
        "module dffe (D, clk, rst, en : Q) "
        + "Q := D on clk, reset when rst, enabled when en "
        + "end module";

    // --- Tests ---

    @Test
    public void captureSurFrontMontant() throws Exception {
        FileSimulateur fs = build(DFF);
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 0); set(fs, rst, 1); set(fs, rst, 0);
        set(fs, D, 1); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("capture de 1 sur front montant", 1, read(fs, Q));
        set(fs, D, 0); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("capture de 0 sur front montant", 0, read(fs, Q));
    }

    @Test
    public void maintienPendantClkHaut() throws Exception {
        FileSimulateur fs = build(DFF);
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 1); set(fs, rst, 1); set(fs, rst, 0);
        set(fs, clk, 1);                       // front montant, capture 1
        assertEquals(1, read(fs, Q));
        set(fs, D, 0);                         // D change pendant clk haut
        assertEquals("Q doit rester 1 (pas transparent)", 1, read(fs, Q));
    }

    @Test
    public void resetAsynchroneForceZero() throws Exception {
        FileSimulateur fs = build(DFF);
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 1); set(fs, rst, 1); set(fs, rst, 0);
        set(fs, clk, 1); set(fs, clk, 0);      // Q = 1
        assertEquals(1, read(fs, Q));
        set(fs, rst, 1);                       // reset asynchrone, clk bas
        assertEquals("reset force Q a 0 sans front", 0, read(fs, Q));
    }

    @Test
    public void setAsynchroneForceUn() throws Exception {
        FileSimulateur fs = build(
            "module dffs (D, clk, s : Q) "
            + "Q := D on clk, set when s "
            + "end module");
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), s = idxE(fs, "s");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 0); set(fs, s, 1); set(fs, s, 0);
        set(fs, clk, 1); set(fs, clk, 0);      // Q = 0
        assertEquals(0, read(fs, Q));
        set(fs, s, 1);                         // set asynchrone
        assertEquals("set force Q a 1 sans front", 1, read(fs, Q));
    }

    @Test
    public void enableCaptureEtMaintien() throws Exception {
        FileSimulateur fs = build(DFFE);
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst"), en = idxE(fs, "en");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 0); set(fs, en, 1); set(fs, rst, 1); set(fs, rst, 0);
        set(fs, D, 1); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("en=1 : capture", 1, read(fs, Q));
        set(fs, en, 0); set(fs, D, 0); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("en=0 : maintien", 1, read(fs, Q));
        set(fs, en, 1); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("en=1 : capture de 0", 0, read(fs, Q));
    }

    @Test
    public void resetDomineEnable() throws Exception {
        FileSimulateur fs = build(DFFE);
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst"), en = idxE(fs, "en");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 1); set(fs, en, 1); set(fs, rst, 1); set(fs, rst, 0);
        set(fs, clk, 1); set(fs, clk, 0);      // Q = 1
        assertEquals(1, read(fs, Q));
        set(fs, en, 0);                        // enable desactive
        set(fs, rst, 1);                       // reset doit dominer malgre en=0
        assertEquals("reset async domine l'enable", 0, read(fs, Q));
    }

    @Test
    public void toggleCompteur() throws Exception {
        // Q := /Q : diviseur de frequence, rebouclage Q->D.
        FileSimulateur fs = build(
            "module toggle (clk, rst : Q) "
            + "Q := /Q on clk, reset when rst "
            + "end module");
        int clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, rst, 1); set(fs, rst, 0);
        assertEquals(0, read(fs, Q));
        set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("cycle 1", 1, read(fs, Q));
        set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("cycle 2", 0, read(fs, Q));
        set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("cycle 3", 1, read(fs, Q));
    }

    @Test
    public void registreVectoriel() throws Exception {
        // q[1..0] := d[1..0] : registre 2 bits.
        FileSimulateur fs = build(
            "module reg (d[1..0], clk, rst : q[1..0]) "
            + "q[1..0] := d[1..0] on clk, reset when rst "
            + "end module");
        int d0 = idxE(fs, "d[0]"), d1 = idxE(fs, "d[1]");
        int clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int q0 = idxS(fs, "q[0]"), q1 = idxS(fs, "q[1]");
        set(fs, clk, 0); set(fs, d0, 0); set(fs, d1, 0);
        set(fs, rst, 1); set(fs, rst, 0);
        set(fs, d0, 1); set(fs, d1, 0); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("bit 0 capture 1", 1, read(fs, q0));
        assertEquals("bit 1 capture 0", 0, read(fs, q1));
        set(fs, d0, 0); set(fs, d1, 1); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("bit 0 capture 0", 0, read(fs, q0));
        assertEquals("bit 1 capture 1", 1, read(fs, q1));
    }

    @Test
    public void deuxBasculesIndependantes() throws Exception {
        FileSimulateur fs = build(
            "module deux (a, b, clk, rst : X, Y) "
            + "X := a on clk, reset when rst "
            + "Y := b on clk, reset when rst "
            + "end module");
        int a = idxE(fs, "a"), b = idxE(fs, "b");
        int clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int X = idxS(fs, "X"), Y = idxS(fs, "Y");
        set(fs, clk, 0); set(fs, a, 0); set(fs, b, 0);
        set(fs, rst, 1); set(fs, rst, 0);
        set(fs, a, 1); set(fs, b, 0); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals(1, read(fs, X));
        assertEquals(0, read(fs, Y));
    }

    @Test
    public void horlogeNonResetee_resteND() throws Exception {
        // Sans front de reset, l'etat de la bascule est ND (non defini).
        FileSimulateur fs = build(DFF);
        int Q = idxS(fs, "Q");
        assertEquals("bascule jamais resetee : etat ND", -1, read(fs, Q));
    }

    @Test
    public void rejetHorlogeVectorielle() throws Exception {
        try {
            build("module bad (D, c[1..0], rst : Q) "
                + "Q := D on c[1..0], reset when rst "
                + "end module");
            fail("une horloge vectorielle doit etre rejetee");
        } catch (ConversionException e) {
            assertEquals(ConversionException.Reason.VECTOR_WIDTH_MISMATCH, e.reason());
        }
    }

    @Test
    public void rejetDataMauvaiseLargeur() throws Exception {
        try {
            build("module bad (d[1..0], clk, rst : Q) "
                + "Q := d[1..0] on clk, reset when rst "
                + "end module");
            fail("data de largeur 2 sur LHS scalaire doit etre rejete");
        } catch (ConversionException e) {
            assertEquals(ConversionException.Reason.VECTOR_WIDTH_MISMATCH, e.reason());
        }
    }
}
```

- [ ] **Step 2 : Compiler et lancer les tests**

Run : `./build.sh test` puis
`java -cp bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore tests.parser.conversion.MemoryAssignmentTest`
Expected : `OK (13 tests)`.

> Si un test de simulation échoue (capture/toggle), c'est le signal d'alerte de la spec §2.1 (convergence du simulateur). Ne pas masquer l'échec : le rapporter — il remettrait en cause l'Approche 1 au profit de la primitive IR (spec §2.3).

- [ ] **Step 3 : Vérifier que la syntaxe `:=` des fixtures est correcte**

Si `CstParser.parse` lève une `ParsingException` sur un module de test, vérifier la syntaxe `:=` contre la grammaire (`Grammar.java:169`) et les mots-clés `on` / `when` / `reset` / `set` / `enabled`. Corriger la chaîne SHDL du test, pas le parser.

- [ ] **Step 4 : Commit**

```bash
git add tests/parser/conversion/MemoryAssignmentTest.java
git commit -m "test(conversion): tests e2e de l'affectation memoire :="
```

---

## Task 6 : Mettre à jour le README

**Files:**
- Modify: `README.md`

- [ ] **Step 1 : Retirer `:=` du hors-scope**

Dans `README.md`, section « Périmètre du convertisseur » (lignes ~61-64), remplacer :

```
**Périmètre du convertisseur** : combinatoire scalaire (params scalaires,
affectations `=`, opérateurs `+ * /`) **et appel de sous-modules** (voir
ci-dessous). Hors-scope (vecteurs, `&`, `:=`, littéraux dans RHS) →
`ConversionException` typée.
```

par :

```
**Périmètre du convertisseur** : combinatoire scalaire et vectoriel,
**appel de sous-modules** et **affectation mémoire `:=`** (bascule D — voir
ci-dessous). Hors-scope (`&`, littéraux dans RHS, FSM, tables de vérité) →
`ConversionException` typée.
```

- [ ] **Step 2 : Ajouter la section « Affectation mémoire (`:=`) »**

Dans `README.md`, juste avant la section `## Arborescence` (ligne ~102), insérer :

```markdown
## Affectation mémoire (`:=`)

`signal := data on clk, {set|reset} when sr [, enabled when en]` : une
bascule D synchrone. La conversion la *desugar* en bascule maître-esclave
combinatoire (`AND`/`OR`/`NOT`), une par bit du LHS — **sans moteur
séquentiel dédié** : l'IR `erwan` et `FileSimulateur` sont inchangés.

⚠️ **État initial** : une bascule n'a pas d'état défini tant que son
`set`/`reset` n'a pas reçu de front — sa sortie vaut `ND`. Pulser le
set/reset pour amorcer le circuit.

Voir `docs/specs/2026-05-18-conversion-sequentiel-design.md`.
```

- [ ] **Step 3 : Mettre à jour l'arborescence**

Dans `README.md`, section `## Arborescence`, dans le bloc `conversion/`, ajouter après la ligne `ModuleBuilder.java  ModuleCallBuilder.java   # corps + appels $call` :

```
        MemoryAssignmentBuilder.java  FreshNames.java   # desugar :=
```

- [ ] **Step 4 : Commit**

```bash
git add README.md
git commit -m "docs(readme): documenter l'affectation memoire :="
```

---

## Task 7 : Validation finale

- [ ] **Step 1 : Batterie complète**

Run : `rm -rf bin/* && ./build.sh test && ./run.sh`
Expected : toute la batterie passe, y compris `MemoryAssignmentTest` et `FreshNamesTest`, aucune régression.

- [ ] **Step 2 : Commit éventuel**

Si `./run.sh` a révélé un ajustement nécessaire, le corriger et committer avec un message `fix(conversion): ...`. Sinon, rien à faire.

---

## Notes pour l'implémenteur

- **Ne jamais modifier** `erwan/` ni `simulateur/` (code d'un coéquipier). Si un test échoue à cause d'un comportement du simulateur, le rapporter — ne pas patcher le simulateur.
- **Propager les exceptions** : aucun `printStackTrace` / `System.err`. Les `ConversionException` typées remontent jusqu'à l'appelant.
- `MEMORY_ASSIGNMENT_NOT_SUPPORTED` reste défini dans `ConversionException.Reason` (pas de rupture d'API) mais n'est plus levé.
- L'ordre des bits d'un bus `ExpressionBuilder` est `lo→hi` ; le bit `data[k]` est apparié à l'indice `lhsSubset.minIndex() + k` du LHS — cohérent avec `Erwan.ARANGE` du chemin `=` existant.
