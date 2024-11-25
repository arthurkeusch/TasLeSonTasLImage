package arthurkeusch.taslesontaslimage;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;


public class TraitementImage {

    // recuperer image et convertir en une arraylist et l'enregistrer en tant qu'image

    public static Image chargerImage(String cheminImage) {
        // Charger l'image avec OpenCV
        Mat matImage = Imgcodecs.imread(cheminImage);
        if (matImage.empty()) {
            throw new IllegalArgumentException("Impossible de charger l'image : " + cheminImage);
        }

        // Conversion en RGB si nécessaire (OpenCV lit les images en BGR par défaut)
        Mat matRgb = new Mat();
        Imgproc.cvtColor(matImage, matRgb, Imgproc.COLOR_BGR2RGB);

        // Récupérer les dimensions
        int hauteur = matGris.rows();
        int largeur = matGris.cols();

        // Créer une ArrayList<ArrayList<Integer>> pour stocker les pixels
        ArrayList<ArrayList<Integer>> pixels = new ArrayList<>();
        for (int y = 0; y < hauteur; y++) {
            ArrayList<Integer> ligne = new ArrayList<>();
            for (int x = 0; x < largeur; x++) {
                int gris = (int) matGris.get(y, x)[0]; // Valeur du pixel
                ligne.add(gris);
            }
            pixels.add(ligne);
        }

        // Retourner une instance de votre classe Image
        return new Image(pixels);
    }

    // niveau de gris
    public static Image convertirEnNiveauxDeGris(String cheminImage) {
        // Charger l'image avec OpenCV
        Mat matImage = Imgcodecs.imread(cheminImage);
        if (matImage.empty()) {
            throw new IllegalArgumentException("Impossible de charger l'image : " + cheminImage);
        }

        // Conversion en niveaux de gris
        Mat matGris = new Mat();
        Imgproc.cvtColor(matImage, matGris, Imgproc.COLOR_BGR2GRAY);

        // Dimensions
        int largeur = matGris.cols();
        int hauteur = matGris.rows();

        // Extraction des pixels en niveaux de gris
        ArrayList<ArrayList<Integer>> pixelsGris = new ArrayList<>();
        for (int y = 0; y < hauteur; y++) {
            ArrayList<Integer> ligne = new ArrayList<>();
            for (int x = 0; x < largeur; x++) {
                double[] pixel = matGris.get(y, x); // Le pixel est un tableau d'une seule valeur en niveaux de gris
                int gris = (int) pixel[0];
                ligne.add(gris);
            }
            pixelsGris.add(ligne);
        }

        // Retourner une nouvelle instance de la classe Image
        return new Image(pixelsGris);
    }

    // format image 64x64

    public static Image compresserEn64x64(Image imageOriginale) {
        // Récupérer la structure de l'image originale
        ArrayList<ArrayList<Integer>> pixelsOriginaux = imageOriginale.getImage();
        int hauteurOriginale = pixelsOriginaux.size();
        int largeurOriginale = pixelsOriginaux.get(0).size();

        // Créer une Mat à partir de l'image originale
        Mat matOriginale = new Mat(hauteurOriginale, largeurOriginale, org.opencv.core.CvType.CV_8UC1);
        for (int y = 0; y < hauteurOriginale; y++) {
            for (int x = 0; x < largeurOriginale; x++) {
                matOriginale.put(y, x, pixelsOriginaux.get(y).get(x));
            }
        }

        // Redimensionner l'image à 64x64 pixels
        Mat mat64x64 = new Mat();
        org.opencv.core.Size taille64x64 = new org.opencv.core.Size(64, 64);
        Imgproc.resize(matOriginale, mat64x64, taille64x64, 0, 0, Imgproc.INTER_AREA);

        // Créer une nouvelle structure pour les pixels compressés
        ArrayList<ArrayList<Integer>> pixelsCompressee = new ArrayList<>();
        for (int y = 0; y < 64; y++) {
            ArrayList<Integer> ligne = new ArrayList<>();
            for (int x = 0; x < 64; x++) {
                int gris = (int) mat64x64.get(y, x)[0];
                ligne.add(gris);
            }
            pixelsCompressee.add(ligne);
        }

        // Retourner une nouvelle instance d'Image avec les pixels compressés
        return new Image(pixelsCompressee);
    }


    // fonction qui prend une image et renvoie une matrice de frequence


}

