package org.simconsole.simconsole;

import javafx.fxml.FXML;

public class MixerController {

    @FXML private DynamicSlider leftVolumeSlider;
    @FXML private DynamicSlider rightVolumeSlider;

    private Deck leftDeck;
    private Deck rightDeck;

    @FXML
    public void initialize() {
        if (leftVolumeSlider != null) {
            leftVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (leftDeck != null) {
                    leftDeck.getControls().setVolume(newVal.doubleValue() / 100.0);
                }
            });
        }
        
        if (rightVolumeSlider != null) {
            rightVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (rightDeck != null) {
                    rightDeck.getControls().setVolume(newVal.doubleValue() / 100.0);
                }
            });
        }
    }
    
    public void setDecks(Deck leftDeck, Deck rightDeck) {
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;
        
        // Inizializza i valori correnti (dal range 0.0-1.0 al range slider 0-100)
        if (this.leftDeck != null && leftVolumeSlider != null) {
            leftVolumeSlider.setValue(this.leftDeck.getControls().getVolume() * 100.0);
        }
        if (this.rightDeck != null && rightVolumeSlider != null) {
            rightVolumeSlider.setValue(this.rightDeck.getControls().getVolume() * 100.0);
        }
    }
}
