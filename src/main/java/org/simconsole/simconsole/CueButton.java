package org.simconsole.simconsole;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Skin;

/**
 * Custom button for CUE points.
 * Extends LedButton for visuals, but exposes bookmark-specific properties.
 */
public class CueButton extends LedButton {

    private final BooleanProperty hasBookmark = new SimpleBooleanProperty(false);

    public CueButton() {
        super();
        this.setToggleMode(false); // We handle the LED state manually via hasBookmark
        
        // The LED should be ON only when a bookmark is present
        this.selectedProperty().bind(hasBookmark);
        
        this.getStyleClass().add("cue-button");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CueButtonSkin(this);
    }

    public BooleanProperty hasBookmarkProperty() {
        return hasBookmark;
    }

    public boolean getHasBookmark() {
        return hasBookmark.get();
    }

    public void setHasBookmark(boolean val) {
        hasBookmark.set(val);
    }
}
