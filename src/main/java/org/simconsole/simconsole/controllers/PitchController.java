package org.simconsole.simconsole.controllers;
import org.simconsole.simconsole.components.PitchSlider;
import org.simconsole.simconsole.components.LedButton;
import org.simconsole.simconsole.models.Deck;
import javafx.fxml.FXML;
public class PitchController {
    @FXML private PitchSlider pitchSlider;
    @FXML private LedButton keyLockButton;
    private Deck deck;
    @FXML
    public void initialize() {
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
            double currentPitch = deck.getControls().getPitch();
            pitchSlider.setValue(currentPitch);
            keyLockButton.setSelected(deck.getControls().isKeyLock());
        }
    }
}
