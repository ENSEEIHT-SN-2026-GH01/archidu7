# Construction d'une simulation

La construction d'une simulation se fait en deux temps:
- La génération d'une description correcte et interprétable,
- La génération du circuit à partir de cette description.

Pour cela il y a deux package :
- le package erwan
- le package simulateur


## Creation de la description (erwan)

La classe Module décrit l'ensemble d'un circuit, comprenant les différents branchements à effectuer,
ainsi que les signaux parmi ceux généré et lus par le circuit qui sont des sorties et des entrées.


### Entrées et Sorties

Les entrées et sorties sont décrite grâce à des descripteur :
Chaque descripteur ne représente que une entrée ou sortie.
Chaque Descripteur ne contient que un nom mais peut contenir differents numéro dans le cas d'une entrée sortie "vecteur".
Les nom et numéros dans les Descripteur doivent absolument correspondre à des nom de signaux présents dans les branchements du circuit. En effet ces descripteur servent justement à les retrouver.
On peut, bien entendu faire référence à un signal lu ou généré dans un module appelé, mais en aucun cas à un signal interne au module appelé. Il faut que l'on fasse référence à un signal fournit en entrée ou sortie de ce module.

Pour décrire l'ensemble des entrées d'un module on fournira donc une liste de descripteur de chaque entrée. 
Attention même si le module n'a qu'une entrée il faudra quand même fournir une liste. Idem pour les sorties.

Ces listes correspondent aux DE (DescripteursEntrees) et DS (DescripteurSorties) de Module.



### Branchements

Les branchement sont l'ensembles des liaisons que l'on va faire entres des composant pour transformer un signal selon une fonction logique.
Dans un module on distingue deux types de branchement:
- la manipulation directe de signaux interne au module
- l'appel à d'autre module en fournissant des signaux en entrée et en récupérant dans d'autre signaux en sortie.

#### Manipulation de signaux (Erwan)

La manipulation de signaux interne se fait à l'aide de la class Erwan.
L'idée de cette classe est de décrire un circuit de manière arborescente :
Chaque objet Erwan contient le nom du signal qu'il représente, l'operation qu'il fait et les entrées sur lesquelles il applique l'opération.
Une opération du type énuméré Operation peut prendre les valeurs :
- LITTERAL (Lecture logique)
- NOT      (Inversion logique)
- OR       (Ou logique)
- AND      (Et logique)
- AFFECTATION (Une sorte de copie, permettant de nommer un signal)

En pratique, pour générer un Erwan il faut passer par une fabrique static. De plus il n'existe pas de modificateur sur Erwan :
on construit donc un circuit d'un seul coup de manière "recursive".

Chaque 'arbre' de Erwan commence par une affectation (AFFECTATION), et les Erwan 'feuille' sont necessairement des lectures logique (LITTERAL).
Les affectation sont les seuls signaux que l'on peut nommer nous même (façon de parler, il faut tout de même suivre le choix de l'utilisateur qui écrit le code shdl). Les autres signaux on nécessairement le nom de leur fonction logique, par exemple :
- a * /b
- /a * (/b + c + (d * e))
Un Erwan de lecture logique aura le nom du signal qu'il lit, un Erwan de et aura les nom de ses entrés séparés de +, etc...
Ces noms sont générés automatiquement de par la structure de l'arbre.

Chacun de ces arbre génère des signaux 'nommés' pas l'utilisateur. 
