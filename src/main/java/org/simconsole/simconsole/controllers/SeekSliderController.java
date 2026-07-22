package org.simconsole.simconsole.controllers;
import org.simconsole.simconsole.components.GraphicSlider;
import org.simconsole.simconsole.models.Deck;
import org.simconsole.simconsole.models.Tracks;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;

/**
 * Controller for the graphic seek slider displaying track waveform, current playback time, and overall progress.
 */
public class SeekSliderController {
	@FXML private GraphicSlider seekSlider;
	private boolean isUpdatingFromBackend = false;
	private Runnable onTrackChanged;
	public GraphicSlider getGraphicSlider() { return seekSlider; }
	public void setOnTrackChanged(Runnable action) {
		this.onTrackChanged = action;
	}
	@FXML void initialize(){
	}
	public void setDeck(Deck deck) {
		if (deck == null) return;
		seekSlider.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
			if (!isUpdatingFromBackend && deck.getCurrentTrack() != null) {
				double sampleRate = deck.getCurrentTrack().getSampleRate();
				double targetPlayhead = newVal.doubleValue() * 2.0 * sampleRate;
				deck.setPlayheadDouble(targetPlayhead);
			}
		});
		AnimationTimer timer = new AnimationTimer() {
			private Tracks lastTrack = null;
			@Override
			public void handle(long now) {
				Tracks currentTrack = deck.getCurrentTrack();
				if (currentTrack != lastTrack) {
					lastTrack = currentTrack;
					if (onTrackChanged != null) {
						onTrackChanged.run();
					}
					if (currentTrack != null) {
						String title = currentTrack.getTitle();
						seekSlider.setSongName(title != null ? title : "Unknown");
						double[] audioData = currentTrack.getAudioData();
						double totalSec = (audioData != null) ? (audioData.length / 2.0 / currentTrack.getSampleRate()) : 0;
						seekSlider.setTotalTime(totalSec);
						seekSlider.setWaveform(currentTrack.getWaveformPreview());
					} else {
						seekSlider.setSongName("no track");
						seekSlider.setTotalTime(1.0);
						seekSlider.setWaveform(new float[0]);
					}
				}
				if (currentTrack != null) {
					double timeSec = (deck.getPlayheadDouble() / 2.0) / currentTrack.getSampleRate();
					isUpdatingFromBackend = true;
					seekSlider.setCurrentTime(timeSec);
					isUpdatingFromBackend = false;
				}
			}
		};
		timer.start();
	}
}
