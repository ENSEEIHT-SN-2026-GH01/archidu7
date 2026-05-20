# Cascade de propagation non idempotente → stack overflow sur cycles

**Date** : 2026-05-20 — **Signalé par** : Erwan — **Sévérité** : haute
**Reproducteur** : `tests/parser/conversion/ErwanCount2BugTest.java`

## Symptôme
Cliquer un bouton sur un circuit combinant un sous-module et `:=` provoque un `StackOverflowError`.

## Cause
Chaîne `BouttonEntree.set → StructEntree.setValeur → calculer → EntreeModule.calculer → BouttonEntree.set` sans test d'égalité. Tout cycle (vrai ou transitoire) de propagation récurse à l'infini.

## Fix proposé (1 ligne, code Mati — demander accord)
`simulateur/StructEntree.java:144` :
```java
public void setValeur(int i, Etat e) {
    if (T.get(i) == e) return;   // idempotence
    T.set(i, e);
    calculer(D.get(i));
}
```

## Cas reproducteur
```shdl
module not (a : b) b = /a end module
module count2 (rst, clk : D[1..0], c[1..0])
   not(c[0] : D[0])
   D[1] = (c[0] * /c[1] + c[1] * /c[0])
   c[1..0] := D[1..0] on clk reset when rst
end module
```
