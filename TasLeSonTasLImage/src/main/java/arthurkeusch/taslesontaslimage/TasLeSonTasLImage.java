package arthurkeusch.taslesontaslimage;

//
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.util.List;
//

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.Core;

import java.io.IOException;
import java.util.Objects;

public class TasLeSonTasLImage extends Application {

    //
    private int currentImageIndex = 0;
    private int repeatCount = 0;
    private List<File> imageFiles;
    //


    //
    private void startImageSoundCycle(TasLeSonTasLImageController controller) {
        // Créer un ImageView pour afficher l'image
        ImageView imageView = controller.getImageView(); // Assurez-vous que le contrôleur a une méthode pour récupérer le ImageView

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
                        System.out.println("=== Test : Charger une image ===");
                        ImageMatrice imageChargee = TraitementImage.chargerImage(currentImageFile.getAbsolutePath());
                        System.out.println("Image chargée avec succès !");
                        imageChargee.printImage();

                        // Tester la conversion en niveaux de gris
                        System.out.println("\n=== Test : Conversion en niveaux de gris ===");
                        ImageMatrice imageGris = TraitementImage.convertirEnNiveauxDeGris(currentImageFile.getAbsolutePath());
                        System.out.println("Image convertie en niveaux de gris avec succès !");
                        imageGris.printImage();

                        // Tester la compression en 64x64
                        System.out.println("\n=== Test : Compression en 64x64 ===");
                        ImageMatrice imageCompressee = TraitementImage.compresserEn64x64(imageGris);
                        System.out.println("Image compressée à 64x64 pixels avec succès !");
                        imageCompressee.printImage();

                        // Tester la génération de la matrice sonore
                        System.out.println("\n=== Test : Génération de matrice sonore ===");
                        TraitementImage.generateImageSound(imageCompressee);
                        System.out.println("Matrice sonore générée avec succès !");
                        imageCompressee.printSound();

                        // Lecture du son généré
                        System.out.println("\n=== Test : Lecture du son ===");
                        CreationAudio.generateAndPlaySound(imageCompressee);
                        System.out.println("Son généré et lu avec succès !");

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

        // Lancer le cycle à une fréquence d'une image toutes les 3 secondes (par exemple)
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    //


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TasLeSonTasLImage.class.getResource("TasLeSonTasLImage-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600); // modificaion dimension fenetre
        stage.setTitle("T'as le son ! T'as l'image !");
        stage.setScene(scene);
        stage.show();

        //
        // Charger la liste des fichiers images depuis le dossier
        File folder = new File("src/main/images");
        imageFiles = List.of(Objects.requireNonNull(folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"))));

        // Charger la bibliothèque OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Déclencher le processus de lecture des images et sons
        startImageSoundCycle(fxmlLoader.getController());
        //


    }

    public static void main(String[] args) {
        launch(args);

        /*
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Chemin vers l'image d'entrée
        String cheminImage = "src/main/images/traits_horizontaux.png";

        try {
            System.out.println("=== Test : Charger une image ===");
            ImageMatrice imageChargee = TraitementImage.chargerImage(cheminImage);
            System.out.println("Image chargée avec succès !");
            imageChargee.printImage();

            // Tester la conversion en niveaux de gris
            System.out.println("\n=== Test : Conversion en niveaux de gris ===");
            ImageMatrice imageGris = TraitementImage.convertirEnNiveauxDeGris(cheminImage);
            System.out.println("Image convertie en niveaux de gris avec succès !");
            imageGris.printImage();

            // Tester la compression en 64x64
            System.out.println("\n=== Test : Compression en 64x64 ===");
            ImageMatrice imageCompressee = TraitementImage.compresserEn64x64(imageGris);
            System.out.println("Image compressée à 64x64 pixels avec succès !");
            imageCompressee.printImage();

            // Tester la génération de son à partir d'une image
            System.out.println("\n=== Test : Génération de matrice sonore ===");
            TraitementImage.generateImageSound(imageCompressee);
            System.out.println("Matrice sonore générée avec succès !");
            imageCompressee.printSound();

            // Lecture du son généré
            System.out.println("\n=== Test : Lecture du son ===");
            CreationAudio.generateAndPlaySound(imageCompressee);
            System.out.println("Son généré et lu avec succès !");

        } catch (IllegalArgumentException e) {
            System.err.println("Erreur : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Une erreur inattendue est survenue : " + e.getMessage());
        }

         */
    }
}