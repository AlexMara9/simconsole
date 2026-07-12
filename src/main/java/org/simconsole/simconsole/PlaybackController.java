package org.simconsole.simconsole;

import javafx.fxml.FXML;

public class PlaybackController {

    @FXML private PlayButton playButton;
    private Deck deck;

    @FXML
    public void initialize() {
        // Initialization logic if needed
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
        
        // Timer per sincronizzare l'UI se la riproduzione si ferma da sola
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
