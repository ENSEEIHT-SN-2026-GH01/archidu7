# État des lieux — 2026-05-18

Synthèse de la situation pipeline **parser → conversion → simulateur** et
répartition des tâches restantes.

## Ce qui marche

| Brique | Branche | État |
|--------|---------|------|
| Parser LL(1) table-driven | `feature/parser-ll1-table-driven` | OK |
| Conversion combinatoire (scalaire + vecteur) | `feature/parser-ll1-table-driven` | OK |
| Conversion appel de sous-modules `$call` | `feature/parser-ll1-table-driven` | OK, testé |
| Conversion affectation mémoire `:=` (bascule D) | `feature/parser-ll1-table-driven` | OK, 269 tests verts |

`:=` est *réécrit* en bascule maître-esclave combinatoire. Il cible
**`new FileSimulateur(module.Plan)`** — pas le constructeur `Module`.

## Problèmes ouverts

### P1 — `FileSimulateur(Module)` ne converge pas sur rétroaction d'état
**Symptôme (vérifié)** : le constructeur `FileSimulateur(erwan.Module)` ne
stabilise pas un circuit qui reboucle (bascule, compteur) : la sortie reste
`ND`. Le constructeur `FileSimulateur(Plan)` converge sur le *même* circuit.

**Cause à localiser** : les deux constructeurs passent par le même
`construction(M.Plan)` (donc même moteur `StructEntree`) — la divergence vient
donc du traitement que `FileSimulateur(Module)` fait *après* `this(M.Plan)` :
le remappage des entrées/sorties (`FileSimulateur.java` ~28-185, reconstruction
de `EntreesG`/`SortiesG`, insertion d'`EntreeModule`/`SortieModule`). À
diagnostiquer par Mati — ce n'est pas le garde « ≤ 3 passes » de `StructEntree`,
qui est partagé par les deux constructeurs.

**Exemple concret.** Le *même* compteur marche seul mais devient `ND` une
fois appelé :

```
module compteur (clk, rst : Q)
  Q := /Q on clk, reset when rst
end module

module principal (h, r : sortie)
  $compteur(h, r : sortie)
end module
```

`compteur` simulé seul via `FileSimulateur(Plan)` → `Q` compte. `principal`
simulé → il simule `compteur` via `FileSimulateur(Module)` → `sortie` reste
`ND`. La conversion est correcte dans les deux cas ; seul le moteur diffère.

**Impact :** séquentiel OK dans le module principal ; bloqué pour du séquentiel
**dans un sous-module appelé**. Non bloquant pour le livré actuel — c'est le
trou restant avant de pouvoir réutiliser une bascule comme sous-module.

### P2 — Câblage de l'appel de sous-modules côté simulateur incomplet
Branche `appel_module` : `GestionnaireModules` indexe les `.shdl` en `CstNode`,
le câblage `FileSimulateur` avance. Test e2e
`subModuleCall_halfAdder_tableauVerite` encore `@Ignore`.

## Qui fait quoi

**Mati — simulateur**
- Diagnostiquer P1 : pourquoi `FileSimulateur(Module)` ne converge pas sur
  rétroaction alors que `FileSimulateur(Plan)` y arrive. Piste : le remappage
  I/O fait après `this(M.Plan)` dans le constructeur `Module`.
- Débloque le séquentiel dans les sous-modules.

**Arthur — appel de sous-modules / GUI**
- Terminer le câblage `appel_module`, brancher `GestionnaireModules` sur
  `Conversion.convert(main, library)` (la conversion ne lit aucun fichier).
- Réactiver `subModuleCall_halfAdder_tableauVerite` une fois le câblage prêt.

**Alexis — parser / conversion**
- Conversion `:=` et sous-modules livrée ; merger `feature/parser-ll1-table-driven`.
- Compléter les tests `:=` manquants (chaînage de bascules, `:=` en sous-module,
  rejets `sr`/`en` vectoriels).

## Dépendance

P1 (Mati) bloque le séquentiel-dans-sous-module. Tant qu'il n'est pas levé,
un `:=` à l'intérieur d'un module appelé restera `ND` même si la conversion
est correcte.
