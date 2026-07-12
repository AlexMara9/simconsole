package org.simconsole.simconsole;

import javafx.scene.Node;

/**
 * Skin for PlayButton. Inherits the round LED button aesthetics
 * but adds the interactive visual logic to swap the play/pause icon.
 */
public class PlayButtonSkin extends LedButtonRoundSkin {

    public PlayButtonSkin(PlayButton control) {
        super(control);

        control.selectedProperty().addListener((obs, oldVal, isPlaying) -> {
            updateIconState(control, isPlaying);
        });

        control.graphicProperty().addListener((obs, oldVal, newVal) -> {
            updateIconState(control, control.isSelected());
        });
        updateIconState(control, control.isSelected());
    }

    private void updateIconState(PlayButton control, boolean isPlaying) {
        Node graphic = control.getGraphic();
        if (graphic != null) {
            graphic.getStyleClass().removeAll("icon-play", "icon-pause");
            graphic.getStyleClass().add(isPlaying ? "icon-pause" : "icon-play");
        }
    }
}
