package arthurkeusch.taslesontaslimage;

import arthurkeusch.taslesontaslimage.views.ErrorDialogView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

/**
 * Classe principale permettant de lire des images ou des images extraites d'une vidéo.
 */
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
     * Instance de {@link TraitementVideo} pour traiter les vidéos.
     */
    private final TraitementVideo traitementVideo = new TraitementVideo();

    /**
     * Flag pour indiquer si le son est en cours de lecture ou en pause.
     */
    private boolean isPlaying = true;

    /**
     * Objet pour synchroniser les threads et gérer les pauses.
     */
    private final Object pauseLock = new Object();

    /**
     * Thread de lecture en cours.
     */
    private Thread playbackThread;

    /**
     * Point d'entrée principal de l'application JavaFX.
     * Initialise les composants JavaFX et démarre le cycle d'affichage et de traitement des images.
     *
     * @param primaryStage La scène principale de l'application.
     */
    @Override
    public void start(Stage primaryStage) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        VBox root = new VBox();
        root.setSpacing(10);
        root.setStyle("-fx-alignment: center;");

        Button imageButton = new Button("Images");
        imageButton.setFocusTraversable(false);
        Button videoButton = new Button("Vidéo");
        videoButton.setFocusTraversable(false);

        root.getChildren().addAll(imageButton, videoButton);

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("T'as le son ! T'as l'image !");
        primaryStage.show();

        imageButton.setOnAction(event -> startImageMode(primaryStage, scene));
        videoButton.setOnAction(event -> startVideoMode(primaryStage, scene));
    }

    /**
     * Démarre le mode lecture d'images.
     *
     * @param primaryStage La fenêtre principale utilisée pour afficher les dialogues de sélection.
     * @param scene        La scène actuelle à mettre à jour.
     */
    private void startImageMode(Stage primaryStage, Scene scene) {
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

            setupPlaybackScene(scene, primaryStage);
        } else {
            System.out.println("Dossier invalide ou non sélectionné !");
        }
    }

    /**
     * Démarre le mode lecture de vidéo.
     *
     * @param primaryStage La fenêtre principale utilisée pour afficher les dialogues de sélection.
     * @param scene        La scène actuelle à mettre à jour.
     */
    private void startVideoMode(Stage primaryStage, Scene scene) {
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
                    resetToMainMenu(scene);
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

                javafx.application.Platform.runLater(() -> setupPlaybackScene(scene, primaryStage));
            }).start();
        } else {
            System.out.println("Fichier vidéo invalide ou non sélectionné !");
        }
    }

    /**
     * Configure la scène pour la lecture des images.
     *
     * @param scene        La scène actuelle à mettre à jour.
     * @param primaryStage La fenêtre principale pour revenir au menu principal.
     */
    private void setupPlaybackScene(Scene scene, Stage primaryStage) {
        // Arrête toute lecture en cours pour éviter les superpositions
        stopPlayback();

        StackPane root = new StackPane();
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(500);
        imageView.setFitHeight(500);

        Label instructions = new Label("<--: Précédent | ␣: Pause | -->: Suivant");
        instructions.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        Button backButton = new Button("Retour");
        backButton.setFocusTraversable(false);
        backButton.setOnAction(event -> {
            stopPlayback();
            resetToMainMenu(scene);
        });

        VBox layout = new VBox();
        layout.setStyle("-fx-alignment: top-center;");
        layout.getChildren().addAll(backButton, instructions, root);

        scene.setRoot(layout);
        root.getChildren().add(imageView);

        // Réinitialise les paramètres de lecture
        currentIndex = 0;
        isPlaying = true;

        // Met à jour et affiche la première image
        updateImage(imageView);

        // Démarre la lecture
        playAllImages(imageView, scene);

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
     * @param scene     La scène actuelle pour revenir au menu principal en cas d'erreur.
     */
    private void playAllImages(ImageView imageView, Scene scene) {
        playbackThread = new Thread(() -> {
            try {
                while (true) {
                    synchronized (pauseLock) {
                        while (!isPlaying) {
                            pauseLock.wait();
                        }
                    }

                    File imageFile = images.get(currentIndex);

                    // Vérifie que l'image est valide
                    if (!imageFile.exists() || !imageFile.canRead() || containsSpecialCharacters(imageFile.getName())) {
                        String errorMessage = "Erreur : Impossible de lire le fichier. Le nom contient des caractères spéciaux non supportés : " + imageFile.getName();
                        System.out.println(errorMessage);
                        javafx.application.Platform.runLater(() -> {
                            new ErrorDialogView("Erreur", errorMessage).show();
                            resetToMainMenu(scene);
                        });
                        break; // Stop le processus immédiatement
                    }

                    // Affiche l'image actuelle
                    javafx.application.Platform.runLater(() -> imageView.setImage(new Image(imageFile.toURI().toString())));

                    // Lecture synchrone du son uniquement si l'image courante n'a pas changé
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
     * Arrête la lecture en cours et réinitialise les ressources.
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
     * Réinitialise la scène au menu principal.
     *
     * @param scene La scène à réinitialiser.
     */
    private void resetToMainMenu(Scene scene) {
        VBox root = new VBox();
        root.setSpacing(10);
        root.setStyle("-fx-alignment: center;");

        Button imageButton = new Button("Images");
        imageButton.setFocusTraversable(false);
        Button videoButton = new Button("Vidéo");
        videoButton.setFocusTraversable(false);

        root.getChildren().addAll(imageButton, videoButton);

        imageButton.setOnAction(event -> startImageMode((Stage) scene.getWindow(), scene));
        videoButton.setOnAction(event -> startVideoMode((Stage) scene.getWindow(), scene));

        scene.setRoot(root);
    }

    /**
     * Vérifie si un nom de fichier contient des caractères spéciaux non supportés.
     *
     * @param fileName Le nom du fichier à vérifier.
     * @return true si le nom contient des caractères spéciaux, false sinon.
     */
    private boolean containsSpecialCharacters(String fileName) {
        return !Normalizer.normalize(fileName, Normalizer.Form.NFD).replaceAll("\\p{M}", "").matches("[a-zA-Z0-9._-]+");
    }

    /**
     * Affiche un message d'erreur dans la vue.
     *
     * @param errorMessage Le message d'erreur à afficher.
     */
    private void showErrorDialog(String errorMessage) {
        Stage errorStage = new Stage();
        VBox root = new VBox();
        root.setStyle("-fx-padding: 10; -fx-alignment: center;");
        Label errorLabel = new Label(errorMessage);
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
        Button closeButton = new Button("Fermer");
        closeButton.setOnAction(e -> errorStage.close());
        root.getChildren().addAll(errorLabel, closeButton);
        Scene scene = new Scene(root, 400, 200);
        errorStage.setScene(scene);
        errorStage.setTitle("Erreur");
        errorStage.show();
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
                if (file.isFile() && (file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg"))) {
                    imageFiles.add(file);
                }
            }
        }
        imageFiles.sort(Comparator.comparing(file -> extractNumber(file.getName())));
        return imageFiles;
    }

    /**
     * Extrait le premier nombre trouvé dans le nom d'un fichier pour le tri.
     *
     * @param fileName Le nom du fichier.
     * @return Le nombre trouvé ou 0 si aucun nombre n'est présent.
     */
    private int extractNumber(String fileName) {
        String number = fileName.replaceAll("\\D", "");
        try {
            if (number.isEmpty()) {
                return 0;
            }
            long parsedNumber = Long.parseLong(number);
            if (parsedNumber > Integer.MAX_VALUE) {
                System.out.println("Nombre trop grand pour int, utilisation de la valeur maximale.");
                return Integer.MAX_VALUE; // ou une autre gestion selon votre besoin
            }
            return (int) parsedNumber;
        } catch (NumberFormatException e) {
            System.err.println("Erreur lors de l'extraction du nombre : " + e.getMessage());
            return 0;
        }
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
