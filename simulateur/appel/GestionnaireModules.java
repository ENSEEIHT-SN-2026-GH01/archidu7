package simulateur.appel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import erwan.Module;
import parser.conversion.Conversion;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import sauvegarde.FileStorage;

public class GestionnaireModules {

    private static Map<String, CstNode> arbresCalculees = new HashMap<>();
    public static FileStorage sauveur;
    
    public static Module appelModule(String nom) throws IOException{
        if (arbresCalculees.containsKey(nom)){
            return Conversion.convert(arbresCalculees.get(nom));
        }
        else{
            if (sauveur == null ) throw new IOException("FileStorage non initialisé");
            CstNode nouveau = CstParser.parse(sauveur.load("/modules/" + nom + ".shdl"));
            arbresCalculees.put(nom, nouveau);
            return Conversion.convert(nouveau);
        }
    }
}