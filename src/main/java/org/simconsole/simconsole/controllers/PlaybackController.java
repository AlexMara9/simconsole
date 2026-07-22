package org.simconsole.simconsole.controllers;
import org.simconsole.simconsole.components.LedButton;
import org.simconsole.simconsole.components.PlayButton;
import org.simconsole.simconsole.models.Deck;
import javafx.fxml.FXML;

/**
 * Controller for controlling audio playback state such as play, pause, and track rewinding.
 */
public class PlaybackController {
    @FXML private PlayButton playButton;
    @FXML private LedButton rewindButton;
    private Deck deck;
    @FXML
    public void initialize() {
        if (rewindButton != null) {
            rewindButton.setOnAction(e -> {
                if (deck != null) {
                    deck.setPlayheadDouble(0.0);
                }
            });
        }
    }
    public void setDeck(Deck deck) {
        this.deck = deck;
        playButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (deck != null) {
                if (newVal) {
                    deck.play();
                } else {
                    deck.pause();
                }
            }
        });
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                if (deck != null && playButton.isSelected() != deck.isPlaying()) {
                    playButton.setSelected(deck.isPlaying());
                }
            }
        };
        timer.start();
    }
}
