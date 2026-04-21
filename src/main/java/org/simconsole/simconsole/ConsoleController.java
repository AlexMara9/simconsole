package org.simconsole.simconsole;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class ConsoleController {
	@FXML
	private Label welcomeText;
	@FXML
	private Label volume;
	@FXML
	private Slider volumeSlider;

	@FXML
	protected void onHelloButtonClick() {
		welcomeText.setText("Welcome to JavaFX Application!");
	}

	@FXML
	protected void onVolumeSliderSlide() {
		volume.setText("volume");
	}@FXML
	protected void onVolumeSliderRelease() {
		volume.setText("");
	}
}
