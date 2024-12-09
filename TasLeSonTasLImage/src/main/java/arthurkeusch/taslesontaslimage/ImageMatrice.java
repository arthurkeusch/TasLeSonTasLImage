package arthurkeusch.taslesontaslimage;

import java.util.ArrayList;

public class ImageMatrice {

    /**
     * Représente l'image sous forme de matrice 2D où chaque entier correspond à un pixel (niveaux de gris).
     */
    private ArrayList<ArrayList<Integer>> image;

    /**
     * Constructeur de la classe {@code Image}.
     *
     * @param image la matrice de l'image à initialiser.
     */
    public ImageMatrice(ArrayList<ArrayList<Integer>> image) {
        this.image = image;
    }

    /**
     * Retourne la matrice de l'image.
     *
     * @return la matrice de l'image sous forme d'une liste 2D.
     */
    public ArrayList<ArrayList<Integer>> getImage() {
        return image;
    }

    /**
     * Met à jour la matrice de l'image.
     *
     * @param image la nouvelle matrice de l'image.
     */
    public void setImage(ArrayList<ArrayList<Integer>> image) {
        this.image = image;
    }

    /**
     * Retourne la valeur du pixel à la position spécifiée.
     *
     * @param row la ligne du pixel.
     * @param col la colonne du pixel.
     * @return la valeur du pixel.
     */
    public Integer getPixel(int row, int col) {
        return this.image.get(row).get(col);
    }

    /**
     * Met à jour la valeur d'un pixel à la position spécifiée.
     *
     * @param row   la ligne du pixel.
     * @param col   la colonne du pixel.
     * @param pixel la nouvelle valeur du pixel.
     */
    public void setPixel(int row, int col, Integer pixel) {
        this.image.get(row).set(col, pixel);
    }

    /**
     * Affiche la matrice de l'image dans la console.
     */
    public void printImage() {
        for (ArrayList<Integer> row : this.image) {
            System.out.println(row);
        }
    }
}
