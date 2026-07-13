package org.simconsole.simconsole;

import javafx.fxml.FXML;

public class PitchController {

    @FXML private PitchSlider pitchSlider;
    @FXML private LedButton keyLockButton;

    private Deck deck;

    @FXML
    public void initialize() {
        // Configuriamo lo slider per riflettere il vero valore del pitch (0.25 -> 3.0)
        pitchSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (deck != null) {
                deck.getControls().setPitch(newVal.doubleValue());
            }
        });

        keyLockButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (deck != null) {
                deck.getControls().setKeyLock(newVal);
            }
        });
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
        if (deck != null) {
            // Sincronizza UI iniziale con lo stato reale del Deck
            double currentPitch = deck.getControls().getPitch();
            pitchSlider.setValue(currentPitch);
            
            keyLockButton.setSelected(deck.getControls().isKeyLock());
        }
    }
}
