import editeur.EditeurTexte;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import sauvegarde.FileStorage;
import sauvegarde.SaveListener;

import java.util.ArrayList;
import java.util.List;

/* Bandeau d'onglets affiché au-dessus de l'éditeur.
 * Permet d'ouvrir plusieurs fichiers .shdl en parallèle et de basculer
 * entre eux. Chaque onglet conserve le buffer du fichier (édits non
 * sauvegardés inclus) pour qu'on retrouve l'état au retour. */
public class BandeauOnglet extends HBox {

    private final FileStorage storage;
    private final EditeurTexte editeur;
    private final List<Onglet> onglets = new ArrayList<>();
    private Onglet actif = null;
    private final Label vide;
    private boolean ignorerEdits = false;

    public BandeauOnglet(FileStorage storage, EditeurTexte editeur) {
        super(4); // permet de mettre un petit espace entre les onglets
        this.storage = storage;
        this.editeur = editeur;

        setPadding(new Insets(4, 8, 0, 8));
        setAlignment(Pos.BOTTOM_LEFT);
        setStyle("-fx-background-color: #e8e8e8; -fx-border-color: #bbb; -fx-border-width: 0 0 1 0;");

        vide = new Label("(aucun module d'ouvert)");
        vide.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
        vide.setPadding(new Insets(6, 0, 6, 0));

        editeur.addListener(new EditeurChangeListener());
        storage.addListener(new StorageChangeListener());
        rebuild();
    }

    /* L'utilisateur tape dans l'éditeur : on garde le buffer de l'onglet
     * actif à jour. Le drapeau évite que setText() programmatique ne
     * vienne réécrire le buffer pendant un switch. */
    private class EditeurChangeListener implements ChangeListener<String> {
        public void changed(ObservableValue<? extends String> obs, String old, String nouv) {
            if (actif == null) return;
            actif.buffer = nouv;          // toujours synchroniser le buffer sur l'éditeur
            if (ignorerEdits) return;     // mais ne pas marquer « modifié » lors d'un load/switch programmatique
            if (!actif.modifie) {
                actif.modifie = true;
                actif.rafraichirStyle(true);
            }
        }
    }

    /* Appelé par FichierModuleBouton après un load réussi.
     * On le fait via un appel direct (pas via un listener du storage) pour
     * éviter que ListeModulePrincipale ne recharge l'environnement à chaque
     * ouverture — ça force un layout pass qui casse le rendu de l'éditeur. */
    public void ouvrir(String chemin) {
        if (actif == null || !chemin.equals(actif.chemin)) {
            Onglet existant = trouver(chemin);
            if (existant != null) {
                actif = existant;
            } else {
                Onglet nouv = new Onglet(chemin);
                onglets.add(nouv);
                actif = nouv;
            }
        }
        // Le buffer correspond au disque → onglet propre. ignorerEdits empêche
        // l'EditeurChangeListener de re-dirty l'onglet quand setText() arrive
        // juste après côté FichierModuleBouton.
        actif.modifie = false;
        ignorerEdits = true;
        Platform.runLater(() -> ignorerEdits = false);
        rebuild();
    }

    /* Listener du storage : utilisé pour les sauvegardes (save() appelle
     * toujours notifier()), pour effacer l'indicateur de modif. */
    private class StorageChangeListener implements SaveListener {
        public void onSave() {
            if (actif == null) return;
            String chemin = storage.getActuel();
            if (chemin == null || !chemin.equals(actif.chemin)) return;
            actif.modifie = false;
            actif.rafraichirStyle(true);
        }
    }

    private Onglet trouver(String chemin) {
        for (Onglet o : onglets) {
            if (o.chemin.equals(chemin)) return o;
        }
        return null;
    }

    private void rebuild() {
        getChildren().clear();
        if (onglets.isEmpty()) {
            getChildren().add(vide);
            return;
        }
        for (Onglet o : onglets) {
            o.rafraichirStyle(o == actif);
            getChildren().add(o);
        }
    }

    /* Bascule vers un onglet déjà ouvert (sans recharger depuis le disque). */
    private void switchVers(Onglet o) {
        if (o == actif) return;
        actif = o;
        storage.setActuel(o.chemin);
        ignorerEdits = true;
        editeur.setText(o.buffer);
        ignorerEdits = false;
        rebuild();
    }

    /* Ferme un onglet. Si c'était l'actif, bascule sur le voisin
     * (gauche en priorité), ou vide tout s'il était le dernier. */
    private void fermer(Onglet o) {
        int idx = onglets.indexOf(o);
        onglets.remove(o);
        if (o != actif) {
            rebuild();
            return;
        }
        if (onglets.isEmpty()) {
            actif = null;
            storage.setActuel(null);
            ignorerEdits = true;
            editeur.setText("");
            ignorerEdits = false;
        } else {
            int voisinIdx = Math.max(0, idx - 1);
            switchVers(onglets.get(voisinIdx));
        }
        rebuild();
    }

    private static String nomLisible(String chemin) {
        int idx = chemin.lastIndexOf('/');
        return idx >= 0 ? chemin.substring(idx + 1) : chemin;
    }

    private class Onglet extends HBox {
        final String chemin;
        String buffer = "";
        boolean modifie = false;
        final Button label;
        final Button fermer;

        Onglet(String chemin) {
            super(2);
            this.chemin = chemin;
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(4, 4, 4, 10));

            label = new Button("📄 " + nomLisible(chemin));
            label.setOnAction(e -> switchVers(this));

            fermer = new Button();
            fermer.setOnAction(e -> fermer(this));

            label.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

            getChildren().addAll(label, fermer);
        }

        void rafraichirStyle(boolean estActif) {
            String fond = estActif ? "#ffffff" : "#d0d0d0";
            String labelStyle = estActif
                ? "-fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold;"
                : "-fx-background-color: transparent; -fx-cursor: hand;";
            setStyle(
                "-fx-background-color: " + fond + ";"
                + "-fx-border-color: #999;"
                + "-fx-border-width: 1 1 0 1;"
                + "-fx-background-radius: 4 4 0 0;"
                + "-fx-border-radius: 4 4 0 0;"
            );
            label.setStyle(labelStyle);
            if (modifie) {
                fermer.setText("●");
                fermer.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14;");
            } else {
                fermer.setText("✕");
                fermer.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #666;");
            }
        }
    }
}
