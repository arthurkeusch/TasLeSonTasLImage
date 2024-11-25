package arthurkeusch.taslesontaslimage;

import java.util.ArrayList;

/**
 * La classe {@code Image} représente une image sous forme de matrice 2D de pixels
 * et ses données sonores associées. Elle fournit des méthodes pour manipuler les pixels
 * et les fréquences sonores correspondantes.
 */
public class Image {

    /**
     * Représente l'image sous forme de matrice 2D où chaque entier correspond à un pixel (niveaux de gris).
     */
    private ArrayList<ArrayList<Integer>> image;

    /**
     * Représente les données sonores associées à l'image sous forme de matrice 2D.
     * Chaque valeur correspond à une fréquence ou amplitude sonore.
     */
    private ArrayList<ArrayList<Double>> sound;

    /**
     * Constructeur de la classe {@code Image}.
     *
     * @param image la matrice de l'image à initialiser.
     */
    public Image(ArrayList<ArrayList<Integer>> image) {
        this.image = image;
        this.sound = null;
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

    /**
     * Retourne les données sonores associées à l'image.
     *
     * @return la matrice des données sonores sous forme de liste 2D.
     */
    public ArrayList<ArrayList<Double>> getSound() {
        return sound;
    }

    /**
     * Met à jour les données sonores associées à l'image.
     *
     * @param sound la nouvelle matrice des données sonores.
     */
    public void setSound(ArrayList<ArrayList<Double>> sound) {
        this.sound = sound;
    }

    /**
     * Retourne la valeur sonore (fréquence ou amplitude) à la position spécifiée.
     *
     * @param row la ligne de la valeur sonore.
     * @param col la colonne de la valeur sonore.
     * @return la valeur sonore (fréquence ou amplitude).
     */
    public Double getSoundPart(int row, int col) {
        return this.sound.get(row).get(col);
    }

    /**
     * Met à jour la valeur sonore (fréquence ou amplitude) à la position spécifiée.
     *
     * @param row       la ligne de la valeur sonore.
     * @param col       la colonne de la valeur sonore.
     * @param frequency la nouvelle valeur sonore (fréquence ou amplitude).
     */
    public void setSoudPart(int row, int col, Double frequency) {
        this.sound.get(row).set(col, frequency);
    }

    /**
     * Affiche la matrice des données sonores dans la console.
     */
    public void printSound() {
        for (ArrayList<Double> row : this.sound) {
            System.out.println(row);
        }
    }
}
