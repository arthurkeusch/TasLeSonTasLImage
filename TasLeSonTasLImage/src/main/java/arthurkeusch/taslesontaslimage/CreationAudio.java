package arthurkeusch.taslesontaslimage;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Classe permettant de générer et de jouer un son à partir d'une matrice d'image.
 */
public class CreationAudio {

    /**
     * Tableau des valeurs sinus pré-calculées, indexées par ligne et échantillon.
     */
    private final double[][] sineTable;

    /**
     * Fréquence d'échantillonnage pour la génération du son (exemple : 44100 Hz).
     */
    private final int sampleRate;

    /**
     * Nombre d'échantillons par frame.
     */
    private final int samplesPerFrame;

    /**
     * Nombre de lignes dans la matrice de l'image.
     */
    private final int numRows;

    /**
     * Nombre de colonnes dans la matrice de l'image.
     */
    private final int numCols;


    /**
     * Constructeur qui initialise les paramètres audio, le tableau des fréquences et les valeurs sinus pré-calculées.
     *
     * @param numRows      Nombre de lignes dans la matrice sonore (image).
     * @param numCols      Nombre de colonnes dans la matrice sonore (image).
     * @param minFrequency Fréquence minimale (en Hz).
     * @param maxFrequency Fréquence maximale (en Hz).
     * @param sampleRate   Fréquence d'échantillonnage (exemple : 44100 Hz).
     */
    public CreationAudio(int numRows, int numCols, double minFrequency, double maxFrequency, int sampleRate) {
        this.sampleRate = sampleRate;
        this.samplesPerFrame = sampleRate / numRows;
        this.numRows = numRows;
        this.numCols = numCols;
        double[] frequencyTable = new double[numRows];
        sineTable = new double[numRows][samplesPerFrame * numCols];

        for (int row = 0; row < numRows; row++) {
            frequencyTable[row] = maxFrequency - (row * (maxFrequency - minFrequency) / (numRows - 1));
        }

        for (int row = 0; row < numRows; row++) {
            double frequency = frequencyTable[row];
            for (int col = 0; col < numCols; col++) {
                for (int sample = 0; sample < samplesPerFrame; sample++) {
                    double time = (double) (col * samplesPerFrame + sample) / sampleRate;
                    sineTable[row][col * samplesPerFrame + sample] = Math.sin(2.0 * Math.PI * frequency * time);
                }
            }
        }

        System.out.println("Tableau des fréquences : " + Arrays.toString(frequencyTable));
    }

    /**
     * Génère et joue un son à partir d'une matrice sonore représentée sous forme d'image.
     *
     * @param image L'image contenant la matrice de l'image sous la forme d'ArrayList<ArrayList<Integer>>.
     */
    public void generateAndPlaySound(ImageMatrice image) {
        ArrayList<ArrayList<Integer>> soundMatrix = image.getImage();

        if (soundMatrix == null || soundMatrix.isEmpty() || soundMatrix.get(0).isEmpty()) {
            throw new IllegalArgumentException("La matrice sonore est vide ou non définie.");
        }

        byte[] audioBuffer = new byte[this.numCols * this.samplesPerFrame];

        for (int col = 0; col < this.numCols; col++) {
            for (int sample = 0; sample < this.samplesPerFrame; sample++) {
                double sampleValue = 0;

                for (int row = 0; row < this.numRows; row++) {
                    sampleValue += soundMatrix.get(row).get(col) * sineTable[row][col * this.samplesPerFrame + sample];
                }

                sampleValue = Math.max(-1.0, Math.min(1.0, sampleValue));
                audioBuffer[col * this.samplesPerFrame + sample] = (byte) (sampleValue * 127);
            }
        }

        AudioFormat audioFormat = new AudioFormat(this.sampleRate, 8, 1, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        try {
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();

            line.write(audioBuffer, 0, audioBuffer.length);

            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
