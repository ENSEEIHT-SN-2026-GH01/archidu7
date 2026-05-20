# Pas d'isolation maître-esclave entre `$BasculeD` cascadées

**Date** : 2026-05-20 — **Sévérité** : haute (tout design séquentiel multi-bit chaîné)
**Reproducteur** : `tests/parser/conversion/BasculeDIntegrationTest#t9_shiftRegister_2etages`

## Symptôme
Sur un shift register `D1 → D2`, au front montant `q1` prend la nouvelle valeur de `q0` au lieu de l'ancienne. Pas d'un tick de retard.

## Cause
Dans `BasculeDSimulateur.calculer()`, le patch `super.getConnecteurSortie(i).getComposant().calculer()` (commit `f29a0dd`) propage *immédiatement* la nouvelle sortie. Quand la 2e bascule reçoit ensuite `clk=UP` via le BFS partagé, elle voit déjà le nouveau `q0`.

## Fix proposé (architectural, code Mati — discussion nécessaire)
Deux options :
- **Simulation 2-phases** : sample tous les D *avant* de mettre à jour tous les Q (besoin d'un moteur d'événements global).
- **Vraie maître-esclave dans `BasculeDSimulateur`** : latch master sur front descendant, propagation vers slave sur front montant.

## Impact
Tout compteur multi-bit chaîné, pipeline, FIFO, registre à décalage est cassé. Les registres parallèles (T6) marchent car pas de dépendance inter-bascule.
