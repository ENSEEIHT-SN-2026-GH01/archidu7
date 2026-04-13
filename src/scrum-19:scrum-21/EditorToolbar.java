import javax.swing.*;
import java.awt.*;

/**
 * Barre d'outils de l'éditeur (boutons Sauvegarder et Compiler).
 */
public class EditorToolbar extends JToolBar {

    public EditorToolbar(EditorController controleur) {
        setFloatable(false);
        setBackground(new Color(45, 45, 48));

        add(creerBouton("Sauvegarder (Ctrl+S)", new Color(70, 130, 180), controleur::demanderSauvegarde));
        addSeparator(new Dimension(5, 0));
        add(creerBouton("Compiler (Ctrl+Enter)", new Color(34, 139, 34), controleur::demanderCompilation));
    }

    private JButton creerBouton(String texte, Color couleurFond, Runnable action) {
        JButton bouton = new JButton(texte);
        bouton.setBackground(couleurFond);
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);
        bouton.setBorderPainted(false);
        bouton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        bouton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bouton.addActionListener(e -> action.run());
        return bouton;
    }
}
