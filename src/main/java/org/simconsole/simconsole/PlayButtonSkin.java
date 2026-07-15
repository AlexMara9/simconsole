package org.simconsole.simconsole;

import javafx.scene.Node;

/**
 * Skin for PlayButton. Inherits the round LED button aesthetics
 * but adds the interactive visual logic to swap the play/pause icon.
 */
public class PlayButtonSkin extends LedButtonRoundSkin {

    public PlayButtonSkin(PlayButton control) {
        super(control);
    }
}
