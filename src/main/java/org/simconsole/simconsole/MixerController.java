package org.simconsole.simconsole;

import javafx.fxml.FXML;

public class MixerController {

    @FXML private DynamicSlider leftVolumeSlider;
    @FXML private DynamicSlider rightVolumeSlider;
    
    @FXML private LineKnob leftPanKnob;
    @FXML private LineKnob rightPanKnob;

    @FXML private MasterSlider masterVolumeSlider;

    private Deck leftDeck;
    private Deck rightDeck;

    @FXML
    public void initialize() {
        if (masterVolumeSlider != null) {
            masterVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                AudioProcessor.masterVolume = newVal.doubleValue() / 100.0;
            });
        }
        if (leftVolumeSlider != null) {
            leftVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (leftDeck != null) {
                    leftDeck.getControls().setVolume(newVal.doubleValue() / 100.0);
                }
            });
        }
        if (leftPanKnob != null) {
            leftPanKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (leftDeck != null) {
                    leftDeck.getControls().setPan(-newVal.doubleValue());
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
        if (rightPanKnob != null) {
            rightPanKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (rightDeck != null) {
                    rightDeck.getControls().setPan(-newVal.doubleValue());
                }
            });
        }
    }
    
    public void setDecks(Deck leftDeck, Deck rightDeck) {
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;
        
        // Inizializza i valori correnti (dal range 0.0-1.0 al range slider 0-100)
        if (masterVolumeSlider != null) {
            masterVolumeSlider.setValue(AudioProcessor.masterVolume * 100.0);
        }
        
        if (this.leftDeck != null) {
            if (leftVolumeSlider != null) leftVolumeSlider.setValue(this.leftDeck.getControls().getVolume() * 100.0);
            if (leftPanKnob != null) leftPanKnob.setValue(-this.leftDeck.getControls().getPan());
        }
        if (this.rightDeck != null) {
            if (rightVolumeSlider != null) rightVolumeSlider.setValue(this.rightDeck.getControls().getVolume() * 100.0);
            if (rightPanKnob != null) rightPanKnob.setValue(-this.rightDeck.getControls().getPan());
        }
    }
}
