package org.simconsole.simconsole;

import javafx.fxml.FXML;
import javafx.scene.layout.Region;

public class PlaybackController {

    @FXML private LedButton playButton;

    @FXML
    public void initialize() {
        if (playButton != null) {
            Region graphic = (Region) playButton.getGraphic();
            
            // Invert the icon based on the state
            // Standard: when paused show PLAY, when playing show PAUSE
            playButton.selectedProperty().addListener((obs, oldVal, isPlaying) -> {
                graphic.getStyleClass().removeAll("icon-play", "icon-pause");
                if (isPlaying) {
                    graphic.getStyleClass().add("icon-pause");
                } else {
                    graphic.getStyleClass().add("icon-play");
                }
            });
            
            // Initial state
            boolean isPlaying = playButton.isSelected();
            graphic.getStyleClass().removeAll("icon-play", "icon-pause");
            graphic.getStyleClass().add(isPlaying ? "icon-pause" : "icon-play");
        }
    }
}
