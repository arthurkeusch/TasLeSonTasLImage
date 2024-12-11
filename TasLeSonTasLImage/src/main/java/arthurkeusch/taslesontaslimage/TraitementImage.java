package arthurkeusch.taslesontaslimage;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class TraitementImage {

    /**
     * Convertit une image donnée en niveaux de gris.
     *
     * @param cheminImage Chemin absolu de l'image à traiter.
     * @return Une instance d'ImageMatrice contenant les valeurs en niveaux de gris.
     * @throws IllegalArgumentException Si l'image ne peut pas être chargée depuis le chemin spécifié.
     */
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

    /**
     * Compresse une matrice d'image en une taille de 64x64 pixels, avec réduction des niveaux de gris
     * à une échelle de 16 niveaux (0 à 15).
     *
     * @param imageOriginale La matrice d'image originale à compresser.
     * @return Une instance d'ImageMatrice contenant les valeurs compressées.
     */
    public static ImageMatrice compresserEn64x64(ImageMatrice imageOriginale) {
        Mat matOriginale = getMatrice(imageOriginale);

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

    /**
     * Convertit une instance d'ImageMatrice en une matrice OpenCV ({@link Mat}).
     *
     * @param imageOriginale L'image source sous forme d'ImageMatrice.
     * @return La matrice correspondante sous forme d'un objet OpenCV {@link Mat}.
     */
    private static Mat getMatrice(ImageMatrice imageOriginale) {
        ArrayList<ArrayList<Integer>> pixelsOriginaux = imageOriginale.getImage();
        int hauteurOriginale = pixelsOriginaux.size();
        int largeurOriginale = pixelsOriginaux.getFirst().size();

        Mat matOriginale = new Mat(hauteurOriginale, largeurOriginale, org.opencv.core.CvType.CV_8UC1);
        for (int y = 0; y < hauteurOriginale; y++) {
            for (int x = 0; x < largeurOriginale; x++) {
                matOriginale.put(y, x, pixelsOriginaux.get(y).get(x));
            }
        }
        return matOriginale;
    }

    /**
     * Méthode principale de traitement d'une image.
     * Convertit une image en niveaux de gris, puis la compresse en une matrice 64x64 pixels.
     *
     * @param cheminImage Chemin absolu de l'image à traiter.
     * @return Une instance d'ImageMatrice représentant l'image traitée.
     */
    public ImageMatrice traitement(String cheminImage) {
        return compresserEn64x64(convertirEnNiveauxDeGris(cheminImage));
    }
}
