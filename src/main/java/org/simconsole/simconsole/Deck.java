package org.simconsole.simconsole;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class Deck {
    private MediaPlayer player;
    private boolean isPlaying = false;
    public void loadTrack(Tracks tracks) {
        if (player != null) {
            player.dispose(); // Free system resources
        }

        File audioFile = new File(tracks.getFilePath());
        Media media = new Media(audioFile.toURI().toString());
        player = new MediaPlayer(media);
    }

    public void play() {
        if (player != null) {
            isPlaying = true;
            player.play();
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void pause() {
        if (player != null) {
            isPlaying = false;
            player.pause();
        }
    }

    public void setVolume(double volume) {
        if (player != null) {
            player.setVolume(volume);
        }
    }

    // Needed to link the visualizer
    public MediaPlayer getPlayer() {
        return player;
    }
}
