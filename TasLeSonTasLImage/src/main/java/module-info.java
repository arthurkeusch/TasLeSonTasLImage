module arthurkeusch.taslesontaslimage {
    requires javafx.controls;
    requires javafx.fxml;


    opens arthurkeusch.taslesontaslimage to javafx.fxml;
    exports arthurkeusch.taslesontaslimage;
}