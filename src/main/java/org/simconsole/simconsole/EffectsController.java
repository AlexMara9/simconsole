package org.simconsole.simconsole;

import javafx.fxml.FXML;

public class EffectsController {

    @FXML private LedKnob eqLowKnob;
    @FXML private LedKnob eqMidKnob;
    @FXML private LedKnob eqHighKnob;

    @FXML private LedButton eqLowEnableBtn;
    @FXML private LedButton eqMidEnableBtn;
    @FXML private LedButton eqHighEnableBtn;

    @FXML private LedButton eqLowResetBtn;
    @FXML private LedButton eqMidResetBtn;
    @FXML private LedButton eqHighResetBtn;

    private Deck deck;

    @FXML
    public void initialize() {
        // Reset Buttons
        if (eqLowResetBtn != null) {
            eqLowResetBtn.setOnAction(e -> {
                if (eqLowKnob != null) eqLowKnob.setValue(0.0);
            });
        }
        if (eqMidResetBtn != null) {
            eqMidResetBtn.setOnAction(e -> {
                if (eqMidKnob != null) eqMidKnob.setValue(0.0);
            });
        }
        if (eqHighResetBtn != null) {
            eqHighResetBtn.setOnAction(e -> {
                if (eqHighKnob != null) eqHighKnob.setValue(0.0);
            });
        }

        // Enable Buttons (Kill switches)
        if (eqLowEnableBtn != null) {
            eqLowEnableBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (deck != null && eqLowKnob != null) {
                    deck.getControls().setEqLow(!newVal ? eqLowKnob.getValue() : 0.0);
                }
            });
        }
        if (eqMidEnableBtn != null) {
            eqMidEnableBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (deck != null && eqMidKnob != null) {
                    deck.getControls().setEqMid(!newVal ? eqMidKnob.getValue() : 0.0);
                }
            });
        }
        if (eqHighEnableBtn != null) {
            eqHighEnableBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (deck != null && eqHighKnob != null) {
                    deck.getControls().setEqHigh(!newVal ? eqHighKnob.getValue() : 0.0);
                }
            });
        }

        // Knobs
        if (eqLowKnob != null) {
            eqLowKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (deck != null && (eqLowEnableBtn == null || !eqLowEnableBtn.isSelected())) {
                    deck.getControls().setEqLow(newVal.doubleValue());
                }
            });
        }
        if (eqMidKnob != null) {
            eqMidKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (deck != null && (eqMidEnableBtn == null || !eqMidEnableBtn.isSelected())) {
                    deck.getControls().setEqMid(newVal.doubleValue());
                }
            });
        }
        if (eqHighKnob != null) {
            eqHighKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (deck != null && (eqHighEnableBtn == null || !eqHighEnableBtn.isSelected())) {
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

            if (eqLowEnableBtn != null) eqLowEnableBtn.setSelected(false);
            if (eqMidEnableBtn != null) eqMidEnableBtn.setSelected(false);
            if (eqHighEnableBtn != null) eqHighEnableBtn.setSelected(false);
        }
    }
}
