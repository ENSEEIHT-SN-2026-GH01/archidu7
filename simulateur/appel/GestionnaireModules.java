package simulateur.appel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import parser.lexer.Lexem;
import parser.lexer.Lexer;
import parser.lexer.Token;
import parser.conversion.ConversionException;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import sauvegarde.FileStorage;

public class GestionnaireModules {

    private static HashMap<String, CstNode> arbresCalculees = new HashMap<>();
    public static FileStorage sauveur; //initialiser un TextFileStorage pour l'utiliser
    public static Lexer lexer;

    public static CstNode calculerUn(String texteSHDL){
        CstNode res = CstParser.parse(texteSHDL);
        String nom = getNom(texteSHDL);
        //System.out.println("|"+nom+"|");
        arbresCalculees.put(nom, res);
        return res;
    }

    public static Collection<CstNode> getAllBut(String pasLui){
        //on en enlève un
        System.out.println("pas lui|"+pasLui+"|");
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

    public static void ajoutManquant(ConversionException moduleNotFound){
        String nomModuleManquant = moduleNotFound.getMessage().substring(8, moduleNotFound.getMessage().length() - 13);
        try{
            String texteSHDL = sauveur.load("/modules/" + nomModuleManquant + ".shdl", false);
            calculerUn(texteSHDL);                        
        } catch (ConversionException nouv){
            if (nouv.reason() == ConversionException.Reason.MODULE_NOT_FOUND){
                ajoutManquant(nouv);
            }
            else {
                throw new ConversionException(nouv.offset(), nouv.nodeKind(), nouv.reason(), "Dans le module " + nomModuleManquant + " :" + nouv.getMessage());
            }
        } catch (IOException fnf){
            throw moduleNotFound;
        } 
    }

    private static String getNom(String texteSHDL){
        List<Lexem<Token>> toks = lexer.tokenize(texteSHDL);
        for (Lexem<Token> lexem : toks) {
            if (lexem.getToken() == Token.Identifiant){
                return lexem.getText();
            }
        }
        return "";
    }
}