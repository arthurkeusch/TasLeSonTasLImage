package arthurkeusch.taslesontaslimage;

import arthurkeusch.taslesontaslimage.views.ErrorDialogView;
import arthurkeusch.taslesontaslimage.views.SelectionView;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TasLeSonTasLImage extends Application {

    private int currentIndex = 0;
    private List<File> images;
    private final CreationAudio creationAudio = new CreationAudio(64, 64, 200, 3000, 44100);
    private final TraitementImage traitementImage = new TraitementImage();
    private final TraitementVideo traitementVideo = new TraitementVideo();
    private boolean isPlaying = true;
    private final Object pauseLock = new Object();
    private Thread playbackThread;

    @Override
    public void start(Stage primaryStage) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        SelectionView selectionView = new SelectionView(
                () -> startImageMode(primaryStage),
                () -> startVideoMode(primaryStage)
        );

        primaryStage.setScene(selectionView.getScene());
        primaryStage.setTitle("T'as le son ! T'as l'image !");

        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);

        primaryStage.show();
    }

    private void startImageMode(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionnez un dossier contenant des images");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File folder = directoryChooser.showDialog(primaryStage);

        if (folder != null && folder.isDirectory()) {
            images = getImagesFromFolder(folder);

            if (images.isEmpty()) {
                System.out.println("Aucune image trouvée dans le dossier sélectionné !");
                return;
            }

            setupPlaybackScene(primaryStage);
        } else {
            System.out.println("Dossier invalide ou non sélectionné !");
        }
    }

    private void startVideoMode(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez une vidéo");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers vidéo", "*.mp4", "*.avi", "*.mkv"));
        File videoFile = fileChooser.showOpenDialog(primaryStage);

        if (videoFile != null && videoFile.isFile()) {
            if (containsSpecialCharacters(videoFile.getName())) {
                String errorMessage = "Erreur : Impossible de lire la vidéo. Le nom contient des caractères spéciaux non supportés : " + videoFile.getName();
                System.out.println(errorMessage);
                javafx.application.Platform.runLater(() -> {
                    new ErrorDialogView("Erreur", errorMessage).show();
                    resetToMainMenu(primaryStage);
                });
                return;
            }

            new Thread(() -> {
                traitementVideo.traitementVideo(videoFile.getAbsolutePath());

                File folder = new File("src/main/imagesVideo");
                images = getImagesFromFolder(folder);

                if (images.isEmpty()) {
                    System.out.println("Aucune image extraite de la vidéo sélectionnée !");
                    return;
                }

                javafx.application.Platform.runLater(() -> setupPlaybackScene(primaryStage));
            }).start();
        } else {
            System.out.println("Fichier vidéo invalide ou non sélectionné !");
        }
    }

    private void setupPlaybackScene(Stage primaryStage) {
        stopPlayback();

        BorderPane mainLayout = new BorderPane();

        // Bouton retour en haut
        Button backButton = new Button("Retour");
        backButton.setOnAction(event -> {
            stopPlayback();
            resetToMainMenu(primaryStage);
        });
        HBox topBar = new HBox(backButton);
        topBar.setStyle("-fx-padding: 10px; -fx-alignment: center-left;");
        mainLayout.setTop(topBar);

        // Section centrale : ImageView
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(500);
        imageView.setFitHeight(500);
        StackPane centerPane = new StackPane(imageView);
        centerPane.setStyle("-fx-padding: 20px;");
        mainLayout.setCenter(centerPane);

        // Bouton Précédent
        Button prevButton = new Button();
        ImageView prevIcon = new ImageView(new Image("file:src/main/resources/icons/left-arrow.png"));
        prevIcon.setFitWidth(40); // Largeur de l'icône
        prevIcon.setFitHeight(40); // Hauteur de l'icône
        prevButton.setGraphic(prevIcon);
        prevButton.setOnAction(event -> {
            synchronized (pauseLock) {
                currentIndex = (currentIndex - 1 + images.size()) % images.size();
                updateImage(imageView);
                pauseLock.notifyAll();
            }
        });

        // Bouton Pause/Play
        Button pauseButton = new Button();
        Image pauseImage = new Image("file:src/main/resources/icons/pause.jpg");
        Image playImage = new Image("file:src/main/resources/icons/play.png");
        ImageView pauseIconView = new ImageView(pauseImage);
        pauseIconView.setFitWidth(40);
        pauseIconView.setFitHeight(40);
        pauseButton.setGraphic(pauseIconView);

        pauseButton.setOnAction(event -> {
            synchronized (pauseLock) {
                isPlaying = !isPlaying;
                if (isPlaying) {
                    // Revenir à l'état "Lecture"
                    pauseIconView.setImage(pauseImage);
                    pauseLock.notifyAll();
                } else {
                    // Passer à l'état "Pause"
                    pauseIconView.setImage(playImage);
                }
            }
        });


        // Bouton Suivant
        Button nextButton = new Button();
        ImageView nextIcon = new ImageView(new Image("file:src/main/resources/icons/right-arrow.png"));
        nextIcon.setFitWidth(40);
        nextIcon.setFitHeight(40);
        nextButton.setGraphic(nextIcon);
        nextButton.setOnAction(event -> {
            synchronized (pauseLock) {
                currentIndex = (currentIndex + 1) % images.size();
                updateImage(imageView);
                pauseLock.notifyAll();
            }
        });


        HBox navigationBox = new HBox(20); // 20 px d'espacement entre les boutons
        navigationBox.setAlignment(Pos.CENTER);

        // Ajouter les boutons à la barre de navigation
        navigationBox.getChildren().addAll(prevButton, pauseButton, nextButton);
        mainLayout.setBottom(navigationBox);
        mainLayout.setStyle("-fx-background-image: url('../../main/resources/backgrounds/app-bg.jpeg'); -fx-background-size: cover;");


        mainLayout.setCenter(imageView);   // Image au centre
        mainLayout.setBottom(navigationBox); // Barre de navigation en bas

        mainLayout.getStylesheets().add("file:src/main/resources/styles.css");

        primaryStage.getScene().setRoot(mainLayout);

        currentIndex = 0;
        isPlaying = true;

        updateImage(imageView);
        playAllImages(imageView, primaryStage);
    }

    private void playAllImages(ImageView imageView, Stage primaryStage) {
        playbackThread = new Thread(() -> {
            try {
                while (true) {
                    synchronized (pauseLock) {
                        while (!isPlaying) {
                            pauseLock.wait();
                        }
                    }

                    File imageFile = images.get(currentIndex);
                    javafx.application.Platform.runLater(() -> imageView.setImage(new Image(imageFile.toURI().toString())));

                    synchronized (creationAudio) {
                        if (images.get(currentIndex).equals(imageFile)) {
                            creationAudio.generateAndPlaySound(traitementImage.traitement(imageFile.getAbsolutePath()));
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        playbackThread.start();
    }

    private void stopPlayback() {
        if (playbackThread != null && playbackThread.isAlive()) {
            playbackThread.interrupt();
        }
        synchronized (pauseLock) {
            isPlaying = false;
            pauseLock.notifyAll();
        }
        playbackThread = null;
    }

    private void resetToMainMenu(Stage primaryStage) {
        SelectionView selectionView = new SelectionView(
                () -> startImageMode(primaryStage),
                () -> startVideoMode(primaryStage)
        );

        primaryStage.setScene(selectionView.getScene());
    }

    private boolean containsSpecialCharacters(String fileName) {
        return !Normalizer.normalize(fileName, Normalizer.Form.NFD).replaceAll("\\p{M}", "").matches("[a-zA-Z0-9._-]+");
    }

    private void updateImage(ImageView imageView) {
        File imageFile = images.get(currentIndex);
        Image image = new Image(imageFile.toURI().toString());
        javafx.application.Platform.runLater(() -> imageView.setImage(image));
    }

    private List<File> getImagesFromFolder(File folder) {
        List<File> imageFiles = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (file.isFile() && (file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg"))) {
                    imageFiles.add(file);
                }
            }
        }
        imageFiles.sort(Comparator.comparing(file -> extractNumber(file.getName())));
        return imageFiles;
    }

    private int extractNumber(String fileName) {
        String number = fileName.replaceAll("\\D", "");
        try {
            if (number.isEmpty()) {
                return 0;
            }
            long parsedNumber = Long.parseLong(number);
            if (parsedNumber > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return (int) parsedNumber;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
