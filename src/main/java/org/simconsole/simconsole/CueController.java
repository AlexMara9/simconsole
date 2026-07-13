package org.simconsole.simconsole;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public class CueController {

    @FXML private HBox buttonsContainer;
    @FXML private CueButton cue1;
    @FXML private CueButton cue2;
    @FXML private CueButton cue3;
    @FXML private CueButton cue4;

    private Deck deck;
    private GraphicSlider slider;

    public void initialize() {
        // Le logiche dei singoli bottoni CUE verranno applicate quando avremo il deck
    }

    public void setDeckAndSlider(Deck deck, GraphicSlider slider) {
        this.deck = deck;
        this.slider = slider;
        
        setupCue(cue1, "CUE1", javafx.scene.paint.Color.web("#1eff00")); // Verde
        setupCue(cue2, "CUE2", javafx.scene.paint.Color.web("#ff2a2a")); // Rosso
        setupCue(cue3, "CUE3", javafx.scene.paint.Color.web("#ffb300")); // Arancio/Giallo
        setupCue(cue4, "CUE4", javafx.scene.paint.Color.web("#00bfff")); // Azzurro
    }

    private void setupCue(CueButton btn, String id, javafx.scene.paint.Color color) {
        // Impostiamo il testo col nome del cue
        btn.setText(id.replace("CUE", ""));

        btn.setOnMouseClicked(e -> {
            if (slider == null || deck == null) return;

            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                // Tasto destro: Rimuovi CUE
                slider.removeCue(id);
                btn.setSelected(false);
            } else if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                // Tasto sinistro: Imposta CUE o Salta al CUE
                if (slider.getCuePoints().containsKey(id)) {
                    // CUE esistente -> Salta
                    double pos = slider.getCuePoints().get(id).position;
                    slider.setCurrentTime(pos); 
                    
                    // Sincronizza il playhead del deck audio
                    if (deck.getCurrentTrack() != null) {
                        double sampleRate = deck.getCurrentTrack().getSampleRate();
                        double targetPlayhead = pos * 2.0 * sampleRate;
                        deck.setPlayheadDouble(targetPlayhead);
                    }
                } else {
                    // CUE non impostato -> Imposta alla posizione corrente
                    slider.setCue(id, color);
                    btn.setSelected(true); // Accendi il pulsante
                }
            }
        });
    }
}