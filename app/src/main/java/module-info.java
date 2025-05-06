module com.MyIAE {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.desktop;

    opens com.MyIAE to javafx.fxml;
    exports com.MyIAE;
}
