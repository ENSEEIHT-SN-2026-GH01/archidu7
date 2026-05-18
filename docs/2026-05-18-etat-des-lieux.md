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
Le constructeur `FileSimulateur(erwan.Module)` ne stabilise pas un circuit
qui reboucle (bascule, compteur) : la sortie reste `ND`. Cause : `StructEntree`
re-parcourt chaque composant **≤ 3 fois** au lieu d'itérer jusqu'au point fixe
(`//TODO ne converge pas en cas de rebouclage`). Le constructeur
`FileSimulateur(Plan)` converge, lui.
**Impact :** séquentiel OK dans le module principal ; bloqué pour du séquentiel
**dans un sous-module appelé**.

### P2 — Câblage de l'appel de sous-modules côté simulateur incomplet
Branche `appel_module` : `GestionnaireModules` indexe les `.shdl` en `CstNode`,
le câblage `FileSimulateur` avance. Test e2e
`subModuleCall_halfAdder_tableauVerite` encore `@Ignore`.

## Qui fait quoi

**Mati — simulateur**
- Faire converger `FileSimulateur` sur rétroaction d'état : remplacer le
  garde « ≤ 3 passes » de `StructEntree` par une itération jusqu'au point fixe.
- Débloque P1 → autorise le séquentiel dans les sous-modules.

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
