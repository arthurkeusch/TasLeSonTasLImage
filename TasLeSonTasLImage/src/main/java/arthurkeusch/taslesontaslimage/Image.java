package arthurkeusch.taslesontaslimage;

import java.util.ArrayList;

public class Image {
    private ArrayList<ArrayList<Integer>> image;

    public Image(ArrayList<ArrayList<Integer>> image) {
        this.image = image;
    }

    public ArrayList<ArrayList<Integer>> getImage() {
        return image;
    }

    public void setImage(ArrayList<ArrayList<Integer>> image) {
        this.image = image;
    }

    public Integer getPixel(int row, int col) {
        return this.image.get(row).get(col);
    }

    public void setPixel(int row, int col, Integer pixel) {
        this.image.get(row).set(col, pixel);
    }

    public void printImage() {
        for (ArrayList<Integer> row : this.image) {
            System.out.println(row);
        }
    }
}
