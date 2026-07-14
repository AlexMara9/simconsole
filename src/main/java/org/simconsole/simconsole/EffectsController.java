package org.simconsole.simconsole;

import javafx.fxml.FXML;

public class EffectsController {

    @FXML private LedKnob eqLowKnob;
    @FXML private LedKnob eqMidKnob;
    @FXML private LedKnob eqHighKnob;

    private Deck deck;

    @FXML
    public void initialize() {
        if (eqLowKnob != null) {
            eqLowKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (deck != null) {
                    deck.getControls().setEqLow(newVal.doubleValue());
                }
            });
        }
        if (eqMidKnob != null) {
            eqMidKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (deck != null) {
                    deck.getControls().setEqMid(newVal.doubleValue());
                }
            });
        }
        if (eqHighKnob != null) {
            eqHighKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (deck != null) {
                    deck.getControls().setEqHigh(newVal.doubleValue());
                }
            });
        }
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
        
        if (deck != null) {
            if (eqLowKnob != null) eqLowKnob.setValue(deck.getControls().getEqLow());
            if (eqMidKnob != null) eqMidKnob.setValue(deck.getControls().getEqMid());
            if (eqHighKnob != null) eqHighKnob.setValue(deck.getControls().getEqHigh());
        }
    }
}
