# FSM hors scope MVP — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Retirer FSM du périmètre runtime du MVP en levant une `UnsupportedOperationException` explicite au niveau de l'interpréteur, sans toucher à la grammaire, au parser ni à l'AST.

**Architecture:** Modification unique dans `mvp/Interpreteur.java` (check explicite sur `Fsm` avant la branche Assignment). Restauration de deux fichiers de tests FSM supprimés par erreur avant cette session (`tests/parser/ll1/parser/ParserFsmTest.java` et `ParserFsmErrorTest.java`) pour rester cohérent avec le choix « on garde parser+grammaire+AST FSM intacts ». Ajout d'un cas dans `mvp/SmokeTest.java` pour vérifier le refus. Mise à jour de la section Alexis du bilan sprint 1.

**Tech Stack:** Java 21+, JUnit 4.13.2, scripts `./build.sh` / `./run.sh`.

---

## Fichiers touchés

- Restaurer (depuis HEAD, supprimés sur le working tree) :
  - `tests/parser/ll1/parser/ParserFsmTest.java`
  - `tests/parser/ll1/parser/ParserFsmErrorTest.java`
- Modifier :
  - `mvp/Interpreteur.java` : ajouter check `Fsm` en début de `construire()`.
  - `mvp/SmokeTest.java` : ajouter un cas de rejet FSM.
  - `docs/sprint1/2026-04-13-bilan-sprint1.md` : reformuler la mention TERM_REST / FSM.

---

### Task 1 : Restaurer les deux fichiers de tests FSM supprimés

**Files:**
- Restore: `tests/parser/ll1/parser/ParserFsmTest.java`
- Restore: `tests/parser/ll1/parser/ParserFsmErrorTest.java`

- [ ] **Step 1 : Vérifier l'état**

```bash
git status tests/parser/ll1/parser/ParserFsm*.java
```

Attendu : deux lignes `deleted:` (non stagées).

- [ ] **Step 2 : Restaurer depuis HEAD**

```bash
git restore tests/parser/ll1/parser/ParserFsmTest.java tests/parser/ll1/parser/ParserFsmErrorTest.java
```

- [ ] **Step 3 : Vérifier que les fichiers existent**

```bash
ls tests/parser/ll1/parser/ParserFsm*.java
```

Attendu : les deux fichiers listés.

- [ ] **Step 4 : Lancer les tests pour vérifier la compilation**

```bash
./run.sh test 2>&1 | grep -E "ParserFsm|OK \(" | tail -10
```

Attendu : `ParserFsmTest` → `OK (2 tests)` et `ParserFsmErrorTest` → `OK (5 tests)`.

- [ ] **Step 5 : Commit**

```bash
git add tests/parser/ll1/parser/ParserFsmTest.java tests/parser/ll1/parser/ParserFsmErrorTest.java
git commit -m "tests: restaure les fichiers FSM supprimes par erreur

Ces deux fichiers avaient ete supprimes du working tree (avant cette
session) mais la suppression n etait pas committee. Option B pour FSM
(refus au niveau interpreteur seulement) suppose que le parser+grammaire
+AST FSM restent intacts, donc les tests egalement.

Co-Authored-By: Claude Opus 4.6 (1M context) <noreply@anthropic.com>"
```

---

### Task 2 : Rejet explicite des FSM dans l'Interpreteur (test-first)

**Files:**
- Modify: `mvp/SmokeTest.java` (ajout d'un cas de refus FSM, avant impl)
- Modify: `mvp/Interpreteur.java:45-72` (check Fsm avant la boucle)

Rationale : pas de JUnit pour `mvp/` (le pattern actuel dans ce module est un smoke test `main()` avec assertions maison). On suit le pattern existant.

- [ ] **Step 1 : Ajouter le cas de test qui échoue dans SmokeTest**

Éditer `mvp/SmokeTest.java` — ajouter une méthode `verifRejetFsm` et son appel dans `main`.

Dans `main(String[] args)`, après la dernière ligne `verif(p, notShdl, "a=DW", "b", Etat.UP);` et avant `System.out.println("Smoke test OK");`, ajouter :

```java
        String fsmShdl =
            "module FEUX(clk, reset : rouge)\n" +
            "  fsm synchronous on clk, s0 when reset\n" +
            "    s0 -> s0 when reset\n" +
            "  end fsm\n" +
            "  rouge = 0\n" +
            "end module";
        verifRejetFsm(p, fsmShdl);
```

Puis ajouter la méthode auxiliaire à la fin de la classe (avant l'accolade fermante finale) :

```java
    private static void verifRejetFsm(Pilote p, String shdl) {
        try {
            p.simuler(shdl, new LinkedHashMap<>());
            throw new AssertionError("FSM aurait du etre refusee par l'interpreteur");
        } catch (UnsupportedOperationException e) {
            if (e.getMessage() == null || !e.getMessage().contains("FSM hors scope")) {
                throw new AssertionError(
                    "message attendu contient 'FSM hors scope', recu : " + e.getMessage());
            }
            System.out.println("  [OK] FSM correctement refusee : " + e.getMessage());
        } catch (Exception e) {
            throw new AssertionError(
                "exception attendue UnsupportedOperationException, recu " + e.getClass().getSimpleName()
                + " : " + e.getMessage());
        }
    }
```

Note : si la signature exacte de `Pilote.simuler(...)` diffère, la lire via `head -50 mvp/Pilote.java` et adapter l'appel — la méthode publique qui prend un SHDL et une map d'entrées doit exister (utilisée plus haut dans le même fichier par `verif`). Si nécessaire, s'aligner exactement sur la manière dont `verif(...)` déclenche le pipeline.

- [ ] **Step 2 : Compiler et lancer — doit échouer**

```bash
./build.sh && java --module-path lib/javafx-sdk-21 --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" mvp.SmokeTest
```

Attendu : `AssertionError` (soit "FSM aurait du etre refusee", soit une autre exception remontée — typiquement `IllegalArgumentException` "MVP : seul Assignment est supporte (recu Fsm)").

- [ ] **Step 3 : Implémenter le check explicite dans Interpreteur**

Éditer `mvp/Interpreteur.java`. Dans la méthode `construire(Module module)`, **juste après** la boucle des params (ligne 51, après `}` fermant `for (Signal p : module.getParams())`) et **avant** la boucle `for (Instance inst : module.getInstances())`, insérer :

```java
        for (Instance inst : module.getInstances()) {
            if (inst instanceof parser.ll1.ast.Fsm) {
                throw new UnsupportedOperationException(
                    "FSM hors scope MVP sprint 1 (cf. docs/sprint1/2026-04-13-bilan-sprint1.md). "
                    + "Le parser LL(1) accepte la syntaxe FSM mais l'interpreteur la refuse tant "
                    + "que l'axe FSM n'est pas repris.");
            }
        }
```

Le `for` existant (qui commence ligne 53) reste inchangé en dessous. On a donc **deux** boucles successives : la première vérifie l'absence de FSM, la seconde fait la traduction.

Pourquoi une passe dédiée plutôt que fusionner dans le `for` existant : séparer la validation de la traduction donne un message d'erreur clair avant que le code d'Assignment ne bute sur autre chose. Coût : une boucle `O(n)` en plus, négligeable.

- [ ] **Step 4 : Rebuild et relancer — doit réussir**

```bash
./build.sh && java --module-path lib/javafx-sdk-21 --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" mvp.SmokeTest
```

Attendu : sortie se terminant par
```
  [OK] FSM correctement refusee : FSM hors scope MVP sprint 1 (...)
Smoke test OK
```

- [ ] **Step 5 : Vérifier qu'aucun autre test n'est cassé**

```bash
./run.sh test 2>&1 | tail -30
```

Attendu : tous les blocs de tests se terminent par `OK (...)`, aucun `FAILURES!!!`.

- [ ] **Step 6 : Commit**

```bash
git add mvp/Interpreteur.java mvp/SmokeTest.java
git commit -m "mvp: refuse explicitement les FSM au niveau interpreteur

Le parser LL(1) accepte la syntaxe FSM (SCRUM-23 couvre tout SHDL),
mais l'axe FSM est hors scope pour la demo sprint 1. Le check est
localise dans mvp/Interpreteur.java et leve UnsupportedOperationException
avec un message qui pointe vers le bilan sprint 1.

Ajoute un smoke test qui verifie qu'un SHDL contenant une FSM declenche
bien cette exception.

Co-Authored-By: Claude Opus 4.6 (1M context) <noreply@anthropic.com>"
```

---

### Task 3 : Mise à jour du bilan sprint 1

**Files:**
- Modify: `docs/sprint1/2026-04-13-bilan-sprint1.md:11` (section Alexis, ligne « Limite connue »)

- [ ] **Step 1 : Remplacer la ligne « Limite connue »**

Remplacer :

```markdown
**Limite connue** : piège grammatical `TERM_REST` (wildcard FSM + `when` suivi d'un STAR de règle suivante) documenté par un test, correction reportée au sprint 2.
```

Par :

```markdown
**Choix de scope (2026-04-14)** : FSM hors scope du MVP sprint 1. Le parser LL(1) accepte la syntaxe FSM (test `ParserFsmTest` + `ParserFsmErrorTest`), mais `mvp/Interpreteur.construire()` refuse tout module contenant un nœud `Fsm` avec `UnsupportedOperationException("FSM hors scope MVP sprint 1 ...")`. Conséquence pratique : le conflit LL(1) `TERM_REST` (wildcard FSM suivant un `when`), documenté par `Ll1ConflictTest`, reste présent dans la grammaire mais n'a aucune incidence runtime sur la démo — aucun `.shdl` exécuté par le MVP ne contient de FSM. Réouverture FSM = implémenter la traduction AST→circuit, rien à refaire côté parser.
```

- [ ] **Step 2 : Vérifier le rendu Markdown**

```bash
grep -A3 "2026-04-14" docs/sprint1/2026-04-13-bilan-sprint1.md
```

Attendu : le nouveau paragraphe s'affiche correctement (une ligne continue, markdown bold intact).

- [ ] **Step 3 : Commit**

```bash
git add docs/sprint1/2026-04-13-bilan-sprint1.md
git commit -m "docs(bilan): reformule TERM_REST en choix de scope FSM

La ligne 'Limite connue : piege grammatical TERM_REST' pouvait laisser
croire a un bug a corriger. La nouvelle formulation explicite que FSM
est hors scope MVP par design (refus cote interpreteur), et que
TERM_REST est neutralise en pratique.

Co-Authored-By: Claude Opus 4.6 (1M context) <noreply@anthropic.com>"
```

---

## Vérification finale

- [ ] **Build propre**

```bash
rm -rf bin && ./build.sh test 2>&1 | tail -5
```

Attendu : `>> Build OK  (~140 classes compilees)`, aucun `error:`.

- [ ] **Tous les tests passent**

```bash
./run.sh test 2>&1 | grep -cE "FAILURES|failed"
```

Attendu : `0`.

- [ ] **Smoke test OK**

```bash
java --module-path lib/javafx-sdk-21 --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "bin:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" mvp.SmokeTest | tail -3
```

Attendu : `[OK] FSM correctement refusee : ...` suivi de `Smoke test OK`.

- [ ] **Git status propre**

```bash
git status
```

Attendu : `rien à valider, la copie de travail est propre` (ou seulement des fichiers non suivis attendus type `bin/`).
