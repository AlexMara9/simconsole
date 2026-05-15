package org.simconsole.simconsole;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class PlaybackController {
	/* responsive */
	private static final double BUTTON_WIDTH_FACTOR = 0.5; // compared to the HBox
	private static final double BUTTON_CONTAINER_SPACING_FACTOR = 0.05;// compared to the HBox
	private static final double ICON_SIZE_FACTOR = 0.3;

	@FXML private HBox buttonsContainer;
	@FXML private DynamicRoundButton rewindButton;
	@FXML private DynamicRoundButton playButton;

	@FXML
	public void initialize(){
		responsive();
	}
	private void responsive(){
		DynamicRoundButton [] buttons = {rewindButton, playButton};

		for (DynamicRoundButton b : buttons){
			DoubleBinding minDim = Bindings.createDoubleBinding(
					() -> Math.min(buttonsContainer.getWidth() * BUTTON_WIDTH_FACTOR, buttonsContainer.getHeight() * 0.8), // added * 0.8 so it doesn't touch the edges completely if height is the limiting factor
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
