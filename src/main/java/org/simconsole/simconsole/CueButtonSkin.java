package org.simconsole.simconsole;

import javafx.scene.input.MouseEvent;

/**
 * Skin for CueButton. Inherits the square LED button aesthetics
 * but adds the interactive logic for setting/removing bookmarks via Shift+Click.
 */
public class CueButtonSkin extends LedButtonSquareSkin {

    public CueButtonSkin(CueButton control) {
        super(control);

        // Add the bookmark logic here in the skin, keeping the controller clean
        control.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.isShiftDown()) {
                // Remove bookmark
                if (control.getHasBookmark()) {
                    control.setHasBookmark(false);
                    control.setBookmarkPosition(-1);
                }
            } else {
                if (!control.getHasBookmark()) {
                    // Set bookmark
                    control.setHasBookmark(true);
                    control.setBookmarkPosition(0.0); // Placeholder
                }
            }
            event.consume();
        });
    }
}
