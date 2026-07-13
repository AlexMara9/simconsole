package org.simconsole.simconsole;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * Controller that connects the UI to the app logic
 */
public class ConsoleController {
	// grid & general UI scale
	private static final int MIN_FONT_SIZE = 0;
	private static final double FONT_SCALE_FACTOR = 50.0;
	// playback
	private static final double BUTTON_CONTAINER_SPACING_FACTOR = 0.125;// compared to the HBox

	// grid & general UI scale
	@FXML private GridPane grid;
	// playback
	@FXML private HBox testest;
    
    // Injected included controllers
    @FXML private PlaybackController playbackLeftController;
    @FXML private PlaybackController playbackRightController;
    
    @FXML private SeekSliderController seekLeftController;
    @FXML private SeekSliderController seekRightController;
    
    // Injected decks
    @FXML private DynamicDeck deckLeft;
    @FXML private DynamicDeck deckRight;

	/**
	 * init function
	 */
	@FXML
	public void initialize(){
		initResponsiveness();
	}
    
    public void setDecks(Deck leftDeck, Deck rightDeck) {
        if (playbackLeftController != null) {
            playbackLeftController.setDeck(leftDeck);
        }
        if (playbackRightController != null) {
            playbackRightController.setDeck(rightDeck);
        }
        
        if (seekLeftController != null) {
            seekLeftController.setDeck(leftDeck);
        }
        if (seekRightController != null) {
            seekRightController.setDeck(rightDeck);
        }
        
        // Timer per sincronizzare l'interfaccia con lo stato dei deck backend
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                if (deckLeft != null && leftDeck != null) {
                    if (deckLeft.isSpinning() != leftDeck.isPlaying()) {
                        deckLeft.setSpinning(leftDeck.isPlaying());
                    }
                }
                if (deckRight != null && rightDeck != null) {
                    if (deckRight.isSpinning() != rightDeck.isPlaying()) {
                        deckRight.setSpinning(rightDeck.isPlaying());
                    }
                }
            }
        };
        timer.start();
    }

	private void initResponsiveness(){
		// grid & general UI scale
		grid.heightProperty().addListener((o,n,j)-> updateGridFontSize());
		grid.widthProperty().addListener((o,n,j)-> updateGridFontSize());

		// playback
//		testest.spacingProperty().bind(testest.widthProperty().multiply(BUTTON_CONTAINER_SPACING_FACTOR));

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
