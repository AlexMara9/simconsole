package org.simconsole.simconsole.components;
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
        control.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.isShiftDown()) {
                if (control.getHasBookmark()) {
                    control.setHasBookmark(false);
                }
            } else {
                if (!control.getHasBookmark()) {
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
