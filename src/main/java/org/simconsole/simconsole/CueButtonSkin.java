package org.simconsole.simconsole;

import javafx.scene.input.MouseEvent;

/**
 * Skin for CueButton. Inherits the square LED button aesthetics
 * but adds the interactive logic for setting/removing bookmarks via Shift+Click.
 */
public class CueButtonSkin extends LedButtonSquareSkin {

    public CueButtonSkin(CueButton control) {
        super(control);

        control.selectedProperty().addListener((obs, old, isSelected) -> updatePadState(control));
        control.armedProperty().addListener((obs, old, isArmed) -> updatePadState(control));
        updatePadState(control);

        // Add the bookmark logic here in the skin, keeping the controller clean
        control.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.isShiftDown()) {
                // Remove bookmark
                if (control.getHasBookmark()) {
                    control.setHasBookmark(false);
                }
            } else {
                if (!control.getHasBookmark()) {
                    // Set bookmark
                    control.setHasBookmark(true);
                }
            }
            event.consume();
        });
    }

    private void updatePadState(CueButton control) {
        if (control.isSelected() || control.isArmed()) {
            if (!control.getStyleClass().contains("pad-lit")) {
                control.getStyleClass().add("pad-lit");
            }
        } else {
            control.getStyleClass().remove("pad-lit");
        }
    }
}
