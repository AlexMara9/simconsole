package org.simconsole.simconsole.controllers;
import org.simconsole.simconsole.models.Deck;
import org.simconsole.simconsole.models.DeckControls;
import org.simconsole.simconsole.components.LedKnob;
import org.simconsole.simconsole.components.LedButton;
import org.simconsole.simconsole.components.DynamicSlider;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for managing audio equalizer settings and sound effects controls for a DJ deck.
 */
public class EffectsController {
    private Deck deck;
    private DeckControls deckControls;
    @FXML private LedKnob eqLowKnob;
    @FXML private LedKnob eqMidKnob;
    @FXML private LedKnob eqHighKnob;
    @FXML private LedButton eqLowEnableBtn;
    @FXML private LedButton eqMidEnableBtn;
    @FXML private LedButton eqHighEnableBtn;
    @FXML private LedButton eqLowResetBtn;
    @FXML private LedButton eqMidResetBtn;
    @FXML private LedButton eqHighResetBtn;
    @FXML private LedButton flangerBtn;
    @FXML private DynamicSlider flangerKnob;
    @FXML private Label fx1Label;
    @FXML private Label flangerValLabel;
    @FXML private LedButton reverbBtn;
    @FXML private DynamicSlider reverbKnob;
    @FXML private Label fx2Label;
    @FXML private Label reverbValLabel;
    @FXML private LedButton ecoBtn;
    @FXML private DynamicSlider ecoKnob;
    @FXML private Label fx3Label;
    @FXML private Label ecoValLabel;
    @FXML
    public void initialize() {
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
        if (eqLowEnableBtn != null) {
            eqLowEnableBtn.setSelected(true);
            eqLowEnableBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateKnobIdleState(eqLowKnob, newVal);
                DeckControls dc = getActiveControls();
                if (dc != null && eqLowKnob != null) {
                    dc.setEqLow(newVal ? eqLowKnob.getValue() : -40.0);
                }
            });
        }
        if (eqMidEnableBtn != null) {
            eqMidEnableBtn.setSelected(true);
            eqMidEnableBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateKnobIdleState(eqMidKnob, newVal);
                DeckControls dc = getActiveControls();
                if (dc != null && eqMidKnob != null) {
                    dc.setEqMid(newVal ? eqMidKnob.getValue() : -40.0);
                }
            });
        }
        if (eqHighEnableBtn != null) {
            eqHighEnableBtn.setSelected(true);
            eqHighEnableBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateKnobIdleState(eqHighKnob, newVal);
                DeckControls dc = getActiveControls();
                if (dc != null && eqHighKnob != null) {
                    dc.setEqHigh(newVal ? eqHighKnob.getValue() : -40.0);
                }
            });
        }
        updateLabelColor(fx3Label, false);
        updateLabelColor(fx1Label, false);
        updateLabelColor(fx2Label, false);
        updateSliderIdleState(ecoKnob, false);
        updateSliderIdleState(flangerKnob, false);
        updateSliderIdleState(reverbKnob, false);
        if (ecoValLabel != null && ecoKnob != null) ecoValLabel.setText(String.format("%.0f%%", ecoKnob.getValue() * 100));
        if (flangerValLabel != null && flangerKnob != null) flangerValLabel.setText(String.format("%.0f%%", flangerKnob.getValue() * 100));
        if (reverbValLabel != null && reverbKnob != null) reverbValLabel.setText(String.format("%.0f%%", reverbKnob.getValue() * 100));
        if (ecoValLabel != null) ecoValLabel.setOpacity(0.4);
        if (flangerValLabel != null) flangerValLabel.setOpacity(0.4);
        if (reverbValLabel != null) reverbValLabel.setOpacity(0.4);
        setupListeners();
    }
    private DeckControls getActiveControls() {
        if (deckControls != null) return deckControls;
        if (deck != null) return deck.getControls();
        return null;
    }
    private void updateLabelColor(Label label, boolean isOn) {
        if (label == null) return;
        String color = isOn ? "#90ee90" : "#ffffff";
        label.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }
    private void updateSliderIdleState(DynamicSlider slider, boolean isOn) {
        if (slider == null) return;
        slider.setDisable(!isOn);
        if (isOn) {
            slider.setStyle("");
            slider.setOpacity(1.0);
        } else {
            slider.setStyle("-track-filled: #555555;");
            slider.setOpacity(0.4);
        }
    }
    private void updateKnobIdleState(LedKnob knob, boolean isOn) {
        if (knob == null) return;
        knob.setLedsEnabled(isOn);
    }
    public void setDeckControls(DeckControls controls) {
        this.deckControls = controls;
        if (controls != null) {
            if (eqLowKnob != null) eqLowKnob.setValue(controls.getEqLow());
            if (eqMidKnob != null) eqMidKnob.setValue(controls.getEqMid());
            if (eqHighKnob != null) eqHighKnob.setValue(controls.getEqHigh());
            if (flangerBtn != null) flangerBtn.setSelected(controls.isFlangerEnabled());
            if (flangerKnob != null) flangerKnob.setValue(controls.getFlangerWet());
            if (reverbBtn != null) reverbBtn.setSelected(controls.isReverbEnabled());
            if (reverbKnob != null) reverbKnob.setValue(controls.getReverbWet());
            if (ecoBtn != null) ecoBtn.setSelected(controls.isEcoEnabled());
            if (ecoKnob != null) ecoKnob.setValue(controls.getEcoWet());
        }
    }
    public void setDeck(Deck deck) {
        this.deck = deck;
        if (deck != null) {
            DeckControls controls = deck.getControls();
            if (controls != null) {
                if (eqLowKnob != null) eqLowKnob.setValue(controls.getEqLow());
                if (eqMidKnob != null) eqMidKnob.setValue(controls.getEqMid());
                if (eqHighKnob != null) eqHighKnob.setValue(controls.getEqHigh());
            }
            if (eqLowEnableBtn != null) eqLowEnableBtn.setSelected(true);
            if (eqMidEnableBtn != null) eqMidEnableBtn.setSelected(true);
            if (eqHighEnableBtn != null) eqHighEnableBtn.setSelected(true);
        }
    }
    private void setupListeners() {
        if (eqLowKnob != null) {
            eqLowKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                DeckControls dc = getActiveControls();
                if (dc != null && (eqLowEnableBtn == null || eqLowEnableBtn.isSelected())) {
                    dc.setEqLow(newVal.doubleValue());
                }
            });
        }
        if (eqMidKnob != null) {
            eqMidKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                DeckControls dc = getActiveControls();
                if (dc != null && (eqMidEnableBtn == null || eqMidEnableBtn.isSelected())) {
                    dc.setEqMid(newVal.doubleValue());
                }
            });
        }
        if (eqHighKnob != null) {
            eqHighKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                DeckControls dc = getActiveControls();
                if (dc != null && (eqHighEnableBtn == null || eqHighEnableBtn.isSelected())) {
                    dc.setEqHigh(newVal.doubleValue());
                }
            });
        }
        if (ecoBtn != null) {
            ecoBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateLabelColor(fx3Label, newVal);
                updateSliderIdleState(ecoKnob, newVal);
                if (ecoValLabel != null) ecoValLabel.setOpacity(newVal ? 1.0 : 0.4);
                DeckControls dc = getActiveControls();
                if (dc != null) dc.setEcoEnabled(newVal);
            });
        }
        if (ecoKnob != null) {
            ecoKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (ecoValLabel != null) ecoValLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
                DeckControls dc = getActiveControls();
                if (dc != null) dc.setEcoWet(newVal.doubleValue());
            });
        }
        if (flangerBtn != null) {
            flangerBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateLabelColor(fx1Label, newVal);
                updateSliderIdleState(flangerKnob, newVal);
                if (flangerValLabel != null) flangerValLabel.setOpacity(newVal ? 1.0 : 0.4);
                DeckControls dc = getActiveControls();
                if (dc != null) dc.setFlangerEnabled(newVal);
            });
        }
        if (flangerKnob != null) {
            flangerKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (flangerValLabel != null) flangerValLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
                DeckControls dc = getActiveControls();
                if (dc != null) dc.setFlangerWet(newVal.doubleValue());
            });
        }
        if (reverbBtn != null) {
            reverbBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateLabelColor(fx2Label, newVal);
                updateSliderIdleState(reverbKnob, newVal);
                if (reverbValLabel != null) reverbValLabel.setOpacity(newVal ? 1.0 : 0.4);
                DeckControls dc = getActiveControls();
                if (dc != null) dc.setReverbEnabled(newVal);
            });
        }
        if (reverbKnob != null) {
            reverbKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (reverbValLabel != null) reverbValLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
                DeckControls dc = getActiveControls();
                if (dc != null) dc.setReverbWet(newVal.doubleValue());
            });
        }
    }
}
