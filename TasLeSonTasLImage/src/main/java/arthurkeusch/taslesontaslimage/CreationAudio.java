package arthurkeusch.taslesontaslimage;

import javax.sound.sampled.*;
import java.util.ArrayList;

public class CreationAudio {

    public static void generateAndPlaySound(ImageMatrice image) {
        ArrayList<ArrayList<Double>> soundMatrix = image.getSound();

        if (soundMatrix == null || soundMatrix.isEmpty()) {
            throw new IllegalArgumentException("La matrice sonore est vide ou non définie.");
        }

        // Paramètres du son
        int sampleRate = 44100; // Fréquence d'échantillonnage standard (44,1 kHz)
        int samplesPerFrame = sampleRate / 64; // Nombre d'échantillons pour 1/64e de seconde
        int numFrames = soundMatrix.size();
        byte[] audioBuffer = new byte[numFrames * samplesPerFrame];

        // Fréquences entre 1000 Hz et 4000 Hz
        double minFrequency = 1000;
        double maxFrequency = 4000;

        // Générer le son
        for (int frame = 0; frame < numFrames; frame++) {
            ArrayList<Double> amplitudes = soundMatrix.get(frame);

            for (int sample = 0; sample < samplesPerFrame; sample++) {
                double sampleValue = 0;

                // Combinaison des fréquences et des amplitudes
                for (int i = 0; i < amplitudes.size(); i++) {
                    double frequency = minFrequency + (i * (maxFrequency - minFrequency) / amplitudes.size());
                    double amplitude = amplitudes.get(i); // Utilisation directe des amplitudes brutes
                    double time = (double) (frame * samplesPerFrame + sample) / sampleRate;
                    sampleValue += amplitude * Math.sin(2.0 * Math.PI * frequency * time);
                }

                // Pas de normalisation immédiate
                sampleValue = Math.max(-1.0, Math.min(1.0, sampleValue)); // Limiter à [-1, 1]
                audioBuffer[frame * samplesPerFrame + sample] = (byte) (sampleValue * 127);
            }
        }

        // Configurer le format audio
        AudioFormat audioFormat = new AudioFormat(sampleRate, 8, 1, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        try {
            // Ouvrir et démarrer la ligne audio
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();

            // Écrire les données audio dans la ligne (lecture du son)
            line.write(audioBuffer, 0, audioBuffer.length);

            // Finir et fermer la ligne audio
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
