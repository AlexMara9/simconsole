package org.simconsole.simconsole;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class EffectsController {

    private DeckControls deckControls;

    @FXML private LedKnob eqLowKnob;
    @FXML private LedKnob eqMidKnob;
    @FXML private LedKnob eqHighKnob;

    @FXML private LedButton ecoBtn;
    @FXML private Label fx3Label;
    @FXML private DynamicSlider ecoKnob;
    @FXML private Label ecoValLabel;

    @FXML private LedButton flangerBtn;
    @FXML private DynamicSlider flangerKnob;
    @FXML private Label fx1Label;
    @FXML private Label flangerValLabel;

    @FXML private LedButton reverbBtn;
    @FXML private DynamicSlider reverbKnob;
    @FXML private Label fx2Label;
    @FXML private Label reverbValLabel;

    @FXML
    public void initialize() {
        updateLabelColor(fx3Label, false);
        updateLabelColor(fx1Label, false);
        updateLabelColor(fx2Label, false);
        
        updateSliderIdleState(ecoKnob, false);
        updateSliderIdleState(flangerKnob, false);
        updateSliderIdleState(reverbKnob, false);

        if (ecoValLabel != null) ecoValLabel.setText(String.format("%.0f%%", ecoKnob.getValue() * 100));
        if (flangerValLabel != null) flangerValLabel.setText(String.format("%.0f%%", flangerKnob.getValue() * 100));
        if (reverbValLabel != null) reverbValLabel.setText(String.format("%.0f%%", reverbKnob.getValue() * 100));

        if (ecoValLabel != null) ecoValLabel.setOpacity(0.4);
        if (flangerValLabel != null) flangerValLabel.setOpacity(0.4);
        if (reverbValLabel != null) reverbValLabel.setOpacity(0.4);

        // Initialize listeners. They will sync with deckControls when it's set.
        setupListeners();
    }

    private void updateLabelColor(Label label, boolean isOn) {
        if (label == null) return;
        String color = isOn ? "#90ee90" : "#ffffff";
        label.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: Arial, sans-serif;");
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

    public void setDeckControls(DeckControls controls) {
        this.deckControls = controls;
        // Optionally, sync initial state from controls to UI here
        if (controls != null) {
            eqLowKnob.setValue(controls.getEqLow());
            eqMidKnob.setValue(controls.getEqMid());
            eqHighKnob.setValue(controls.getEqHigh());
            
            flangerBtn.setSelected(controls.isFlangerEnabled());
            flangerKnob.setValue(controls.getFlangerWet());
            
            reverbBtn.setSelected(controls.isReverbEnabled());
            reverbKnob.setValue(controls.getReverbWet());
            
            ecoBtn.setSelected(controls.isEcoEnabled());
            ecoKnob.setValue(controls.getEcoWet());
        }
    }

    private void setupListeners() {
        if (eqLowKnob != null) {
            eqLowKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (deckControls != null) deckControls.setEqLow(newVal.doubleValue());
            });
        }
        if (eqMidKnob != null) {
            eqMidKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (deckControls != null) deckControls.setEqMid(newVal.doubleValue());
            });
        }
        if (eqHighKnob != null) {
            eqHighKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (deckControls != null) deckControls.setEqHigh(newVal.doubleValue());
            });
        }

        if (ecoBtn != null) {
            ecoBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateLabelColor(fx3Label, newVal);
                updateSliderIdleState(ecoKnob, newVal);
                if (ecoValLabel != null) ecoValLabel.setOpacity(newVal ? 1.0 : 0.4);
                if (deckControls != null) deckControls.setEcoEnabled(newVal);
            });
        }
        if (ecoKnob != null) {
            ecoKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (ecoValLabel != null) ecoValLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
                if (deckControls != null) deckControls.setEcoWet(newVal.doubleValue());
            });
        }

        if (flangerBtn != null) {
            flangerBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateLabelColor(fx1Label, newVal);
                updateSliderIdleState(flangerKnob, newVal);
                if (flangerValLabel != null) flangerValLabel.setOpacity(newVal ? 1.0 : 0.4);
                if (deckControls != null) deckControls.setFlangerEnabled(newVal);
            });
        }
        if (flangerKnob != null) {
            flangerKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (flangerValLabel != null) flangerValLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
                if (deckControls != null) deckControls.setFlangerWet(newVal.doubleValue());
            });
        }

        if (reverbBtn != null) {
            reverbBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateLabelColor(fx2Label, newVal);
                updateSliderIdleState(reverbKnob, newVal);
                if (reverbValLabel != null) reverbValLabel.setOpacity(newVal ? 1.0 : 0.4);
                if (deckControls != null) deckControls.setReverbEnabled(newVal);
            });
        }
        if (reverbKnob != null) {
            reverbKnob.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (reverbValLabel != null) reverbValLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
                if (deckControls != null) deckControls.setReverbWet(newVal.doubleValue());
            });
        }
    }
}
