package arthurkeusch.taslesontaslimage;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TasLeSonTasLImage extends Application {

    /**
     * Index de l'image actuellement affichée.
     */
    private int currentIndex = 0;

    /**
     * Liste des fichiers image à traiter.
     */
    private List<File> images;


    /**
     * Point d'entrée principal de l'application JavaFX.
     * Initialise les composants JavaFX et démarre le cycle d'affichage et de traitement des images.
     *
     * @param primaryStage La scène principale de l'application.
     */
    @Override
    public void start(Stage primaryStage) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        File folder = new File("src/main/images");
        images = getImagesFromFolder(folder);

        if (images.isEmpty()) {
            System.out.println("Aucune image trouvée dans le dossier !");
            return;
        }

        StackPane root = new StackPane();
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(500);
        imageView.setFitHeight(500);
        root.getChildren().add(imageView);

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("T'as le son ! T'as l'image !");
        primaryStage.show();

        TraitementImage traitementImage = new TraitementImage();
        CreationAudio creationAudio = new CreationAudio(64, 64, 200, 3000, 44100);

        displayNextImage(imageView, traitementImage, creationAudio);
    }

    /**
     * Récupère tous les fichiers image dans un dossier donné.
     *
     * @param folder Le dossier contenant les fichiers image.
     * @return Une liste des fichiers image trouvés.
     */
    private List<File> getImagesFromFolder(File folder) {
        List<File> imageFiles = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (file.isFile()) {
                    imageFiles.add(file);
                }
            }
        }
        return imageFiles;
    }

    /**
     * Affiche l'image suivante dans la vue et génère un son basé sur le traitement de cette image.
     * Passe automatiquement à l'image suivante une fois le son terminé.
     *
     * @param imageView       Le composant d'affichage de l'image.
     * @param traitementImage L'objet utilisé pour analyser l'image.
     * @param creationAudio   L'objet utilisé pour générer et jouer le son.
     */
    private void displayNextImage(ImageView imageView, TraitementImage traitementImage, CreationAudio creationAudio) {
        if (currentIndex >= images.size()) {
            System.out.println("Toutes les images ont été affichées.");
            return;
        }

        File imageFile = images.get(currentIndex);
        Image image = new Image(imageFile.toURI().toString());

        imageView.setImage(image);

        new Thread(() -> {
            creationAudio.generateAndPlaySound(traitementImage.traitement(imageFile.getAbsolutePath()));

            javafx.application.Platform.runLater(() -> {
                currentIndex++;
                displayNextImage(imageView, traitementImage, creationAudio);
            });
        }).start();
    }

    /**
     * Point d'entrée de l'application.
     *
     * @param args Arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
