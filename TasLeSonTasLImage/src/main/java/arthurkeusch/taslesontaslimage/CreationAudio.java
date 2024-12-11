package arthurkeusch.taslesontaslimage;

import javax.sound.sampled.*;
import java.util.ArrayList;

/**
 * Classe permettant de générer et de jouer un son à partir d'une matrice d'image.
 */
public class CreationAudio {

    /**
     * Table des sinusoïdes pré-générées pour chaque fréquence et chaque échantillon.
     */
    private double[][] sineTable;

    /**
     * Taux d'échantillonnage audio (en Hz).
     */
    private final int sampleRate;

    /**
     * Nombre d'échantillons par frame audio.
     */
    private final int samplesPerFrame;

    /**
     * Nombre de lignes dans la matrice sonore.
     */
    private final int numRows;

    /**
     * Nombre de colonnes dans la matrice sonore.
     */
    private final int numCols;

    /**
     * Table des fréquences pour chaque ligne.
     */
    private double[] frequencyTable;


    /**
     * Constructeur pour initialiser les paramètres audio et générer les tables de fréquences et de sinusoïdes.
     *
     * @param numRows      Nombre de lignes dans la matrice sonore.
     * @param numCols      Nombre de colonnes dans la matrice sonore.
     * @param minFrequency Fréquence minimale (en Hz) utilisée pour la première ligne.
     * @param maxFrequency Fréquence maximale (en Hz) utilisée pour la dernière ligne.
     * @param sampleRate   Taux d'échantillonnage audio (en Hz).
     */
    public CreationAudio(int numRows, int numCols, double minFrequency, double maxFrequency, int sampleRate) {
        this.sampleRate = sampleRate;
        this.samplesPerFrame = sampleRate / numRows;
        this.numRows = numRows;
        this.numCols = numCols;
        initFrequencyTable(minFrequency, maxFrequency);
    }


    /**
     * Initialise la table des fréquences pour chaque ligne, en interpolant entre les fréquences minimale et maximale.
     *
     * @param minFrequency Fréquence minimale (en Hz) pour la première ligne.
     * @param maxFrequency Fréquence maximale (en Hz) pour la dernière ligne.
     */
    public void initFrequencyTable(double minFrequency, double maxFrequency) {
        this.frequencyTable = new double[numRows];
        for (int row = 0; row < numRows; row++) {
            this.frequencyTable[row] = maxFrequency - (row * (maxFrequency - minFrequency) / (numRows - 1));
        }
        initSineTable();
    }


    /**
     * Pré-génère une table de sinusoïdes pour chaque fréquence, échantillon et colonne.
     */
    public void initSineTable() {
        this.sineTable = new double[numRows][samplesPerFrame * numCols];
        for (int row = 0; row < numRows; row++) {
            double frequency = this.frequencyTable[row];
            for (int col = 0; col < numCols; col++) {
                for (int sample = 0; sample < samplesPerFrame; sample++) {
                    double time = (double) (col * samplesPerFrame + sample) / sampleRate;
                    this.sineTable[row][col * samplesPerFrame + sample] = Math.sin(2.0 * Math.PI * frequency * time);
                }
            }
        }
    }

    /**
     * Génère et joue un son basé sur une matrice d'images.
     * Chaque pixel de la matrice contrôle l'amplitude de la fréquence correspondante.
     *
     * @param image Matrice d'images contenant des valeurs (amplitudes) pour chaque pixel.
     * @throws IllegalArgumentException Si la matrice est vide ou nulle.
     */
    public void generateAndPlaySound(ImageMatrice image) {
        ArrayList<ArrayList<Integer>> soundMatrix = image.getImage();

        if (soundMatrix == null || soundMatrix.isEmpty() || soundMatrix.getFirst().isEmpty()) {
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
