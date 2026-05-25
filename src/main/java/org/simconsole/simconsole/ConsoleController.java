package org.simconsole.simconsole;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.util.Comparator;

/**
 * Controller that connects the UI to the app logic
 */
public class ConsoleController {
	@FXML private GridPane grid;

	@FXML private Circle vinyl;

	@FXML private StackPane graphContainer;
	@FXML private Canvas graph;

	@FXML private StackPane songListContainer;
	@FXML private ListView<Tracks> searchResultsList;
	@FXML private ListView<Tracks> playlistList;
	@FXML private TextField searchField;
	@FXML private Button addTrackButton;
	@FXML private Button removeButton;
	@FXML private Button sortDurationButton;
	@FXML private Button sortTitleButton;
	@FXML private Button sortArtistButton;

	@FXML private StackPane slidersGrillContainer;
	@FXML private GridPane slidersGrill;

	@FXML private HBox trackButtonsContainer;
	@FXML private Button playButtonA;
	@FXML private Button playButtonB;
	@FXML private Button unskipButton1;
	@FXML private Button unskipButton2;
	@FXML private Slider s; // Horizontal slider
	@FXML private Label timeLabelA;
	@FXML private Label timeLabelB;

	private Deck deckA;
	private Deck deckB;
	private DeckControls controlsA;
	private DeckControls controlsB;
	private Crossfader crossfader;
	private AnimationTimer uiTimer;

	public void initialize(){
		initResponsiveness();
		initControls();
		
		uiTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				updateUI();
			}
		};
		uiTimer.start();
	}

	private void updateUI() {
		if (deckA != null && deckA.getCurrentTrack() != null && timeLabelA != null) {
			Tracks trackA = deckA.getCurrentTrack();
			int sampleRate = trackA.getSampleRate();
			double elapsedSeconds = deckA.getPlayheadDouble() / (sampleRate * 2.0);
			double totalSeconds = trackA.getDurationMs() / 1000.0;
			timeLabelA.setText(formatTime(elapsedSeconds) + " / " + formatTime(totalSeconds));
		}
		
		if (deckB != null && deckB.getCurrentTrack() != null && timeLabelB != null) {
			Tracks trackB = deckB.getCurrentTrack();
			int sampleRate = trackB.getSampleRate();
			double elapsedSeconds = deckB.getPlayheadDouble() / (sampleRate * 2.0);
			double totalSeconds = trackB.getDurationMs() / 1000.0;
			timeLabelB.setText(formatTime(elapsedSeconds) + " / " + formatTime(totalSeconds));
		}

		drawWaveform();
	}

	private String formatTime(double totalSeconds) {
		int minutes = (int) (totalSeconds / 60);
		int seconds = (int) (totalSeconds % 60);
		return String.format("%02d:%02d", minutes, seconds);
	}

	public void setupDecks(Deck deckA, DeckControls controlsA, Deck deckB, DeckControls controlsB) {
		this.deckA = deckA;
		this.controlsA = controlsA;
		this.deckB = deckB;
		this.controlsB = controlsB;
		this.crossfader = new Crossfader(controlsA, controlsB);

		setupSliders();
	}

	private void initResponsiveness(){
		double vinylRadiusSceneScaleFactor = (double) 1 / 8;

		double songListCellScaleFactor = 0.9;
		double songListLabelHeightCellScaleFactor = (double) 1 - songListCellScaleFactor;
		double songListLabelFontCellScaleFactor = songListLabelHeightCellScaleFactor * 0.8;

		double slidersGrillHeightCellScaleFactor = 0.9;
		double slidersGrillWidthCellScaleFactor = 1;

		double seekButtonsFontContainerScaleFactor = 0.5;

		// vinyl
		grid.heightProperty().addListener((o,n,j)->{
			if (grid.getWidth() > j.doubleValue()){
				vinyl.setRadius(j.doubleValue()* vinylRadiusSceneScaleFactor);
			}
		});
		grid.widthProperty().addListener((o,n,j)->{
			if (grid.getHeight() > j.doubleValue()){
				vinyl.setRadius(j.doubleValue()* vinylRadiusSceneScaleFactor);
			}
		});

		// graph
		graph.heightProperty().bind(graphContainer.heightProperty());
		graph.widthProperty().bind(graphContainer.widthProperty());
		graph.widthProperty().addListener((o, oldV, newV) -> drawWaveform());
		graph.heightProperty().addListener((o, oldV, newV) -> drawWaveform());

		// song list panels — sizing handled by VBox.vgrow in FXML

		// effects sliders
		slidersGrill.prefHeightProperty().bind(slidersGrillContainer.heightProperty().multiply(slidersGrillHeightCellScaleFactor));
		slidersGrill.prefWidthProperty().bind(slidersGrillContainer.widthProperty().multiply(slidersGrillWidthCellScaleFactor));

		// track seeking buttons
		DoubleBinding buttonsBoundSize = trackButtonsContainer.heightProperty().multiply(seekButtonsFontContainerScaleFactor);
		Button[] arr = {playButtonA, playButtonB, unskipButton1, unskipButton2};
		for (Button b : arr) {
			if (b != null) {
				b.fontProperty().bind(
						Bindings.createObjectBinding(
								() -> {
									double size = Math.round(buttonsBoundSize.get());
									return Font.font(size);
								},
								buttonsBoundSize
						)
				);
			}
		}
	}

	// --- Palette ---
	private static final Color COLOR_DECK_A = Color.web("#00e5ff"); // ciano luminoso
	private static final Color COLOR_DECK_B = Color.web("#00e676"); // verde fluorescente

	private void drawWaveform() {
		double w = Math.max(1, graph.getWidth());
		double h = Math.max(1, graph.getHeight());
		GraphicsContext gc = graph.getGraphicsContext2D();

		gc.clearRect(0, 0, w, h);

		// --- Background ---
		gc.setFill(Color.web("#0a0a0a"));
		gc.fillRect(0, 0, w, h);

		// --- Grid lines ---
		// Full-scale reference lines per deck (25% e 75% = bordi esterni)
		gc.setStroke(Color.web("#1c1c1c"));
		gc.setLineWidth(1.0);
		gc.strokeLine(0, h * 0.05, w, h * 0.05);
		gc.strokeLine(0, h * 0.45, w, h * 0.45);
		gc.strokeLine(0, h * 0.55, w, h * 0.55);
		gc.strokeLine(0, h * 0.95, w, h * 0.95);

		// Zero-axis per deck A e B
		gc.setStroke(Color.web("#2a2a2a"));
		gc.strokeLine(0, h * 0.25, w, h * 0.25);
		gc.strokeLine(0, h * 0.75, w, h * 0.75);

		// Separator line between the two decks
		gc.setStroke(Color.web("#222222"));
		gc.setLineWidth(2.0);
		gc.strokeLine(0, h / 2.0, w, h / 2.0);

		// --- Waveforms ---
		if (deckA != null && deckA.getCurrentTrack() != null) {
			drawSingleWaveform(gc, deckA, w, h, COLOR_DECK_A, true);
		}
		if (deckB != null && deckB.getCurrentTrack() != null) {
			drawSingleWaveform(gc, deckB, w, h, COLOR_DECK_B, false);
		}

		// --- Playhead cursors (drawn on top) ---
		drawPlayheadCursor(gc, w, h);

		// --- Deck labels ---
		drawDeckLabel(gc, "A", COLOR_DECK_A, 6, 6, true);
		drawDeckLabel(gc, "B", COLOR_DECK_B, 6, h / 2.0 + 6, false);
	}

	private void drawSingleWaveform(GraphicsContext gc, Deck deck, double w, double h, Color color, boolean isTopDeck) {
		Tracks track = deck.getCurrentTrack();
		float[] preview = track.getWaveformPreview(); // already RMS + smoothed from Tracks
		if (preview == null || preview.length == 0) return;

		int n = preview.length;
		double deckTop = isTopDeck ? 0 : h / 2.0;
		double deckH   = h / 2.0;
		double axisY   = deckTop + deckH / 2.0;
		double maxBar  = deckH / 2.0 * 0.90;

		// Normalize: always fills the full height regardless of track loudness
		float maxVal = 0;
		for (float v : preview) if (v > maxVal) maxVal = v;
		if (maxVal == 0) return;

		// 1 column per screen pixel, aggregate max within each column
		int cols = (int) Math.max(1, w);
		double barW = w / cols;

		// Playhead progress
		double[] audioData  = track.getAudioData();
		double totalSamples = (audioData != null && audioData.length > 0) ? audioData.length : 1;
		double playedX      = Math.max(0, Math.min(w, deck.getPlayheadDouble() / totalSamples * w));

		Color playedColor   = color;
		Color playedDim     = color.deriveColor(0, 1.0, 0.55, 1.0);
		Color unplayedColor = color.deriveColor(0, 0.55, 0.22, 0.95);
		Color unplayedDim   = color.deriveColor(0, 0.4, 0.12, 0.95);

		for (int col = 0; col < cols; col++) {
			// Map column → bucket range
			int bStart = (int)((double) col       / cols * n);
			int bEnd   = (int)((double)(col + 1)  / cols * n);
			if (bEnd > n) bEnd = n;
			if (bEnd <= bStart) bEnd = bStart + 1;
			if (bEnd > n) bEnd = n;

			// Peak within this column's buckets
			float peak = 0;
			for (int b = bStart; b < bEnd; b++) {
				if (preview[b] > peak) peak = preview[b];
			}

			// sqrt gamma: expands quiet zones, compresses loud — more visual dynamic range
			double norm   = peak / maxVal;
			double barH   = Math.pow(norm, 0.5) * maxBar;

			double x    = col * barW;
			double y    = axisY - barH;
			double rectH = barH * 2;
			if (rectH < 1) rectH = 1;

			boolean played = x < playedX;
			Color center = played ? playedColor   : unplayedColor;
			Color edge   = played ? playedDim     : unplayedDim;

			// Vertical gradient: bright at center (zero axis), dims toward peaks — CRT glow effect
			LinearGradient grad = new LinearGradient(
				0, y, 0, y + rectH, false, CycleMethod.NO_CYCLE,
				new Stop(0.0,  edge),
				new Stop(0.42, center),
				new Stop(0.50, center.brighter()),
				new Stop(0.58, center),
				new Stop(1.0,  edge)
			);
			gc.setFill(grad);
			gc.fillRect(x, y, Math.max(1, barW), rectH);
		}
	}

	private void drawPlayheadCursor(GraphicsContext gc, double w, double h) {
		if (deckA != null && deckA.getCurrentTrack() != null) {
			double[] audio = deckA.getCurrentTrack().getAudioData();
			double progress = deckA.getPlayheadDouble() / (audio != null && audio.length > 0 ? audio.length : 1);
			double x = progress * w;
			gc.setStroke(Color.WHITE);
			gc.setLineWidth(1.5);
			gc.strokeLine(x, 0, x, h / 2.0);
			double ts = 5.0;
			gc.setFill(Color.WHITE);
			gc.fillPolygon(
				new double[]{x - ts, x + ts, x},
				new double[]{h / 2.0 - ts * 1.8, h / 2.0 - ts * 1.8, h / 2.0 - 2},
				3
			);
		}
		if (deckB != null && deckB.getCurrentTrack() != null) {
			double[] audio = deckB.getCurrentTrack().getAudioData();
			double progress = deckB.getPlayheadDouble() / (audio != null && audio.length > 0 ? audio.length : 1);
			double x = progress * w;
			gc.setStroke(Color.WHITE);
			gc.setLineWidth(1.5);
			gc.strokeLine(x, h / 2.0, x, h);
			double ts = 5.0;
			gc.setFill(Color.WHITE);
			gc.fillPolygon(
				new double[]{x - ts, x + ts, x},
				new double[]{h / 2.0 + ts * 1.8, h / 2.0 + ts * 1.8, h / 2.0 + 2},
				3
			);
		}
	}

	private void drawDeckLabel(GraphicsContext gc, String label, Color color, double x, double y, boolean above) {
		gc.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
		gc.setFill(color.deriveColor(0, 1.0, 1.0, 0.8));
		gc.fillText(label, x, y + 10);
	}

	private void initControls() {
		// --- Cell Factory per playlistList ---
		if (playlistList != null) {
			playlistList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
				private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(8);
				private final javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
				private final javafx.scene.control.Label titleLabel = new javafx.scene.control.Label();
				private final javafx.scene.control.Label artistLabel = new javafx.scene.control.Label();
				private final javafx.scene.control.Label durationLabel = new javafx.scene.control.Label();
				private final javafx.scene.control.Label statusBadge = new javafx.scene.control.Label();
				private final javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();

				{
					container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
					javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
					
					spinner.setPrefSize(12, 12);
					spinner.setMinSize(12, 12);
					spinner.setMaxSize(12, 12);
					spinner.setProgress(-1.0); // Indeterminate
					
					statusBadge.setStyle("-fx-font-size: 8px; -fx-padding: 2 5 2 5; -fx-background-radius: 4; -fx-font-weight: bold;");
					titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
					artistLabel.setStyle("-fx-font-size: 10px;");
					durationLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
				}

				private String formatDuration(long ms) {
					if (ms <= 0) return "--:--";
					long totalSec = ms / 1000L;
					long min = totalSec / 60;
					long sec = totalSec % 60;
					return String.format("%02d:%02d", min, sec);
				}

				@Override
				protected void updateItem(Tracks item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setGraphic(null);
						setText(null);
					} else {
						titleLabel.setText(item.getTitle() != null ? item.getTitle() : "Senza Titolo");
						artistLabel.setText(item.getArtist() != null ? " • " + item.getArtist() : "");
						durationLabel.setText("[" + formatDuration(item.getDurationMs()) + "]");
						
						container.getChildren().clear();

						Tracks.TrackState state = item.getState();
						
						// Reset standard styles (readable dark colors)
						titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #111111;");
						artistLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #555555;");
						durationLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #444444;");

						if (state == Tracks.TrackState.DOWNLOAD_PENDING || state == Tracks.TrackState.DOWNLOADING) {
							statusBadge.setText("DOWNLOADING");
							statusBadge.setStyle("-fx-background-color: #00e5ff; -fx-text-fill: black; -fx-font-size: 8px; -fx-padding: 2 5 2 5; -fx-background-radius: 4; -fx-font-weight: bold;");
							titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #888888; -fx-font-style: italic;");
							artistLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaaaaa; -fx-font-style: italic;");
							container.getChildren().addAll(spinner, statusBadge, titleLabel, artistLabel, spacer, durationLabel);
						} else if (state == Tracks.TrackState.CONVERTING) {
							statusBadge.setText("DECODING");
							statusBadge.setStyle("-fx-background-color: #d500f9; -fx-text-fill: white; -fx-font-size: 8px; -fx-padding: 2 5 2 5; -fx-background-radius: 4; -fx-font-weight: bold;");
							titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #777777; -fx-font-style: italic;");
							artistLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999999; -fx-font-style: italic;");
							container.getChildren().addAll(spinner, statusBadge, titleLabel, artistLabel, spacer, durationLabel);
						} else if (state == Tracks.TrackState.FAILED) {
							statusBadge.setText("FAILED");
							statusBadge.setStyle("-fx-background-color: #ff3d00; -fx-text-fill: white; -fx-font-size: 8px; -fx-padding: 2 5 2 5; -fx-background-radius: 4; -fx-font-weight: bold;");
							titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #ff3d00;");
							artistLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #ff6e40;");
							container.getChildren().addAll(statusBadge, titleLabel, artistLabel, spacer, durationLabel);
						} else if (state == Tracks.TrackState.READY) {
							statusBadge.setText("READY");
							statusBadge.setStyle("-fx-background-color: #00e676; -fx-text-fill: black; -fx-font-size: 8px; -fx-padding: 2 5 2 5; -fx-background-radius: 4; -fx-font-weight: bold;");
							container.getChildren().addAll(statusBadge, titleLabel, artistLabel, spacer, durationLabel);
						} else {
							// LOCAL
							statusBadge.setText("LOCAL");
							statusBadge.setStyle("-fx-background-color: #ff1744; -fx-text-fill: white; -fx-font-size: 8px; -fx-padding: 2 5 2 5; -fx-background-radius: 4; -fx-font-weight: bold;");
							container.getChildren().addAll(statusBadge, titleLabel, artistLabel, spacer, durationLabel);
						}
						
						setGraphic(container);
					}
				}
			});
		}

		// --- + Aggiungi file locale alla playlist ---
		if (addTrackButton != null) {
			addTrackButton.setOnAction(e -> {
				FileChooser fc = new FileChooser();
				fc.setTitle("Seleziona traccia audio");
				fc.getExtensionFilters().add(
						new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aiff", "*.flac", "*.ogg")
				);
				Window window = addTrackButton.getScene().getWindow();
				File f = fc.showOpenDialog(window);
				if (f != null) {
					Tracks newTrack = new Tracks(f.getAbsolutePath());
					playlistList.getItems().add(newTrack);
					startDownloadAndConversion(newTrack);
				}
			});
		}

		// --- − Rimuovi dalla playlist ---
		if (removeButton != null) {
			removeButton.setOnAction(e -> {
				Tracks selected = playlistList.getSelectionModel().getSelectedItem();
				if (selected != null) playlistList.getItems().remove(selected);
			});
		}

		// --- Ordinamento playlist ---
		if (sortDurationButton != null) {
			sortDurationButton.setOnAction(e ->
				playlistList.getItems().sort(Comparator.comparingLong(Tracks::getDurationMs)));
		}
		if (sortTitleButton != null) {
			sortTitleButton.setOnAction(e ->
				playlistList.getItems().sort(Comparator.comparing(t -> t.toString().toLowerCase())));
		}
		if (sortArtistButton != null) {
			sortArtistButton.setOnAction(e ->
				playlistList.getItems().sort(Comparator.comparing(
						t -> (t.getArtist() != null ? t.getArtist() : "").toLowerCase())));
		}

		// --- Ricerca iTunes → popola searchResultsList ---
		if (searchField != null) {
			searchField.textProperty().addListener((obs, oldVal, newVal) -> {
				String query = newVal.trim();
				if (query.length() < 2) return;
				Thread t = new Thread(() -> {
					java.util.List<Tracks> results = MusicApiService.search(query, 20);
					javafx.application.Platform.runLater(() -> {
						searchResultsList.getItems().setAll(results);
					});
				});
				t.setDaemon(true);
				t.start();
			});
		}

		// --- Doppio click su risultato iTunes → aggiunge alla playlist ---
		if (searchResultsList != null) {
			searchResultsList.setOnMouseClicked(e -> {
				if (e.getClickCount() == 2) {
					Tracks selected = searchResultsList.getSelectionModel().getSelectedItem();
					if (selected != null && !playlistList.getItems().contains(selected)) {
						playlistList.getItems().add(selected);
						startDownloadAndConversion(selected);
					}
				}
			});
		}

		// --- Doppio click su traccia playlist → carica nel deck ---
		if (playlistList != null) {
			playlistList.setOnMouseClicked(e -> {
				if (e.getClickCount() == 2) {
					Tracks track = playlistList.getSelectionModel().getSelectedItem();
					if (track == null) return;
					
					Tracks.TrackState state = track.getState();
					if (state == Tracks.TrackState.READY || state == Tracks.TrackState.LOCAL) {
						loadTrackToDecks(track);
					} else if (state == Tracks.TrackState.DOWNLOADING || state == Tracks.TrackState.CONVERTING) {
						searchField.setPromptText("Attendi il completamento di: " + track.getTitle());
						javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
						pause.setOnFinished(ev -> searchField.setPromptText("Cerca artista o titolo..."));
						pause.play();
					} else if (state == Tracks.TrackState.FAILED || state == Tracks.TrackState.DOWNLOAD_PENDING) {
						startDownloadAndConversion(track);
					}
				}
			});
		}

		// --- Play / Pause deck A e B ---
		if (playButtonA != null) {
			playButtonA.setOnAction(e -> {
				if (deckA != null) {
					if (!deckA.isPlaying()) deckA.play(); else deckA.pause();
				}
			});
		}
		if (playButtonB != null) {
			playButtonB.setOnAction(e -> {
				if (deckB != null) {
					if (!deckB.isPlaying()) deckB.play(); else deckB.pause();
				}
			});
		}
	}

	private void startDownloadAndConversion(Tracks track) {
		if (track.getState() == Tracks.TrackState.READY || track.getState() == Tracks.TrackState.LOCAL) {
			return; // Already prepared
		}

		if (track.isFromApi()) {
			track.setState(Tracks.TrackState.DOWNLOADING);
		} else {
			track.setState(Tracks.TrackState.CONVERTING);
		}
		if (playlistList != null) {
			playlistList.refresh();
		}

		Thread t = new Thread(() -> {
			try {
				if (track.isFromApi() && track.getFilePath() == null) {
					// 1. Download full MP3
					String localPath = MusicApiService.downloadTrack(track);
					
					// 2. Set path on track (this extracts metadata)
					track.setFilePath(localPath);
					
					// Update UI to show we are now converting/decoding
					javafx.application.Platform.runLater(() -> {
						track.setState(Tracks.TrackState.CONVERTING);
						if (playlistList != null) {
							playlistList.refresh();
						}
					});
				}

				// 3. Pre-decode audio data (caches audioData in track)
				double[] audioData = track.getAudioData();
				if (audioData == null || audioData.length == 0) {
					throw new Exception("Decoding failed");
				}

				// 4. Pre-generate waveform (caches waveformPreview in track)
				track.getWaveformPreview();

				// 5. Done! Mark as READY
				javafx.application.Platform.runLater(() -> {
					track.setState(Tracks.TrackState.READY);
					if (playlistList != null) {
						playlistList.refresh();
					}
				});

			} catch (Exception ex) {
				ex.printStackTrace();
				javafx.application.Platform.runLater(() -> {
					track.setState(Tracks.TrackState.FAILED);
					if (playlistList != null) {
						playlistList.refresh();
					}
				});
			}
		});
		t.setDaemon(true);
		t.start();
	}

	/** Mostra un dialog per scegliere in quale deck caricare la traccia. */
	private void loadTrackToDecks(Tracks track) {
		ButtonType btnA      = new ButtonType("Deck A");
		ButtonType btnB      = new ButtonType("Deck B");
		ButtonType btnCancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.setTitle("Carica traccia");
		alert.setHeaderText(track.toString());
		alert.setContentText("In quale deck vuoi caricare questa traccia?");
		alert.getButtonTypes().setAll(btnA, btnB, btnCancel);

		alert.showAndWait().ifPresent(choice -> {
			if (choice == btnA && deckA != null) deckA.loadTrack(track);
			else if (choice == btnB && deckB != null) deckB.loadTrack(track);
		});
	}

	private void setupSliders(){
		grid.setStyle("-fx-border-color: #FF0000;");
		slidersGrill.setStyle("-fx-border-color: #00FF00;");
		trackButtonsContainer.setStyle("-fx-border-color: #0000FF;");
		
		Slider volA = (Slider) slidersGrill.getChildren().get(0);
		volA.valueProperty().addListener((o, oldVal, newVal) -> {
			if(controlsA != null) controlsA.setVolume(newVal.doubleValue());
		});

		Slider eqLowA = (Slider) slidersGrill.getChildren().get(1);
		eqLowA.valueProperty().addListener((o, oldVal, newVal) -> {
			if(controlsA != null) controlsA.setEqLow(newVal.doubleValue());
		});
		Slider eqMidA = (Slider) slidersGrill.getChildren().get(2);
		eqMidA.valueProperty().addListener((o, oldVal, newVal) -> {
			if(controlsA != null) controlsA.setEqMid(newVal.doubleValue());
		});
		Slider eqHighA = (Slider) slidersGrill.getChildren().get(3);
		eqHighA.valueProperty().addListener((o, oldVal, newVal) -> {
			if(controlsA != null) controlsA.setEqHigh(newVal.doubleValue());
		});

		Slider panA = (Slider) slidersGrill.getChildren().get(4);
		panA.valueProperty().addListener((o, oldVal, newVal) -> {
			if(controlsA != null) controlsA.setPan(newVal.doubleValue());
		});

		Slider pitchA = (Slider) slidersGrill.getChildren().get(5);
		pitchA.valueProperty().addListener((o, oldVal, newVal) -> {
			if(controlsA != null) controlsA.setPitch(newVal.doubleValue());
		});

		Button keyLockButton = (Button) slidersGrill.getChildren().get(6);
		keyLockButton.setOnAction(e -> {
			if(controlsA != null) controlsA.toggleKeyLock();
		});

		if (slidersGrill.getChildren().size() > 7) {
			Slider crossSlider = (Slider) slidersGrill.getChildren().get(7);
			crossSlider.valueProperty().addListener((o, oldVal, newVal) -> {
				if(crossfader != null) crossfader.crossfade(newVal.doubleValue());
			});
		}
		if (s != null) {
			s.valueProperty().addListener((o, oldVal, newVal) -> {
				// Calcola in che punto della canzone siamo rispetto al massimo dello slider
				if (deckA != null && deckA.getAudioData() != null) {
					double percentage = newVal.doubleValue() / s.getMax();
					double targetSample = percentage * deckA.getAudioData().length;
					
					// Calcolo del delta e utilizzo di seekTrack
					double delta = targetSample - deckA.getPlayheadDouble();
					deckA.seekTrack(delta);
				}
			});
		}
	}
}