package org.simconsole.simconsole;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class PlaybackController {
	/* responsive */
	private static final double BUTTON_WIDTH_FACTOR = 0.25; // compared to the HBox
	private static final double BUTTON_CONTAINER_SPACING_FACTOR = 0.125;// compared to the HBox
	private static final double ICON_SIZE_FACTOR = 0.6;

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
			DoubleBinding minDim = Bindings.createDoubleBinding(
					() -> Math.min(buttonsContainer.getWidth() * BUTTON_WIDTH_FACTOR, buttonsContainer.getHeight()),
					buttonsContainer.widthProperty(),
					buttonsContainer.heightProperty()
			);
			b.prefHeightProperty().bind(minDim);
			b.prefWidthProperty().bind(minDim);
			if(b.getGraphic() instanceof Region icon){
				icon.prefWidthProperty().bind(minDim.multiply(ICON_SIZE_FACTOR));
				icon.prefHeightProperty().bind(minDim.multiply(ICON_SIZE_FACTOR));
			}
		}
		buttonsContainer.spacingProperty().bind(buttonsContainer.widthProperty().multiply(BUTTON_CONTAINER_SPACING_FACTOR));
	}
}
