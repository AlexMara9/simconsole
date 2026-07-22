package org.simconsole.simconsole.components;

import javafx.scene.control.Skin;

/**
 * Custom button for Play/Pause.
 * Extends LedButton for visuals and toggle logic, but uses PlayButtonSkin
 * to handle the automatic swapping of play and pause icons.
 */
public class PlayButton extends LedButton {

    public PlayButton() {
        super();
        this.setToggleMode(true);
        this.setRoundShape(true);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PlayButtonSkin(this);
    }
}
