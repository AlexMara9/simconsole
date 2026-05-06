package org.simconsole.simconsole;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class    AudioDecoder {

    // Reads a standard 16-bit WAV file and returns it as an array of doubles (-1.0 to 1.0)
    public static double[] readWavFileAsDoubles(String filePath) {
        try {
            File file = new File(filePath);
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);

            // Read all raw bytes from the file
            byte[] bytes = ais.readAllBytes();
            ais.close();

            // A 16-bit sample takes 2 bytes.
            // So the array of doubles will be half the size of the byte array.
            int numSamples = bytes.length / 2;
            double[] doubles = new double[numSamples];

            // Convert bytes to doubles (Little-Endian format, standard for WAV)
            for (int i = 0, j = 0; i < bytes.length - 1; i += 2, j++) {
                int low = bytes[i] & 0xFF;
                int high = bytes[i + 1];

                // Combine the two bytes into a 16-bit short
                short sample = (short) ((high << 8) | low);

                // Normalize to range -1.0 to 1.0
                doubles[j] = sample / 32768.0;
            }

            return doubles;

        } catch (Exception e) {
            System.err.println("Error reading WAV file: " + e.getMessage());
            return new double[0]; // Return empty array on failure
        }
    }
}