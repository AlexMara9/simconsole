package org.simconsole.simconsole;

public class Tracks {
    private String filePath;
    private double[] audioData;
    private long durationMs;
    private int sampleRate;
    private float[] waveformPreview;

    public Tracks(String filePath) {
        this.filePath = filePath;
        extractMetadata();
    }

    public String getFilePath() {
        return filePath;
    }
    
    public double[] getAudioData() {
        if (audioData == null) {
            audioData = AudioDecoder.readWavFileAsDoubles(filePath);
        }
        return audioData;
    }

    public float[] getWaveformPreview() {
        if (waveformPreview == null) {
            generateWaveformPreview();
        }
        return waveformPreview;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    // to show only the name of the track in the list view
    @Override
    public String toString() {
        return new java.io.File(filePath).getName();
    }

    private void generateWaveformPreview() {
        double[] data = getAudioData(); // Assicura che l'audio sia caricato
        if (data == null || data.length == 0) {
            waveformPreview = new float[0];
            return;
        }
        
        // Creiamo un'anteprima fissa di 2000 "barrette" (sufficienti per qualsiasi schermo)
        int numBuckets = 2000;
        waveformPreview = new float[numBuckets];
        
        int samplesPerBucket = data.length / numBuckets;
        if (samplesPerBucket == 0) samplesPerBucket = 1;

        for (int i = 0; i < numBuckets; i++) {
            int start = i * samplesPerBucket;
            int end = Math.min(start + samplesPerBucket, data.length);
            
            // Trova il picco massimo assoluto in questo "secchiello"
            double max = 0;
            for (int j = start; j < end; j++) {
                double abs = Math.abs(data[j]);
                if (abs > max) {
                    max = abs;
                }
            }
            waveformPreview[i] = (float) max;
        }
    }
    private void extractMetadata() {
        // Leggiamo i metadati reali dal file WAV
        try {
            java.io.File file = new java.io.File(filePath);
            javax.sound.sampled.AudioInputStream ais = javax.sound.sampled.AudioSystem.getAudioInputStream(file);
            this.sampleRate = (int) ais.getFormat().getSampleRate();
            long frames = ais.getFrameLength();
            this.durationMs = (long) ((frames * 1000.0) / this.sampleRate);
            ais.close();
        } catch (Exception e) {
            this.sampleRate = 44100; // Valore di default in caso di errore
            this.durationMs = 0;
            System.err.println("Errore nella lettura dei metadati WAV: " + e.getMessage());
        }
    }

}
