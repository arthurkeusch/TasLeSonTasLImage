package arthurkeusch.taslesontaslimage.views;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * Classe représentant la vue de sélection des images ou vidéos.
 */
public class SelectionView {

    private final Runnable onImageSelected;
    private final Runnable onVideoSelected;

    /**
     * Constructeur de la vue de sélection.
     *
     * @param onImageSelected Action à exécuter lorsque l'utilisateur clique sur "Images".
     * @param onVideoSelected Action à exécuter lorsque l'utilisateur clique sur "Vidéo".
     */
    public SelectionView(Runnable onImageSelected, Runnable onVideoSelected) {
        this.onImageSelected = onImageSelected;
        this.onVideoSelected = onVideoSelected;
    }

    /**
     * Crée la scène pour la vue de sélection.
     *
     * @return Une instance de {@link Scene}.
     */
    public Scene getScene() {
        VBox root = new VBox();
        root.setSpacing(10);
        root.setStyle("-fx-alignment: center;");

        Button imageButton = new Button("Images");
        imageButton.setFocusTraversable(false);
        imageButton.setOnAction(event -> onImageSelected.run());

        Button videoButton = new Button("Vidéo");
        videoButton.setFocusTraversable(false);
        videoButton.setOnAction(event -> onVideoSelected.run());

        root.getChildren().addAll(imageButton, videoButton);

        return new Scene(root, 600, 600);
    }
}
