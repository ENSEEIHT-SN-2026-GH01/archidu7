package fr.n7.shdl.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("SHDL Editor - Prêt");
    }
}
