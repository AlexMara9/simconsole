package org.simconsole.simconsole;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    
    @FXML private CueController cueLeftController;
    @FXML private CueController cueRightController;
    
    @FXML private PitchController pitchLeftController;
    @FXML private PitchController pitchRightController;
    
    @FXML private MixerController mixerController;

    @FXML private EffectsController effectsLeftController;
    @FXML private EffectsController effectsRightController;
    
    private Deck leftDeck;
    private Deck rightDeck;
    
    // Injected decks
    @FXML private DynamicDeck deckLeft;
    @FXML private DynamicDeck deckRight;

	@FXML private PlaylistController playlistController;

	@FXML
	public void initialize(){
		initResponsiveness();
	}
    
    public void setDecks(Deck leftDeck, Deck rightDeck) {
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;

        // Propaga i deck ai controller secondari
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
        
        if (pitchLeftController != null) {
            pitchLeftController.setDeck(leftDeck);
        }
        if (pitchRightController != null) {
            pitchRightController.setDeck(rightDeck);
        }
        
        if (cueLeftController != null && seekLeftController != null) {
            cueLeftController.setDeckAndSlider(leftDeck, seekLeftController.getGraphicSlider());
        }
        if (cueRightController != null && seekRightController != null) {
            cueRightController.setDeckAndSlider(rightDeck, seekRightController.getGraphicSlider());
        }
        
        if (mixerController != null) {
            mixerController.setDecks(leftDeck, rightDeck);
        }

        if (effectsLeftController != null) {
            effectsLeftController.setDeck(leftDeck);
            effectsLeftController.setDeckControls(leftDeck.getControls());
        }
        if (effectsRightController != null) {
            effectsRightController.setDeck(rightDeck);
            effectsRightController.setDeckControls(rightDeck.getControls());
        }
        
        if (playlistController != null) {
            playlistController.setDecks(leftDeck, rightDeck);
        }
        
        if (deckLeft != null) {
            deckLeft.scrubbingProperty().addListener((obs, oldVal, newVal) -> {
                if (leftDeck != null) leftDeck.setScrubbing(newVal);
            });
            deckLeft.rotationAngleProperty().addListener((obs, oldVal, newVal) -> {
                if (deckLeft.isScrubbing() && leftDeck != null && leftDeck.getCurrentTrack() != null) {
                    double angleDelta = newVal.doubleValue() - oldVal.doubleValue();
                    double timeDelta = (angleDelta / 360.0) * 1.8; // 1 giro = 1.8 secondi a 33.3 RPM
                    double framesDelta = timeDelta * leftDeck.getCurrentTrack().getSampleRate();
                    double newPlayhead = leftDeck.getPlayheadDouble() + framesDelta * 2.0;
                    if (newPlayhead < 0) newPlayhead = 0;
                    leftDeck.setPlayheadDouble(newPlayhead);
                }
            });
        }
        if (deckRight != null) {
            deckRight.scrubbingProperty().addListener((obs, oldVal, newVal) -> {
                if (rightDeck != null) rightDeck.setScrubbing(newVal);
            });
            deckRight.rotationAngleProperty().addListener((obs, oldVal, newVal) -> {
                if (deckRight.isScrubbing() && rightDeck != null && rightDeck.getCurrentTrack() != null) {
                    double angleDelta = newVal.doubleValue() - oldVal.doubleValue();
                    double timeDelta = (angleDelta / 360.0) * 1.8;
                    double framesDelta = timeDelta * rightDeck.getCurrentTrack().getSampleRate();
                    double newPlayhead = rightDeck.getPlayheadDouble() + framesDelta * 2.0;
                    if (newPlayhead < 0) newPlayhead = 0;
                    rightDeck.setPlayheadDouble(newPlayhead);
                }
            });
        }
        
        // Timer per sincronizzare l'interfaccia con lo stato dei deck backend (Backend -> UI)
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                double elapsedSeconds = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                
                // Evitiamo salti enormi se il thread UI si blocca per un attimo (es. durante il resize)
                if (elapsedSeconds > 0.1) elapsedSeconds = 0.016; 
                
                // Calcoliamo quanti gradi deve ruotare in base al tempo effettivamente trascorso
                double baseRotationDelta = (elapsedSeconds / 1.8) * 360.0; // 33.3 RPM

                if (deckLeft != null && leftDeck != null) {
                    if (!deckLeft.isScrubbing() && leftDeck.isPlaying() && leftDeck.getCurrentTrack() != null) {
                        double pitch = leftDeck.getControls().getPitch();
                        deckLeft.setRotationAngle(deckLeft.getRotationAngle() + baseRotationDelta * pitch);
                    }
                }
                if (deckRight != null && rightDeck != null) {
                    if (!deckRight.isScrubbing() && rightDeck.isPlaying() && rightDeck.getCurrentTrack() != null) {
                        double pitch = rightDeck.getControls().getPitch();
                        deckRight.setRotationAngle(deckRight.getRotationAngle() + baseRotationDelta * pitch);
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
