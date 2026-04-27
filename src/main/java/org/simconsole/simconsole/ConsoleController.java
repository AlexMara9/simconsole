package org.simconsole.simconsole;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class ConsoleController {
	@FXML private Label volume;
	@FXML private Slider volumeSlider;
	@FXML public void initialize(){
		initListeners();
	}
	private void initListeners(){
		volumeSlider.valueProperty().addListener((observableValue, oldValue, newValue) ->{
			volume.setText("volume: " + newValue.intValue() + "%");
		});
	}
}
