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

	// Injected from fx:include
	@FXML private EffectsController leftEffectsController;
	@FXML private EffectsController rightEffectsController;

	// Playlist & Search UI
	@FXML private TextField searchField;
	@FXML private Button searchButton;
	@FXML private javafx.scene.control.ProgressIndicator searchProgress;
	@FXML private ListView<Tracks> searchList;
	@FXML private Button localButton;
	@FXML private TableView<Tracks> playlistTable;
	@FXML private TableColumn<Tracks, String> colTitle;
	@FXML private TableColumn<Tracks, String> colDuration;
	@FXML private TableColumn<Tracks, String> colState;
	@FXML private TableColumn<Tracks, Tracks> colLeft;
	@FXML private TableColumn<Tracks, Tracks> colRight;

	private TrackList trackList = new TrackList("Main Playlist");
	
	private Deck leftDeck;
	private Deck rightDeck;

	/**
	 * init function
	 */
	@FXML
	public void initialize(){
		initResponsiveness();
		initPlaylistUI();
	}

	private void initPlaylistUI() {
		// Table Columns setup
		colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
		colDuration.setCellValueFactory(data -> {
			long ms = data.getValue().getDurationMs();
			long sec = ms / 1000;
			return new SimpleStringProperty(String.format("%02d:%02d", sec / 60, sec % 60));
		});
		colState.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getState().toString()));

		colLeft.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
		colLeft.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
			private final Button btn = new Button("L");
			{
				btn.setOnAction(e -> {
					Tracks t = getItem();
					if (t != null && (t.getState() == Tracks.TrackState.READY || t.getState() == Tracks.TrackState.LOCAL)) {
						if (leftDeck != null) leftDeck.loadTrack(t);
						playlistTable.refresh();
					}
				});
				btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #555; -fx-border-radius: 3; -fx-cursor: hand;");
			}
			@Override
			protected void updateItem(Tracks item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					if (leftDeck != null && leftDeck.getCurrentTrack() == item) {
						btn.setStyle("-fx-background-color: #007aff; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
					} else {
						btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #555; -fx-border-radius: 3; -fx-cursor: hand;");
					}
					setGraphic(btn);
					setAlignment(javafx.geometry.Pos.CENTER);
				}
			}
		});

		colRight.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
		colRight.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
			private final Button btn = new Button("R");
			{
				btn.setOnAction(e -> {
					Tracks t = getItem();
					if (t != null && (t.getState() == Tracks.TrackState.READY || t.getState() == Tracks.TrackState.LOCAL)) {
						if (rightDeck != null) rightDeck.loadTrack(t);
						playlistTable.refresh();
					}
				});
				btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #555; -fx-border-radius: 3; -fx-cursor: hand;");
			}
			@Override
			protected void updateItem(Tracks item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					if (rightDeck != null && rightDeck.getCurrentTrack() == item) {
						btn.setStyle("-fx-background-color: #007aff; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
					} else {
						btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #555; -fx-border-radius: 3; -fx-cursor: hand;");
					}
					setGraphic(btn);
					setAlignment(javafx.geometry.Pos.CENTER);
				}
			}
		});

		playlistTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		playlistTable.setItems(trackList.getObservableTracks());

		// Search functionality
		Runnable performSearch = () -> {
			String query = searchField.getText();
			if (query != null && !query.isBlank()) {
				searchButton.setVisible(false);
				searchButton.setManaged(false);
				searchProgress.setVisible(true);
				searchProgress.setManaged(true);
				CompletableFuture.runAsync(() -> {
					List<Tracks> results = MusicApiService.search(query, 10);
					Platform.runLater(() -> {
						searchList.getItems().setAll(results);
						searchProgress.setVisible(false);
						searchProgress.setManaged(false);
						searchButton.setVisible(true);
						searchButton.setManaged(true);
					});
				});
			}
		};

		searchField.setOnAction(e -> performSearch.run());
		searchButton.setOnAction(e -> performSearch.run());

		// Double-click to add track from search list to playlist
		searchList.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				Tracks selected = searchList.getSelectionModel().getSelectedItem();
				if (selected != null) {
					addTrackFromApi(selected);
				}
			}
		});

		// Load tracks to decks on click
		playlistTable.setOnMouseClicked(e -> {
			Tracks selected = playlistTable.getSelectionModel().getSelectedItem();
			if (selected != null && (selected.getState() == Tracks.TrackState.READY || selected.getState() == Tracks.TrackState.LOCAL)) {
				// We use double click for the left deck to allow normal single-click row selection, 
				// and single right-click for the right deck.
				if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY && e.getClickCount() == 2) {
					if (leftDeck != null) leftDeck.loadTrack(selected);
				} else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
					if (rightDeck != null) rightDeck.loadTrack(selected);
				}
			}
		});

		// Add local track button
		localButton.setOnAction(e -> {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3"));
			File file = fc.showOpenDialog(grid.getScene().getWindow());
			if (file != null) {
				Tracks t = new Tracks(file.getAbsolutePath());
				trackList.addTrack(t);
			}
		});
	}

	private void addTrackFromApi(Tracks track) {
		trackList.addTrack(track);
		
		CompletableFuture.runAsync(() -> {
			try {
				track.setState(Tracks.TrackState.DOWNLOADING);
				Platform.runLater(playlistTable::refresh);
				
				String localPath = MusicApiService.downloadTrack(track);
				track.setFilePath(localPath);
				track.setState(Tracks.TrackState.READY);
				
				Platform.runLater(playlistTable::refresh);
			} catch (Exception ex) {
				track.setState(Tracks.TrackState.FAILED);
				Platform.runLater(playlistTable::refresh);
				System.err.println("Failed to download track: " + ex.getMessage());
			}
		});
	}

	public void setDecks(Deck d1, Deck d2) {
		this.leftDeck = d1;
		this.rightDeck = d2;
		if (leftEffectsController != null && d1 != null) {
			leftEffectsController.setDeckControls(d1.getControls());
		}
		if (rightEffectsController != null && d2 != null) {
			rightEffectsController.setDeckControls(d2.getControls());
		}
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
