package org.simconsole.simconsole;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;

public class SeekSliderController {
	@FXML private GraphicSlider seekSlider;

	private boolean isUpdatingFromBackend = false;

	public GraphicSlider getGraphicSlider() { return seekSlider; }

	@FXML void initialize(){
	}

	public void setDeck(Deck deck) {
		if (deck == null) return;
		
		// Sincronizza UI -> Backend (User scrub)
		seekSlider.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
			if (!isUpdatingFromBackend && deck.getCurrentTrack() != null) {
				double sampleRate = deck.getCurrentTrack().getSampleRate();
				double targetPlayhead = newVal.doubleValue() * 2.0 * sampleRate;
				deck.setPlayheadDouble(targetPlayhead);
			}
		});

		// Sincronizza Backend -> UI (Playback) & Track Loading
		AnimationTimer timer = new AnimationTimer() {
			private Tracks lastTrack = null;

			@Override
			public void handle(long now) {
				Tracks currentTrack = deck.getCurrentTrack();
				
				// Se la traccia è cambiata (o appena impostata)
				if (currentTrack != lastTrack) {
					lastTrack = currentTrack;
					if (currentTrack != null) {
						String title = currentTrack.getTitle();
						seekSlider.setSongName(title != null ? title : "Unknown");
						
						double[] audioData = currentTrack.getAudioData();
						double totalSec = (audioData != null) ? (audioData.length / 2.0 / currentTrack.getSampleRate()) : 0;
						seekSlider.setTotalTime(totalSec);
						seekSlider.setWaveform(currentTrack.getWaveformPreview());
					} else {
						seekSlider.setSongName("No Track");
						seekSlider.setTotalTime(1.0);
						seekSlider.setWaveform(new float[0]);
					}
				}

				if (currentTrack != null) {
					// Aggiorniamo sempre lo slider per mostrare l'onda anche in pausa
					// se l'utente sta muovendo la testina e rilascia, vogliamo
					// che l'UI rifletta il playhead esatto.
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
