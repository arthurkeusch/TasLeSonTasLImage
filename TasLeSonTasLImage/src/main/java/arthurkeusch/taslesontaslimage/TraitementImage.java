package arthurkeusch.taslesontaslimage;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Cette classe fournit des méthodes pour charger, convertir et traiter des images.
 * Elle inclut des opérations telles que la conversion en niveaux de gris et la compression en 64x64 pixels.
 */
public class TraitementImage {

    /**
     * Charge une image à partir d'un fichier et la convertit en une structure Image (ArrayList<ArrayList<Integer>>).
     *
     * @param cheminImage Le chemin du fichier image à charger.
     * @return L'objet Image avec les pixels de l'image chargée.
     * @throws IllegalArgumentException Si l'image ne peut pas être chargée.
     */
    public static Image chargerImage(String cheminImage) {
        Mat matImage = Imgcodecs.imread(cheminImage);
        if (matImage.empty()) {
            throw new IllegalArgumentException("Impossible de charger l'image : " + cheminImage);
        }

        Mat matRgb = new Mat();
        Imgproc.cvtColor(matImage, matRgb, Imgproc.COLOR_BGR2RGB);

        int hauteur = matRgb.rows();
        int largeur = matRgb.cols();

        ArrayList<ArrayList<Integer>> pixels = new ArrayList<>();
        for (int y = 0; y < hauteur; y++) {
            ArrayList<Integer> ligne = new ArrayList<>();
            for (int x = 0; x < largeur; x++) {
                int gris = (int) matRgb.get(y, x)[0];
                ligne.add(gris);
            }
            pixels.add(ligne);
        }

        return new Image(pixels);
    }


    /**
     * Convertit une image en niveaux de gris à partir d'un fichier.
     *
     * @param cheminImage Le chemin du fichier image à convertir.
     * @return L'objet Image avec les pixels convertis en niveaux de gris.
     * @throws IllegalArgumentException Si l'image ne peut pas être chargée.
     */
    public static Image convertirEnNiveauxDeGris(String cheminImage) {
        Mat matImage = Imgcodecs.imread(cheminImage);
        if (matImage.empty()) {
            throw new IllegalArgumentException("Impossible de charger l'image : " + cheminImage);
        }

        Mat matGris = new Mat();
        Imgproc.cvtColor(matImage, matGris, Imgproc.COLOR_BGR2GRAY);

        int largeur = matGris.cols();
        int hauteur = matGris.rows();

        ArrayList<ArrayList<Integer>> pixelsGris = new ArrayList<>();
        for (int y = 0; y < hauteur; y++) {
            ArrayList<Integer> ligne = new ArrayList<>();
            for (int x = 0; x < largeur; x++) {
                double[] pixel = matGris.get(y, x);
                int gris = (int) pixel[0];
                ligne.add(gris);
            }
            pixelsGris.add(ligne);
        }

        return new Image(pixelsGris);
    }


    /**
     * Compresse une image en la redimensionnant à 64x64 pixels et réduit les niveaux
     * de gris à une échelle de 0 à 15.
     *
     * @param imageOriginale L'image à compresser.
     * @return L'image compressée à une taille de 64x64 pixels avec 16 niveaux de gris.
     */
    public static Image compresserEn64x64(Image imageOriginale) {
        ArrayList<ArrayList<Integer>> pixelsOriginaux = imageOriginale.getImage();
        int hauteurOriginale = pixelsOriginaux.size();
        int largeurOriginale = pixelsOriginaux.get(0).size();

        Mat matOriginale = new Mat(hauteurOriginale, largeurOriginale, org.opencv.core.CvType.CV_8UC1);
        for (int y = 0; y < hauteurOriginale; y++) {
            for (int x = 0; x < largeurOriginale; x++) {
                matOriginale.put(y, x, pixelsOriginaux.get(y).get(x));
            }
        }

        Mat mat64x64 = new Mat();
        org.opencv.core.Size taille64x64 = new org.opencv.core.Size(64, 64);
        Imgproc.resize(matOriginale, mat64x64, taille64x64, 0, 0, Imgproc.INTER_AREA);

        ArrayList<ArrayList<Integer>> pixelsCompressee = new ArrayList<>();
        for (int y = 0; y < 64; y++) {
            ArrayList<Integer> ligne = new ArrayList<>();
            for (int x = 0; x < 64; x++) {
                int gris = (int) mat64x64.get(y, x)[0];
                int niveauGris = (gris * 15) / 255;
                ligne.add(niveauGris);
            }
            pixelsCompressee.add(ligne);
        }

        return new Image(pixelsCompressee);
    }


    /**
     * Génère des listes d'amplitudes correspondant aux fréquences pour chaque colonne de l'image.
     * Chaque colonne sera représentée par une liste contenant les amplitudes calculées pour les fréquences de base.
     *
     * @param image L'image à partir de laquelle générer les données sonores.
     */
    public static void generateImageSound(Image image) {
        ArrayList<ArrayList<Integer>> pixels = image.getImage();
        ArrayList<ArrayList<Double>> sound = new ArrayList<>();

        int rows = pixels.size();
        int cols = pixels.get(0).size();

        double[] baseFrequencies = new double[rows];
        for (int i = 0; i < rows; i++) {
            baseFrequencies[i] = 1000 + (i / (double) (rows - 1)) * (4000 - 1000);
        }

        for (int col = 0; col < cols; col++) {
            ArrayList<Double> amplitudes = new ArrayList<>();

            for (int row = 0; row < rows; row++) {
                int pixelValue = pixels.get(row).get(col);

                double amplitude = pixelValue / 255.0;
                amplitudes.add(amplitude);
            }

            sound.add(amplitudes);
        }

        image.setSound(sound);
    }
}
