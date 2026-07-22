package org.simconsole.simconsole.controllers;
import org.simconsole.simconsole.components.GraphicSlider;
import org.simconsole.simconsole.components.CueButton;
import org.simconsole.simconsole.models.Deck;
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
    }
    public void setDeckAndSlider(Deck deck, GraphicSlider slider) {
        this.deck = deck;
        this.slider = slider;
        setupCue(cue1, "CUE1", javafx.scene.paint.Color.web("#f83b3b"));
        setupCue(cue2, "CUE2", javafx.scene.paint.Color.web("#f8cc3b"));
        setupCue(cue3, "CUE3", javafx.scene.paint.Color.web("#71f83b"));
        setupCue(cue4, "CUE4", javafx.scene.paint.Color.web("#3be8f8"));
    }
    private void setupCue(CueButton btn, String id, javafx.scene.paint.Color color) {
        btn.setOnMouseClicked(e -> {
            if (slider == null || deck == null || deck.getCurrentTrack() == null) return;
            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY || e.isShiftDown()) {
                slider.removeCue(id);
                btn.setHasBookmark(false);
            } else if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                if (slider.getCuePoints().containsKey(id)) {
                    double pos = slider.getCuePoints().get(id).position;
                    slider.setCurrentTime(pos);
                    if (deck.getCurrentTrack() != null) {
                        double sampleRate = deck.getCurrentTrack().getSampleRate();
                        double targetPlayhead = pos * 2.0 * sampleRate;
                        deck.setPlayheadDouble(targetPlayhead);
                    }
                } else {
                    slider.setCue(id, color);
                    btn.setHasBookmark(true);
                }
            }
        });
    }
    public void clearCues() {
        cue1.setHasBookmark(false);
        cue2.setHasBookmark(false);
        cue3.setHasBookmark(false);
        cue4.setHasBookmark(false);
        if (slider != null) {
            slider.clearAllCues();
        }
    }
}