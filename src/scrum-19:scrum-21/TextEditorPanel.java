import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TextEditorPanel extends JPanel {

    private final RSyntaxTextArea zoneTexte;
    private final JLabel etiquetteStatut;
    private final EditorController controleur;

    public TextEditorPanel(EditorController controleur) {
        this.controleur = controleur;

        setLayout(new BorderLayout());

        zoneTexte = new RSyntaxTextArea(20, 60);
        zoneTexte.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_VERILOG);
        zoneTexte.setCodeFoldingEnabled(true);
        zoneTexte.setAutoIndentEnabled(true);
        zoneTexte.setTabSize(4);
        zoneTexte.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        zoneTexte.setBackground(new Color(30, 30, 30));
        zoneTexte.setForeground(Color.WHITE);
        zoneTexte.setCaretColor(Color.WHITE);
        zoneTexte.setCurrentLineHighlightColor(new Color(50, 50, 60));

        RTextScrollPane defileur = new RTextScrollPane(zoneTexte);
        defileur.setLineNumbersEnabled(true);

        etiquetteStatut = new JLabel("  Pret");
        etiquetteStatut.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        etiquetteStatut.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        add(new EditorToolbar(controleur), BorderLayout.NORTH);
        add(defileur,                      BorderLayout.CENTER);
        add(etiquetteStatut,               BorderLayout.SOUTH);

        zoneTexte.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e)  { surChangementTexte(); }
            @Override
            public void removeUpdate(DocumentEvent e)  { surChangementTexte(); }
            @Override
            public void changedUpdate(DocumentEvent e) { surChangementTexte(); }
        });

        zoneTexte.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
                    controleur.demanderSauvegarde();
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    controleur.demanderCompilation();
                }
            }
        });
    }

    public void setTexte(String texte) {
        zoneTexte.setText(texte);
        zoneTexte.setCaretPosition(0);
    }

    public String getTexte() {
        return zoneTexte.getText();
    }

    public void setStatut(String message) {
        etiquetteStatut.setText("  " + message);
    }

    private void surChangementTexte() {
        controleur.surChangementTexte(zoneTexte.getText());
        setStatut("Modifie");
    }


}
