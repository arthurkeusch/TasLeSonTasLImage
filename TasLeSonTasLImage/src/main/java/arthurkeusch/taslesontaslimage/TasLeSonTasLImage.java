package arthurkeusch.taslesontaslimage;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
     * Instance de {@link CreationAudio} pour gérer la lecture sonore.
     */
    private final CreationAudio creationAudio = new CreationAudio(64, 64, 200, 3000, 44100);

    /**
     * Instance de {@link TraitementImage} pour analyser et traiter les images.
     */
    private final TraitementImage traitementImage = new TraitementImage();

    /**
     * Flag pour indiquer si le son est en cours de lecture ou en pause.
     */
    private boolean isPlaying = true;

    /**
     * Objet pour synchroniser les threads et gérer les pauses.
     */
    private final Object pauseLock = new Object();

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

        Label instructions = new Label("<--: Précédent | ␣: Pause | -->: Suivant");
        instructions.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: top-center;");
        layout.getChildren().addAll(instructions, root);

        Scene scene = new Scene(layout, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("T'as le son ! T'as l'image !");
        primaryStage.show();

        root.getChildren().add(imageView);
        playAllImages(imageView);

        scene.setOnKeyPressed(event -> {
            synchronized (pauseLock) {
                switch (event.getCode()) {
                    case RIGHT -> {
                        currentIndex = (currentIndex + 1) % images.size();
                        updateImage(imageView);
                        pauseLock.notifyAll();
                    }
                    case LEFT -> {
                        currentIndex = (currentIndex - 1 + images.size()) % images.size();
                        updateImage(imageView);
                        pauseLock.notifyAll();
                    }
                    case SPACE -> {
                        isPlaying = !isPlaying;
                        if (isPlaying) {
                            pauseLock.notifyAll();
                        }
                    }
                }
            }
        });
    }

    /**
     * Joue toutes les images en boucle tant que {@code isPlaying} est vrai.
     * Lorsque la lecture est en pause, le thread est mis en attente.
     *
     * @param imageView Le composant d'affichage de l'image.
     */
    private void playAllImages(ImageView imageView) {
        new Thread(() -> {
            while (true) {
                synchronized (pauseLock) {
                    while (!isPlaying) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }

                File imageFile = images.get(currentIndex);
                Image image = new Image(imageFile.toURI().toString());

                javafx.application.Platform.runLater(() -> imageView.setImage(image));

                creationAudio.generateAndPlaySound(traitementImage.traitement(imageFile.getAbsolutePath()));
            }
        }).start();
    }

    /**
     * Met à jour l'image affichée sans jouer le son.
     *
     * @param imageView Le composant d'affichage de l'image.
     */
    private void updateImage(ImageView imageView) {
        File imageFile = images.get(currentIndex);
        Image image = new Image(imageFile.toURI().toString());
        javafx.application.Platform.runLater(() -> imageView.setImage(image));
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
     * Méthode principale permettant de lancer l'application.
     *
     * @param args Arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
