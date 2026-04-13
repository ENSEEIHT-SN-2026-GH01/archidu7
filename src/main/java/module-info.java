module fr.n7.shdl {
    requires javafx.controls;
    requires javafx.fxml;

    opens fr.n7.shdl to javafx.fxml;
    opens fr.n7.shdl.controller to javafx.fxml;

    exports fr.n7.shdl;
}
