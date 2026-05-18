# Conversion séquentielle — affectation mémoire `:=`

**Date** : 2026-05-18
**Axe** : conversion CST → IR `erwan`
**Statut** : design validé, prêt pour plan d'implémentation

## 1. Objectif

Lever le rejet `MEMORY_ASSIGNMENT_NOT_SUPPORTED` et convertir l'affectation
mémoire SHDL `:=` (bascule D synchrone) en circuit simulable, **sans modifier
l'IR `erwan` ni le simulateur `FileSimulateur`** (code de Mati).

Périmètre : sémantique `:=` **complète** — donnée, horloge, set/reset
asynchrone, enable optionnel, LHS scalaire ou vectoriel.

Hors-scope (inchangé) : FSM, tables de vérité (`map`).

## 2. Principe — desugar en maître-esclave combinatoire

La grammaire (`Grammar.java:169`) :

```
MemoryAssignment ::= MemAssignOp SumOfTermsCompound OnKW SumOfTerms
                     Comma_Opt Set_Or_Reset WhenKW SumOfTerms
                     Enabled_Operand_Opt Semicolon_Opt
```

soit la forme source :

```
lhs := data on clk, {set|reset} when sr [, enabled when en]
```

`ModuleBuilder`, au lieu de lever `MEMORY_ASSIGNMENT_NOT_SUPPORTED`, **expanse**
chaque `MemoryAssignment` en opérations `erwan` (`AND` / `OR` / `NOT` /
`AFFECTATION`) réalisant une bascule D **maître-esclave**, **un exemplaire par
bit du LHS**.

### 2.1 Fondement empirique

Le desugar repose sur un comportement observé de `FileSimulateur` : le
simulateur itère en repartant des valeurs précédentes des signaux, ce qui
permet à un latch à portes croisées de **tenir son état**. Validé par sonde le
2026-05-18 :

- bascule D maître-esclave, `D` externe : capture `0→0`, `1→1`, `0→0` ✓
- toggle auto-rebouclé `D = NQ` (compteur / diviseur de fréquence), avec reset
  asynchrone : `reset→0` puis bascule `1,0,1,0,1,0` — une bascule par cycle ✓

Le cas compteur (rétroaction `Q→D` la plus dure) fonctionne. L'hypothèse est
levée.

### 2.2 Pourquoi pas un vrai moteur séquentiel

Une primitive `Bascule` dans l'IR + un `step()` dans le simulateur serait
sémantiquement plus propre (vrai front d'horloge), mais touche `erwan/` et
`simulateur/` — code de Mati, nécessitant son accord et de la coordination. Le
desugar reste **entièrement côté conversion** (territoire autonome) et est
validé empiriquement. Repli prévu sur la primitive IR uniquement si une
régression de simulation l'imposait.

## 3. Expansion par bit

Pour un bit de LHS de signal de sortie `Q_i`, avec les bus `data`, `clk`, `sr`,
`en` déjà construits :

### 3.1 Donnée effective (mux d'enable)

- Clause `enabled when en` présente : `D_eff = en * data + /en * Q_i`
  (le mux garde l'ancienne valeur de sortie quand `en` est bas).
- Clause absente : `D_eff = data`.

### 3.2 Maître-esclave

Neuf signaux internes (noms frais, cf. §4) :

```
nclk = /clk
mS   = D_eff * nclk
mR   = /D_eff * nclk
qm   = /nqm * /mR        nqm = /qm * /mS        (latch maître, transparent sur /clk)
sS   = qm * clk
sR   = /qm * clk
qs   = /nqs * /sR        nqs = /qs * /sS        (latch esclave, transparent sur clk)
Q_i  = qs
```

### 3.3 Set/reset asynchrone

Termes de forçage ajoutés sur les latchs maître **et** esclave (le forçage
domine l'horloge → priorité asynchrone, conforme au SHDL officiel) :

- `reset when sr` : `q*` reçoit le facteur `* /sr` ; `nq*` reçoit le terme
  `+ sr`. Soit `qm = /nqm * /mR * /sr`, `nqm = /qm * /mS + sr`, idem
  `qs` / `nqs`.
- `set when sr` : symétrique — `q*` reçoit `+ sr`, `nq*` reçoit `* /sr`.

## 4. Noms de signaux internes

Les neuf signaux internes par bascule ne doivent jamais entrer en collision
avec un signal utilisateur. Procédure :

1. Collecter l'ensemble des noms de signaux du module (params + LHS + RHS).
2. Pour chaque expansion, générer un préfixe `__ff<N>__` (compteur `N`
   incrémenté par `:=` et par bit).
3. Si un nom généré appartient à l'ensemble collecté, incrémenter `N` et
   ré-essayer jusqu'à obtenir un préfixe entièrement frais.

## 5. LHS vectoriel

`q[hi..lo] := data on clk, …` : une bascule maître-esclave par bit. `clk`,
`sr`, `en` sont **partagés** entre tous les bits ; `data` est découpé bit à
bit. Contraintes de largeur (cf. §6).

## 6. Erreurs typées

- `clk`, `sr`, `en` de largeur ≠ 1 → `VECTOR_WIDTH_MISMATCH`.
- `data` de largeur ≠ largeur du LHS → `VECTOR_WIDTH_MISMATCH`.
- LHS déjà affecté → `DUPLICATE_LHS` (mécanisme existant de `ModuleBuilder`).
- CST malformé → `MALFORMED_CST`.

`MEMORY_ASSIGNMENT_NOT_SUPPORTED` n'est plus levé pour un `:=` valide ; la
valeur d'enum est conservée (pas de rupture d'API) mais devient inutilisée.

## 7. Architecture

Nouvelle classe `parser/conversion/MemoryAssignmentBuilder.java`, miroir de
`ModuleCallBuilder` / `ExpressionBuilder` — garde `ModuleBuilder` (déjà
volumineux) focalisé.

- **Entrée** : le nœud `MemoryAssignment`, le `Subset` du LHS, et le contexte
  nécessaire à la génération de noms frais.
- **Sortie** : `List<Erwan>` (les opérations de toutes les bascules du LHS),
  intégrée au plan retourné par `ModuleBuilder` pour l'instance courante.
- **Dépend de** : `ExpressionBuilder` (construction des bus `data`/`clk`/
  `sr`/`en`), `Bus`, `Subset`, `ConversionException`.

`ModuleBuilder` : remplacer le `throw MEMORY_ASSIGNMENT_NOT_SUPPORTED`
(`ModuleBuilder.java:289-293`) par un appel à `MemoryAssignmentBuilder`.

Aucune modification de l'IR `erwan` ni de `FileSimulateur`.

## 8. Tests

JUnit, e2e via `FileSimulateur` (harnais résolvant les E/S par nom —
`nomEntree`/`nomSortie`) :

- capture sur front montant (`D=0` puis `D=1`) ;
- maintien (`D` change pendant `clk` haut → `Q` inchangé) ;
- reset asynchrone (force `Q=0` indépendamment de l'horloge) ;
- set asynchrone (force `Q=1`) ;
- enable : capture quand `en=1`, maintien quand `en=0` ;
- toggle / compteur (`data` référence le LHS — rétroaction `Q→D`) ;
- LHS vectoriel (`q[3..0] :=`, registre multi-bits) ;
- collision de noms (module définissant un signal `__ff0__qm` → préfixe frais) ;
- rejets de largeur (`clk`/`sr`/`en` vectoriels, `data` de mauvaise largeur).

## 9. Documentation

- `README.md` : retirer `:=` du hors-scope, ajouter une section
  « Affectation mémoire (`:=`) » ; mettre à jour l'arborescence
  (`MemoryAssignmentBuilder.java`).
- `parser/ll1/tabledriven/README.md` : inchangé.
