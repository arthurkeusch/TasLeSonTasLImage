package arthurkeusch.taslesontaslimage;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TasLeSonTasLImageController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Lecture de l'image en format audio");
    }
}