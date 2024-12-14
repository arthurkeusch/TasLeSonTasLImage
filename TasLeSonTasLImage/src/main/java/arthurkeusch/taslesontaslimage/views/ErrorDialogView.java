package arthurkeusch.taslesontaslimage.views;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ErrorDialogView {

    private final String title;
    private final String message;

    public ErrorDialogView(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public void show() {
        Stage errorStage = new Stage();

        VBox root = new VBox();
        root.setStyle("-fx-padding: 10; -fx-alignment: center;");
        Label errorLabel = new Label(message);
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
        Button closeButton = new Button("Fermer");
        closeButton.setOnAction(e -> errorStage.close());

        root.getChildren().addAll(errorLabel, closeButton);

        Scene scene = new Scene(root, 400, 200);
        errorStage.setScene(scene);
        errorStage.setTitle(title);
        errorStage.show();
    }
}
