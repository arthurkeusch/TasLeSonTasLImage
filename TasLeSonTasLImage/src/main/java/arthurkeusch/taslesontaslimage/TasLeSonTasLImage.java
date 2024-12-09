package arthurkeusch.taslesontaslimage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.Core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class TasLeSonTasLImage extends Application {

    private int currentImageIndex = 0;
    private int repeatCount = 0;
    private List<File> imageFiles;

    private void startImageSoundCycle(TasLeSonTasLImageController controller) {
        // Créer un ImageView pour afficher l'image
        ImageView imageView = controller.getImageView();

        // Timeline pour gérer le cycle des images
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (currentImageIndex < imageFiles.size()) {
                if (repeatCount < 3) {
                    // Charger l'image
                    File currentImageFile = imageFiles.get(currentImageIndex);
                    Image image = new Image(currentImageFile.toURI().toString());
                    imageView.setImage(image);

                    // Traiter l'image et générer le son
                    try {
                        // Créer un objet TraitementImage et CreationAudio
                        TraitementImage traitementImage = new TraitementImage();
                        CreationAudio creationAudio = new CreationAudio(64, 64, 200, 3000, 44100);

                        // Utiliser le chemin de l'image pour traitement
                        String cheminImage = currentImageFile.getAbsolutePath();

                        // Traitement de l'image
                        ImageMatrice imageMatrice = traitementImage.traitement(cheminImage);
                        // Génération et lecture du son
                        creationAudio.generateAndPlaySound(imageMatrice);

                        // Incrémenter le compteur de répétitions
                        repeatCount++;
                    } catch (IllegalArgumentException ex) {
                        System.err.println("Erreur : " + ex.getMessage());
                    } catch (Exception ex) {
                        System.err.println("Une erreur inattendue est survenue : " + ex.getMessage());
                    }
                } else {
                    // Passer à l'image suivante
                    repeatCount = 0;
                    currentImageIndex++;
                }
            } else {
                // Toutes les images ont été lues
                System.out.println("Toutes les images ont été traitées.");
            }
        }));

        // Lancer le cycle à une fréquence d'une image toutes les 1 seconde
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TasLeSonTasLImage.class.getResource("TasLeSonTasLImage-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600); // Dimension de la fenêtre
        stage.setTitle("T'as le son ! T'as l'image !");
        stage.setScene(scene);
        stage.show();

        // Charger la liste des fichiers images depuis le dossier
        File folder = new File("src/main/images");
        imageFiles = List.of(Objects.requireNonNull(folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"))));

        // Charger la bibliothèque OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Déclencher le processus de lecture des images et sons
        startImageSoundCycle(fxmlLoader.getController());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
