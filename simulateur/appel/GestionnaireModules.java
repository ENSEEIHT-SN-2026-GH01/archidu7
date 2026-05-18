package simulateur.appel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import sauvegarde.FileStorage;

public class GestionnaireModules {

    private static HashMap<String, CstNode> arbresCalculees = new HashMap<>();
    public static FileStorage sauveur; //initialiser un TextFileStorage pour l'utiliser

    public static CstNode calculerUn(String nom, String texteSHDL){
        CstNode res = CstParser.parse(texteSHDL);
        arbresCalculees.put(nom, res);
        return res;
    }

    public static Collection<CstNode> getAllBut(String pasLui){
        //on en enlève un
        CstNode non = arbresCalculees.get(pasLui);
        arbresCalculees.remove(pasLui);

        //creation de la liste
        Collection<CstNode> res = new ArrayList<>();
        for (CstNode cstNode : arbresCalculees.values()) {
            res.add(cstNode);
        }

        //on le remet
        if (non != null){
            arbresCalculees.put(pasLui, non);
        }

        return res;
    }
}