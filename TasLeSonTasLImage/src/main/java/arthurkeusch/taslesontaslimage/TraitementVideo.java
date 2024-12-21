package arthurkeusch.taslesontaslimage;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Classe permettant de traiter une vidéo et d'extraire des images à intervalles réguliers.
 */
public class TraitementVideo {

    /**
     * Traite un segment de la vidéo entre deux secondes spécifiques.
     *
     * @param videoPath   Chemin de la vidéo à traiter.
     * @param startSecond Seconde de début du segment.
     * @param endSecond   Seconde de fin du segment.
     */
    public void traiterSegment(String videoPath, int startSecond, int endSecond) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        VideoCapture videoCapture = new VideoCapture(videoPath);

        if (!videoCapture.isOpened()) {
            System.out.println("Impossible d'ouvrir la vidéo : " + videoPath);
            return;
        }

        Mat frame = new Mat();
        for (int seconde = startSecond; seconde < endSecond; seconde++) {
            videoCapture.set(Videoio.CAP_PROP_POS_MSEC, seconde * 1000);

            if (videoCapture.read(frame) && !frame.empty()) {
                BufferedImage image = matToBufferedImage(frame);
                String outputPath = "src/main/imagesVideo/image_" + seconde + ".jpg";
                saveImageWithCompression(image, outputPath);
            } else {
                System.out.println("Aucune image trouvée à la seconde " + seconde);
            }
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

    /**
     * Enregistre une image en format JPEG avec compression.
     *
     * @param image      L'image à sauvegarder.
     * @param outputPath Le chemin du fichier de sortie.
     */
    private void saveImageWithCompression(BufferedImage image, String outputPath) {
        try {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.8f);

            File outputFile = new File(outputPath);
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
            }
            writer.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtenir la durée de la vidéo en secondes.
     *
     * @param videoPath Chemin de la vidéo.
     * @return Durée de la vidéo en secondes.
     */
    public int obtenirDureeVideo(String videoPath) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        VideoCapture videoCapture = new VideoCapture(videoPath);

        if (!videoCapture.isOpened()) {
            System.out.println("Impossible d'ouvrir la vidéo : " + videoPath);
            return 0;
        }

        double frameCount = videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
        double fps = videoCapture.get(Videoio.CAP_PROP_FPS);

        videoCapture.release();

        return (int) Math.ceil(frameCount / fps);
    }
}