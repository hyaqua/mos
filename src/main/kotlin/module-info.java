module org.jandj.mos {
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires atlantafx.base;

    opens org.jandj.mos to javafx.fxml;
    exports org.jandj.mos;
}