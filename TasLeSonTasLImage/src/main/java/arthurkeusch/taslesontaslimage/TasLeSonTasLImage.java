package arthurkeusch.taslesontaslimage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.IOException;

public class TasLeSonTasLImage extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TasLeSonTasLImage.class.getResource("TasLeSonTasLImage-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("T'as le son ! T'as l'image !");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        //launch();

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String cheminImage = "src/main/images/color_in_black_v2.png";

        try {
            TraitementImage traitementImage = new TraitementImage();
            CreationAudio creationAudio = new CreationAudio(64, 64, 200, 3000, 44100);
            creationAudio.generateAndPlaySound(traitementImage.traitement(cheminImage));
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Une erreur inattendue est survenue : " + e.getMessage());
        }
    }
}