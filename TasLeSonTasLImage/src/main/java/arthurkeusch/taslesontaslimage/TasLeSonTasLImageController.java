package arthurkeusch.taslesontaslimage;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class TasLeSonTasLImageController {
    @FXML
    private Label welcomeText;
    @FXML
    private ImageView imageView;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Lecture de l'image en format audio");
    }
    @FXML
    private void initialize() {
        // Initialisation si nécessaire
    }

    public ImageView getImageView() {
        return imageView;
    }
}