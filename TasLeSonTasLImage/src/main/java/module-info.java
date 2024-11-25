module arthurkeusch.taslesontaslimage {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;


    opens arthurkeusch.taslesontaslimage to javafx.fxml;
    exports arthurkeusch.taslesontaslimage;
}