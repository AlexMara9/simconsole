package org.simconsole.simconsole;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

/**
 * Controller that connects the UI to the app logic
 */
public class ConsoleController {

	private static final int MIN_FONT_SIZE = 0;
	private static final double FONT_SCALE_FACTOR = 50.0;

	@FXML private GridPane grid;

	/**
	 * init function
	 */
	@FXML
	public void initialize(){
		initResponsiveness();
	}

	private void initResponsiveness(){
		// grid & general UI scale
		grid.heightProperty().addListener((o,n,j)-> updateGridFontSize());
		grid.widthProperty().addListener((o,n,j)-> updateGridFontSize());
	}

	private void updateGridFontSize() {
		double minWindowDim = Math.min(grid.getWidth(), grid.getHeight());
		if (minWindowDim > 0) {
			// scale factor to keep font size proportional
			double fontSize = minWindowDim / FONT_SCALE_FACTOR;
			fontSize = Math.max(MIN_FONT_SIZE, fontSize);
			grid.setStyle(String.format(java.util.Locale.US, "-fx-font-size: %.2fpx;", fontSize));
		}
	}
}
