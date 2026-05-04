package org.simconsole.simconsole;

public class Tracks {
    private String filePath;
    private double originalBpm = 120.0;

    public Tracks(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public double getOriginalBpm() {
        return originalBpm;
    }

    public void setOriginalBpm(double originalBpm) {
        this.originalBpm = originalBpm;
    }
}
