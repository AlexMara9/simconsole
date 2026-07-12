package org.simconsole.simconsole;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;

public class CueController {

    @FXML private HBox buttonsContainer;
    @FXML private CueButton cue1;
    @FXML private CueButton cue2;
    @FXML private CueButton cue3;
    @   FXML private CueButton cue4;

    public void initialize() {
        if (cue1 != null) setupCueButton(cue1);
        if (cue2 != null) setupCueButton(cue2);
        if (cue3 != null) setupCueButton(cue3);
        if (cue4 != null) setupCueButton(cue4);
    }

    private void setupCueButton(CueButton btn) {
        btn.setOnMouseClicked((MouseEvent event) -> {
            if (event.isShiftDown()) {
                // Remove the bookmark
                if (btn.getHasBookmark()) {
                    btn.setHasBookmark(false);
                    btn.setBookmarkPosition(-1);
                }
            } else {
                if (!btn.getHasBookmark()) {
                    // Set the bookmark
                    btn.setHasBookmark(true);
                    btn.setBookmarkPosition(0.0); // Placeholder
                } else {
                    // Jump to the bookmark
                }
            }
            event.consume();
        });
    }
}