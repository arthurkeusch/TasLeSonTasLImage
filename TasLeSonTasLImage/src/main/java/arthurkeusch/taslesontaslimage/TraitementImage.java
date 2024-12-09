package arthurkeusch.taslesontaslimage;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class TraitementImage {

    public static ImageMatrice chargerImage(String cheminImage) {
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

        return new ImageMatrice(pixels);
    }

    public static ImageMatrice convertirEnNiveauxDeGris(String cheminImage) {
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

        return new ImageMatrice(pixelsGris);
    }

    public static ImageMatrice compresserEn64x64(ImageMatrice imageOriginale) {
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

        return new ImageMatrice(pixelsCompressee);
    }

    public static void generateImageSound(ImageMatrice image) {
        ArrayList<ArrayList<Integer>> pixels = image.getImage();
        ArrayList<ArrayList<Double>> sound = new ArrayList<>();

        int rows = pixels.size();
        int cols = pixels.get(0).size();

        for (int col = 0; col < cols; col++) {
            ArrayList<Double> amplitudes = new ArrayList<>();

            for (int row = 0; row < rows; row++) {
                int pixelValue = pixels.get(row).get(col);

                // Conserver la valeur brute (non normalisée)
                double amplitude = pixelValue / 15.0; // Utilisation directe des niveaux de gris compressés (0 à 15)
                amplitudes.add(amplitude);
            }

            sound.add(amplitudes);
        }

        image.setSound(sound);
    }
}
