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

Pour générer un Module il faut fournir son nom, ses entrées et sorties ainsi que des branchements.
Regardons cela plus en détail...


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

Dans les deux cas ils implémentent l'interface Branchement, ce qui permet de faire des liste comprenant ces deux types.

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

Un Erwan de lecture logique aura le nom du signal qu'il lit, un Erwan de OU aura les nom de ses entrés séparés de +, etc...
Ces noms sont générés automatiquement de par la structure de l'arbre.

Chacun de ces arbre génère des signaux 'nommés' pas l'utilisateur. 
Un exemple pour mieux comprendre : A = B + C * D --> Affectation("A", Ou (Litteral("B"), Et (Litteral("C"), Litteral("D"))));
Attention ce ne sont pas les vrai méthodes et les signatures ne sont pas non plus respectées.


Cas des vecteurs de signaux:

Le langage shdl permet de former des 'vecteurs' de signaux : un nom commun et chaque signal a un indice propre.
Pour prendre cela en compte, des méthodes similaire, avec un paramètre numéro en plus on été ajouter pour permettre de manipuler ces signaux individuellements, comme tout autre signal.

De plus, pour permettre l'affectation multiple, d'autres méthodes permettant de générer directement des listes de Erwan correspodant aux signaux des vecteur ont été ajoutés.
Tout cela est à regarder plus en détaille dans la doc Erwan.


L'objet Module contiendra un plan qui est en réalité une liste de ces Erwan.


#### Appels Modules (AppelModule) 

Les appels module permettent de déleguer le calcul de certain signaux à un module déjà écrit. 
Cela est utile pour aléger la complexité des modules et aussi de tester au fur et à mesure les résultats des modélisation.
Pour pouvoir faire appel à un module, il faut déjà savoir quel module utiliser, puis quels signaux de mon circuit je lui fourni en entrée, et quels signaux de mon circuit je génère grace à lui.
C'est exactement ce que contient un AppelModule : un Module, une liste de Descripteur pour les signaux que je lui fourni depuis mon circuit (ses entrées) et une autre liste de Descripteur des signaux de mon circuit qu'il me génère (ses sorties). 
A noter que le domaine des noms du module appelé est indépendant du module appelant.
Les Descripteur décrivent des signaux du module appelant dans un AppelModule, pas de celui appelé.
Plus de détails dans la doc AppelModule.



## Création d'un circuit dans un simulateur (simulateur)

Pour l'instant ce qu'il faut savoir, c'est que un simulateur implémente l'interface Simulateur et que l'implémentation fonctionelle à ce jour est FileSimulateur.
Il possède deux constructeurs, un qui prend un plan (List<Erwan>), qui detecte seul les entrées sorties, pas d'appel module, et l'autre qui prend un module en entrée (Module) et qui exploite l'ensemble des infos à sa disposition.
On peut, par la suite en extraire les entrées et les sorties pour pouvoir activer la simulation et voir les résultat.
Toutes ces choses sont à voir dans la doc. 
