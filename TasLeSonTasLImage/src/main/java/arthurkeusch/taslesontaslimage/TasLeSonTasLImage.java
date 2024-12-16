package arthurkeusch.taslesontaslimage;

import arthurkeusch.taslesontaslimage.views.ErrorDialogView;
import arthurkeusch.taslesontaslimage.views.SelectionView;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * La classe principale de l'application qui gère le mode image et vidéo,
 * la lecture des images et des sons associés, ainsi que la gestion des interactions de l'utilisateur.
 */
public class TasLeSonTasLImage extends Application {

    /**
     * L'index actuel de l'image affichée.
     */
    private int currentIndex = 0;

    /**
     * La liste des fichiers d'images à afficher.
     */
    private List<File> images;

    /**
     * L'objet responsable de la création du son pour chaque image.
     */
    private final CreationAudio creationAudio = new CreationAudio(64, 64, 200, 3000, 44100);

    /**
     * L'objet responsable du traitement des images.
     */
    private final TraitementImage traitementImage = new TraitementImage();

    /**
     * L'objet responsable du traitement vidéo.
     */
    private final TraitementVideo traitementVideo = new TraitementVideo();

    /**
     * Indicateur de l'état de lecture (lecture ou pause).
     */
    private boolean isPlaying = true;

    /**
     * Objet utilisé pour la synchronisation de la lecture.
     */
    private final Object pauseLock = new Object();

    /**
     * Le thread qui gère la lecture des images et des sons.
     */
    private Thread playbackThread;

    /**
     * Méthode principale pour démarrer l'application.
     *
     * @param primaryStage Le stage principal de l'application.
     */
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

    /**
     * Démarre le mode image où l'utilisateur peut sélectionner un dossier contenant des images.
     *
     * @param primaryStage Le stage principal de l'application.
     */
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

    /**
     * Démarre le mode vidéo où l'utilisateur peut sélectionner un fichier vidéo.
     *
     * @param primaryStage Le stage principal de l'application.
     */
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

            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setMinWidth(300);
            javafx.application.Platform.runLater(() -> {
                VBox progressLayout = new VBox(10, new Label("Traitement en cours..."), progressBar);
                progressLayout.setAlignment(Pos.CENTER);
                Scene progressScene = new Scene(progressLayout, 400, 200);
                primaryStage.setScene(progressScene);
            });

            new Thread(() -> {
                int duree = traitementVideo.obtenirDureeVideo(videoFile.getAbsolutePath());
                int nbThreads = Runtime.getRuntime().availableProcessors();
                int framesPerThread = (int) Math.ceil((double) duree / nbThreads);

                ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
                int totalFrames = duree;
                int[] completedFrames = {0};

                for (int i = 0; i < nbThreads; i++) {
                    final int start = i * framesPerThread;
                    final int end = Math.min((i + 1) * framesPerThread, duree);

                    executor.submit(() -> {
                        for (int second = start; second < end; second++) {
                            traitementVideo.traiterSegment(videoFile.getAbsolutePath(), second, second + 1);
                            synchronized (completedFrames) {
                                completedFrames[0]++;
                                double progress = (double) completedFrames[0] / totalFrames;
                                javafx.application.Platform.runLater(() -> progressBar.setProgress(progress));
                            }
                        }
                    });
                }

                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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

    /**
     * Configure la scène de lecture des images, y compris la gestion des boutons et de la navigation.
     *
     * @param primaryStage Le stage principal de l'application.
     */
    private void setupPlaybackScene(Stage primaryStage) {
        stopPlayback();

        BorderPane mainLayout = new BorderPane();

        Button backButton = new Button("Retour");
        backButton.setOnAction(event -> {
            stopPlayback();
            resetToMainMenu(primaryStage);
        });
        HBox topBar = new HBox(backButton);
        topBar.setStyle("-fx-padding: 10px; -fx-alignment: center-left;");
        mainLayout.setTop(topBar);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(500);
        imageView.setFitHeight(500);
        StackPane centerPane = new StackPane(imageView);
        centerPane.setStyle("-fx-padding: 20px;");
        mainLayout.setCenter(centerPane);

        Button prevButton = new Button();
        ImageView prevIcon = new ImageView(new Image("file:src/main/resources/icons/left-arrow.png"));
        prevIcon.setFitWidth(40);
        prevIcon.setFitHeight(40);
        prevButton.setGraphic(prevIcon);
        prevButton.setOnAction(event -> {
            synchronized (pauseLock) {
                currentIndex = (currentIndex - 1 + images.size()) % images.size();
                updateImage(imageView);
                pauseLock.notifyAll();
            }
        });

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
                    pauseIconView.setImage(pauseImage);
                    pauseLock.notifyAll();
                } else {
                    pauseIconView.setImage(playImage);
                }
            }
        });

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

        HBox navigationBox = new HBox(20);
        navigationBox.setAlignment(Pos.CENTER);
        navigationBox.getChildren().addAll(prevButton, pauseButton, nextButton);
        mainLayout.setBottom(navigationBox);

        mainLayout.setCenter(imageView);
        mainLayout.setBottom(navigationBox);
        mainLayout.getStylesheets().add("file:src/main/resources/styles.css");

        primaryStage.getScene().setRoot(mainLayout);

        currentIndex = 0;
        isPlaying = true;

        updateImage(imageView);
        playAllImages(imageView, primaryStage);
    }

    /**
     * Démarre la lecture de toutes les images et la génération des sons associés.
     *
     * @param imageView    L'objet ImageView pour afficher les images.
     * @param primaryStage Le stage principal de l'application.
     */
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

    /**
     * Arrête la lecture et la génération des sons.
     */
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

    /**
     * Réinitialise le menu principal.
     *
     * @param primaryStage Le stage principal de l'application.
     */
    private void resetToMainMenu(Stage primaryStage) {
        SelectionView selectionView = new SelectionView(
                () -> startImageMode(primaryStage),
                () -> startVideoMode(primaryStage)
        );

        primaryStage.setScene(selectionView.getScene());
    }

    /**
     * Vérifie si le nom de fichier contient des caractères spéciaux.
     *
     * @param fileName Le nom du fichier.
     * @return True si le nom contient des caractères spéciaux, sinon False.
     */
    private boolean containsSpecialCharacters(String fileName) {
        return !Normalizer.normalize(fileName, Normalizer.Form.NFD).replaceAll("\\p{M}", "").matches("[a-zA-Z0-9._-]+");
    }

    /**
     * Met à jour l'image affichée dans le ImageView.
     *
     * @param imageView L'objet ImageView pour afficher l'image.
     */
    private void updateImage(ImageView imageView) {
        File imageFile = images.get(currentIndex);
        Image image = new Image(imageFile.toURI().toString());
        javafx.application.Platform.runLater(() -> imageView.setImage(image));
    }

    /**
     * Récupère la liste des fichiers image dans un dossier donné.
     *
     * @param folder Le dossier contenant les images.
     * @return La liste des fichiers image trouvés.
     */
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

    /**
     * Extrait un nombre d'un nom de fichier.
     *
     * @param fileName Le nom du fichier.
     * @return Le nombre extrait du nom du fichier.
     */
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
