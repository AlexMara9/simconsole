package org.simconsole.simconsole.models;

public class Tracks {
    public enum TrackState {
        LOCAL,             // Local-only files, no download needed
        DOWNLOAD_PENDING,  // Added from search API, waiting to start
        DOWNLOADING,       // Currently downloading the MP3 file
        CONVERTING,        // Decoding MP3 to PCM and generating waveform
        READY,             // Fully downloaded, decoded, and ready to play instantly
        FAILED             // Failed during download or conversion
    }

    private String filePath;
    private double[] audioData;
    private long durationMs;
    private int sampleRate;
    private float[] waveformPreview;

    // API metadata (null for local-only tracks)
    private String artist;
    private String title;
    private String previewUrl;
    private boolean fromApi;
    private TrackState state = TrackState.LOCAL;

    /** Constructor for local files. */
    public Tracks(String filePath) {
        this.filePath = filePath;
        this.fromApi  = false;
        this.state    = TrackState.LOCAL;
        parseArtistAndTitleFromFilename(filePath);
        extractMetadata();
    }

    private void parseArtistAndTitleFromFilename(String filePath) {
        // Try to parse Artist - Title from filename
        String nameWithoutExt = new java.io.File(filePath).getName();
        int dotIdx = nameWithoutExt.lastIndexOf('.');
        if (dotIdx > 0) {
            nameWithoutExt = nameWithoutExt.substring(0, dotIdx);
        }
        int dashIdx = nameWithoutExt.indexOf(" - ");
        if (dashIdx > 0) {
            this.artist = nameWithoutExt.substring(0, dashIdx).trim();
            this.title  = nameWithoutExt.substring(dashIdx + 3).trim();
        } else {
            this.title  = nameWithoutExt;
            this.artist = "Autore locale";
        }
    }

    /** Factory for tracks coming from the iTunes API (no local file). */
    public static Tracks fromApi(String artist, String title, String previewUrl, long durationMs) {
        Tracks t = new Tracks();
        t.artist     = artist;
        t.title      = title;
        t.previewUrl = previewUrl;
        t.durationMs = durationMs;
        t.sampleRate = 44100;
        t.fromApi    = true;
        t.state      = TrackState.DOWNLOAD_PENDING;
        return t;
    }

    private Tracks() {}

    // --- Getters ---

    public TrackState getState() { return state; }
    public void setState(TrackState state) { this.state = state; }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        extractMetadata();
    }

    public String getFilePath()   { return filePath; }
    public String getArtist()     { return artist; }
    public String getTitle()      { return title; }
    public String getPreviewUrl() { return previewUrl; }
    public boolean isFromApi()    { return fromApi; }
    public long getDurationMs()   { return durationMs; }
    public int getSampleRate()    { return sampleRate; }

    public double[] getAudioData() {
        if (audioData == null && filePath != null) {
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

    /** Display name: "Artist – Title" for API tracks, filename for local tracks. */
    @Override
    public String toString() {
        boolean hasArtist = artist != null && !artist.isBlank();
        boolean hasTitle = title != null && !title.isBlank();
        
        if (hasArtist && hasTitle) {
            return artist + " – " + title;
        } else if (hasTitle) {
            return title;
        } else if (filePath != null) {
            return new java.io.File(filePath).getName();
        }
        return "Unknown";
    }

    private void generateWaveformPreview() {
        double[] data = getAudioData();
        if (data == null || data.length == 0) {
            waveformPreview = new float[0];
            return;
        }

        // 1000 buckets — enough detail, no extra smoothing
        int numBuckets = 1000;
        waveformPreview = new float[numBuckets];

        int samplesPerBucket = Math.max(1, data.length / numBuckets);

        for (int i = 0; i < numBuckets; i++) {
            int start = i * samplesPerBucket;
            int end = Math.min(start + samplesPerBucket, data.length);
            // RMS: perceived energy
            double sumSq = 0;
            for (int j = start; j < end; j++) {
                sumSq += data[j] * data[j];
            }
            waveformPreview[i] = (float) Math.sqrt(sumSq / (end - start));
        }
    }

    private void extractMetadata() {
        try {
            java.io.File file = new java.io.File(filePath);
            javax.sound.sampled.AudioInputStream ais = javax.sound.sampled.AudioSystem.getAudioInputStream(file);
            this.sampleRate = (int) ais.getFormat().getSampleRate();
            long frames = ais.getFrameLength();
            if (frames > 0) {
                this.durationMs = (long) ((frames * 1000.0) / this.sampleRate);
            }
            ais.close();
        } catch (Exception e) {
            this.sampleRate = 44100;
            System.err.println("Errore nella lettura dei metadati WAV: " + e.getMessage());
        }
    }
}
