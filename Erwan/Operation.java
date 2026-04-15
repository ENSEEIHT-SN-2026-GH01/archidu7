public enum Operation {

	AND,
	OR,
	NOT,
	LITTERAL,
	RANGE, //Plusieurs Copies envie de virer ça := Plusieurs LITTERAL/CONSTANTE/... On peut génerer autrement
	CONSTANTE,  //Envie de virer ça := LITTERAL avec nom 0/1
	MODULE;     //Envie de virer ça := Generer les connexions engendrés
}
