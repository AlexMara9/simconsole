package org.simconsole.simconsole;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class PlaybackController {
	@FXML private HBox buttonsContainer;
	@FXML private Button unskipButton;
	@FXML private Button playButton;
	@FXML private Button skipButton;

	@FXML
	public void initialize(){
		responsive();
	}
	private void responsive(){
		Button [] buttons = {unskipButton, playButton, skipButton};

		for (Button b : buttons){
			b.prefHeightProperty().bind(buttonsContainer.heightProperty().multiply(1));
		}
		unskipButton.prefWidthProperty().bind(buttonsContainer.widthProperty().multiply(0.33));
		skipButton.prefWidthProperty().bind(buttonsContainer.widthProperty().multiply(0.33));
		playButton.prefWidthProperty().bind(buttonsContainer.widthProperty().multiply(0.32));
		buttonsContainer.spacingProperty().bind(buttonsContainer.widthProperty().multiply(0.1));
	}
}
