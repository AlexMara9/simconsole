package org.simconsole.simconsole;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class CueController {
	private static final double BUTTON_WIDTH_FACTOR = 0.25; // compared to the HBox
//	private static final double BUTTON_CONTAINER_SPACING_FACTOR = 0.125;// compared to the HBox
//	private static final double ICON_SIZE_FACTOR = 0.6;

	@FXML private HBox buttonsContainer;
	@FXML private DynamicSquareButton cue1;
	@FXML private DynamicSquareButton cue2;
	@FXML private DynamicSquareButton cue3;
	@FXML private DynamicSquareButton cue4;

	public void initialize(){
		responsive();
	}
	private void responsive(){
		DynamicSquareButton [] dynButtons = {cue1, cue2, cue3, cue4};

		for (DynamicSquareButton b : dynButtons){
			DoubleBinding minDim = Bindings.createDoubleBinding(
					() -> Math.min(buttonsContainer.getWidth() * BUTTON_WIDTH_FACTOR, buttonsContainer.getHeight()),
					buttonsContainer.widthProperty(),
					buttonsContainer.heightProperty()
			);
			b.prefHeightProperty().bind(minDim);
			b.prefWidthProperty().bind(minDim);
		}

//		buttonsContainer.spacingProperty().bind(buttonsContainer.widthProperty().multiply(BUTTON_CONTAINER_SPACING_FACTOR));
	}
}