package simulateur.scriptTest;

import simulateur.affichage.FenetreSimulateur;

/**Execute une ligne de commande unique d'un script de test.*/
public class LigneExecution {
    private FenetreSimulateur ES;

    public LigneExecution(FenetreSimulateur fen){
        ES = fen;
    }

    /**Execute une ligne de commande de test.
     * 
     * @param ligne
     * @throws SimulationTestException
     * @throws SimulationTestEchecException
     */
    public void executeLigne(String ligne) throws SimulationTestException,SimulationTestEchecException{
        String[] args = ligne.split(" ");

        //commentaire
        if (args.length == 0 || args[0].substring(0, 2).equals("//"));

        //set, format : set <nom entree> <0|1>*
        else if (args[0].equals("set")){
            if (args.length != 3) throw new SimulationTestException();

            for (int i=0; i<args[2].length(); i++){
                ES.set(args[1], i, charToBool(args[2].charAt(i)));
            }
        }

        //check, format : check <nom sortie> <0|1>*
        else if (args[0].equals("check")){
            if (args.length != 3) throw new SimulationTestException();

            for (int i=0; i<args[2].length(); i++){
                ES.check(args[1], i, charToBool(args[2].charAt(i)));
            }
        }

        //commande inconnue
        else{
            throw new SimulationTestException();
        }
    }

    private boolean charToBool(char c) throws SimulationTestException{
        if(c == '0') return false;
        else if(c == '1') return true;
        else throw new SimulationTestException();
    }
}
