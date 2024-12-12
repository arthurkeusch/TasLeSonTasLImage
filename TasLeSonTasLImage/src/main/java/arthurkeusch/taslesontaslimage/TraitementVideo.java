package arthurkeusch.taslesontaslimage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 * Classe permettant de traiter une vidéo et d'extraire des images à intervalles réguliers.
 */
public class TraitementVideo {

    /**
     * Extrait une image toutes les secondes d'une vidéo et les enregistre dans un dossier spécifique.
     * Le dossier est vidé avant tout traitement.
     *
     * @param videoPath Le chemin de la vidéo à traiter.
     */
    public void traitementVideo(String videoPath) {
        // Charger la bibliothèque native OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Chemin du dossier de sortie
        String outputFolder = "src/main/imagesVideo";

        // Vider le dossier de sortie
        File directory = new File(outputFolder);
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                file.delete();
            }
        } else {
            directory.mkdirs();
        }

        // Ouvrir la vidéo
        VideoCapture videoCapture = new VideoCapture(videoPath);
        if (!videoCapture.isOpened()) {
            System.out.println("Impossible d'ouvrir la vidéo : " + videoPath);
            return;
        }

        // Récupérer les informations de la vidéo
        double fps = videoCapture.get(Videoio.CAP_PROP_FPS);
        double frameIntervalMs = 1000; // Intervalle en millisecondes pour une seconde
        double currentTimeMs = 0; // Timestamp en millisecondes

        Mat frame = new Mat();
        int imageIndex = 0;

        while (true) {
            videoCapture.set(Videoio.CAP_PROP_POS_MSEC, currentTimeMs);
            if (!videoCapture.read(frame) || frame.empty()) {
                break; // Fin de la vidéo
            }

            // Convertir la frame en BufferedImage
            BufferedImage image = matToBufferedImage(frame);
            if (image != null) {
                // Enregistrer l'image
                File outputFile = new File(outputFolder, "image_" + imageIndex + ".png");
                try {
                    ImageIO.write(image, "png", outputFile);
                    System.out.println("Image enregistrée : " + outputFile.getAbsolutePath());
                    imageIndex++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            currentTimeMs += frameIntervalMs; // Passer au prochain intervalle d'une seconde
        }

        videoCapture.release();
    }

    /**
     * Convertit une frame OpenCV (Mat) en BufferedImage.
     *
     * @param mat La frame OpenCV à convertir.
     * @return L'image convertie en BufferedImage, ou null si la conversion échoue.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();

        byte[] sourcePixels = new byte[width * height * channels];
        mat.get(0, 0, sourcePixels);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        final byte[] targetPixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }
}
