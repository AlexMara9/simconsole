package org.simconsole.simconsole;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;

/**
 * Controller that connects the UI to the app logic
 */
public class ConsoleController {
	@FXML private GridPane grid;

	@FXML private Circle vinyl;

	@FXML private StackPane graphContainer;
	@FXML private Canvas graph;

	@FXML private StackPane songListContainer;
	@FXML private Label songListLabel;
	@FXML private ListView<Tracks> songList;
	@FXML private Button addTrackButton;

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
		graph.widthProperty().addListener((o, oldV, newV) -> drawPlaceholder(graph));
		graph.heightProperty().addListener((o, oldV, newV) -> drawPlaceholder(graph));

		// song list
		songList.prefHeightProperty().bind(songListContainer.heightProperty().multiply(songListCellScaleFactor));
		songList.prefWidthProperty().bind(songListContainer.widthProperty());
		songListLabel.prefHeightProperty().bind(songListContainer.heightProperty().multiply(songListLabelHeightCellScaleFactor));
		DoubleBinding songListBoundSize = songListContainer.heightProperty().multiply(songListLabelFontCellScaleFactor);
		songListLabel.fontProperty().bind(
				Bindings.createObjectBinding(
						() -> {
							double size = Math.round(songListBoundSize.get());
							return Font.font(size);
						},
						songListBoundSize
				)
		);

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

	private void drawPlaceholder(Canvas canvas) {
		double w = Math.max(1, canvas.getWidth());
		double h = Math.max(1, canvas.getHeight());
		GraphicsContext gc = canvas.getGraphicsContext2D();

		gc.clearRect(0, 0, w, h);

		// colors
		Color bg = Color.web("#f5f5f5");
		Color border = Color.web("#cccccc");
		Color cross = Color.web("#dddddd");
		Color textColor = Color.web("#888888");

		// background
		gc.setFill(bg);
		gc.fillRect(0, 0, w, h);

		// relative thickness
		double minDim = Math.min(w, h);
		double borderWidth = Math.max(1.0, minDim * 0.005);
		double crossWidth = Math.max(1.0, minDim * 0.003);

		// border with offset for line width
		gc.setStroke(border);
		gc.setLineWidth(borderWidth);
		double halfStroke = borderWidth / 2.0;
		gc.strokeRect(halfStroke, halfStroke, Math.max(0, w - borderWidth), Math.max(0, h - borderWidth));

		// diagonal cross with relative margin (10%)
		double margin = Math.min(w, h) * 0.05;
		gc.setStroke(cross);
		gc.setLineWidth(crossWidth);
		gc.strokeLine(margin, margin, w - margin, h - margin);
		gc.strokeLine(margin, h - margin, w - margin, margin);

		// central text: scale the font compared to the dimension of the canvas
		String text = "Canvas placeholder";
		double fontSize = Math.max(10, minDim * 0.07); // responsive dimension
		gc.setFill(textColor);
		gc.setFont(Font.font("System", FontWeight.NORMAL, fontSize));

		Text meas = new Text(text);
		meas.setFont(gc.getFont());
		double textWidth = meas.getLayoutBounds().getWidth();
		double textHeight = meas.getLayoutBounds().getHeight();

		double x = (w - textWidth) / 2.0;
		double y = (h + textHeight / 2.0) / 2.0;
		gc.fillText(text, x, y);
	}

	private void initControls(){
		if (addTrackButton != null) {
			addTrackButton.setOnAction(e -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Seleziona traccia audio");
				fileChooser.getExtensionFilters().addAll(
						new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aiff", "*.flac", "*.ogg")
				);
				Window window = addTrackButton.getScene().getWindow();
				File selectedFile = fileChooser.showOpenDialog(window);
				if (selectedFile != null) {
					Tracks newTrack = new Tracks(selectedFile.getAbsolutePath());
					songList.getItems().add(newTrack);
				}
			});
		}

		if (playButtonA != null) {
			playButtonA.setOnAction(e -> {
				if(deckA != null ) {
					if(!deckA.isPlaying()){
						deckA.play();
					}else {
						deckA.pause();
					}
				}
			});
		}

		if (playButtonB != null) {
			playButtonB.setOnAction(e -> {
				if(deckB != null ) {
					if(!deckB.isPlaying()){
						deckB.play();
					}else {
						deckB.pause();
					}
				}
			});
		}
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