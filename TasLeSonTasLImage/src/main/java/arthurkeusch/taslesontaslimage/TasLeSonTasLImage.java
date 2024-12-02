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

        // Chemin vers l'image d'entrée
        String cheminImage = "src/main/images/traits_horizontaux.png";

        try {
            System.out.println("=== Test : Charger une image ===");
            Image imageChargee = TraitementImage.chargerImage(cheminImage);
            System.out.println("Image chargée avec succès !");
            imageChargee.printImage();

            // Tester la conversion en niveaux de gris
            System.out.println("\n=== Test : Conversion en niveaux de gris ===");
            Image imageGris = TraitementImage.convertirEnNiveauxDeGris(cheminImage);
            System.out.println("Image convertie en niveaux de gris avec succès !");
            imageGris.printImage();

            // Tester la compression en 64x64
            System.out.println("\n=== Test : Compression en 64x64 ===");
            Image imageCompressee = TraitementImage.compresserEn64x64(imageGris);
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
    }
}