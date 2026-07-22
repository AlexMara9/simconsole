package org.simconsole.simconsole.models;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
/**
 * Utility class for reading and decoding WAV audio files into normalized floating-point audio sample arrays.
 */
public class AudioDecoder {
    public static double[] readWavFileAsDoubles(String filePath) {
        try {
            File file = new File(filePath);
            AudioInputStream in = AudioSystem.getAudioInputStream(file);
            javax.sound.sampled.AudioFormat baseFormat = in.getFormat();
            javax.sound.sampled.AudioFormat decodedFormat = new javax.sound.sampled.AudioFormat(
                    javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );
            AudioInputStream ais = AudioSystem.getAudioInputStream(decodedFormat, in);
            byte[] bytes = ais.readAllBytes();
            ais.close();
            in.close();
            int numSamples = bytes.length / 2;
            double[] doubles = new double[numSamples];
            for (int i = 0, j = 0; i < bytes.length - 1; i += 2, j++) {
                int low = bytes[i] & 0xFF;
                int high = bytes[i + 1];
                short sample = (short) ((high << 8) | low);
                doubles[j] = sample / 32768.0;
            }
            return doubles;
        } catch (Exception e) {
            System.err.println("Error reading WAV file: " + e.getMessage());
            return new double[0];
        }
    }
}