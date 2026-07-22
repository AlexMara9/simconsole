package org.simconsole.simconsole.controllers;
import org.simconsole.simconsole.models.TrackList;
import org.simconsole.simconsole.models.MusicApiService;
import org.simconsole.simconsole.models.Deck;
import org.simconsole.simconsole.models.Tracks;
import javafx.fxml.FXML;
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
 * Controller for managing track search, playlist display, local file imports, and loading tracks to decks.
 */
public class PlaylistController {
	@FXML private TextField searchField;
	@FXML private Button searchButton;
	@FXML private Button clearSearchButton;
	@FXML private javafx.scene.control.ProgressIndicator searchProgress;
	@FXML private ListView<Tracks> searchList;
	@FXML private javafx.scene.layout.VBox placeholderBox;
	@FXML private javafx.scene.image.ImageView placeholderImage;
	@FXML private Button localButton;
	@FXML private TableView<Tracks> playlistTable;
	@FXML private TableColumn<Tracks, String> colTitle;
	@FXML private TableColumn<Tracks, String> colDuration;
	@FXML private TableColumn<Tracks, String> colState;
	@FXML private TableColumn<Tracks, Tracks> colLeft;
	@FXML private TableColumn<Tracks, Tracks> colRight;
	private TrackList trackList = new TrackList("Main Playlist");
	private java.util.Set<String> addedTrackUrls = new java.util.HashSet<>();
	private Deck leftDeck;
	private Deck rightDeck;
	@FXML
	public void initialize(){
		initPlaylistUI();
	}
	private void initPlaylistUI() {
		if (placeholderBox != null && searchList != null) {
			placeholderBox.visibleProperty().bind(javafx.beans.binding.Bindings.isEmpty(searchList.getItems()));
			placeholderBox.managedProperty().bind(placeholderBox.visibleProperty());
		}
		if (placeholderImage != null && searchList != null) {
			placeholderImage.fitWidthProperty().bind(searchList.widthProperty().multiply(0.4));
			placeholderImage.fitHeightProperty().bind(searchList.heightProperty().multiply(0.4));
		}
		searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal) {
				searchField.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-border-color: #007aff; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 25 4 8; -fx-effect: dropshadow(three-pass-box, rgba(0,122,255,0.6), 8, 0, 0, 0);");
			} else {
				searchField.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-border-color: #555555; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 25 4 8; -fx-effect: none;");
			}
		});
		searchField.textProperty().addListener((obs, oldVal, newVal) -> {
			clearSearchButton.setVisible(newVal != null && !newVal.isEmpty());
		});
		clearSearchButton.setOnAction(e -> {
			searchField.clear();
			searchField.requestFocus();
			searchList.getItems().clear();
		});
		localButton.hoverProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal) {
				localButton.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-family: 'Segoe UI', 'Inter', sans-serif; -fx-font-weight: bold; -fx-border-color: #007aff; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 15; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,122,255,0.6), 8, 0, 0, 0);");
			} else {
				localButton.setStyle("-fx-background-color: #222222; -fx-text-fill: white; -fx-font-family: 'Segoe UI', 'Inter', sans-serif; -fx-font-weight: bold; -fx-border-color: #555555; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 15; -fx-cursor: hand; -fx-effect: none;");
			}
		});
		colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
		colDuration.setCellValueFactory(data -> {
			long ms = data.getValue().getDurationMs();
			long sec = ms / 1000;
			return new SimpleStringProperty(String.format("%02d:%02d", sec / 60, sec % 60));
		});
		colState.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getState().toString()));
		colState.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
			private javafx.animation.Timeline blinker;
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (blinker != null) { blinker.stop(); blinker = null; }
				setStyle("");
				setOpacity(1.0);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item);
					if ("DOWNLOADING".equals(item)) {
						blinker = new javafx.animation.Timeline(
								new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, new javafx.animation.KeyValue(opacityProperty(), 1.0)),
								new javafx.animation.KeyFrame(javafx.util.Duration.millis(500), new javafx.animation.KeyValue(opacityProperty(), 0.3))
						);
						blinker.setAutoReverse(true);
						blinker.setCycleCount(javafx.animation.Animation.INDEFINITE);
						blinker.play();
						setStyle("-fx-text-fill: #ffaa00; -fx-font-weight: bold;");
					} else if ("FAILED".equals(item)) {
						setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
					} else if ("READY".equals(item) || "LOCAL".equals(item)) {
						setStyle("-fx-text-fill: #90ee90;");
					}
				}
			}
		});
		colLeft.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
		colLeft.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
			private final Button btn = new Button("L");
			{
				btn.getStyleClass().add("deck-button");
				btn.setMaxWidth(Double.MAX_VALUE);
				btn.setMaxHeight(Double.MAX_VALUE);
				btn.setOnAction(e -> {
					Tracks t = getItem();
					if (t != null && (t.getState() == Tracks.TrackState.READY || t.getState() == Tracks.TrackState.LOCAL)) {
						if (leftDeck != null) {
							javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
							glow.setColor(javafx.scene.paint.Color.web("#007aff"));
							btn.setEffect(glow);
							btn.setStyle("-fx-border-color: #007aff;");
							javafx.animation.Timeline pulse = new javafx.animation.Timeline(
									new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, new javafx.animation.KeyValue(glow.radiusProperty(), 5), new javafx.animation.KeyValue(glow.spreadProperty(), 0.2)),
									new javafx.animation.KeyFrame(javafx.util.Duration.millis(300), new javafx.animation.KeyValue(glow.radiusProperty(), 20), new javafx.animation.KeyValue(glow.spreadProperty(), 0.6))
							);
							pulse.setAutoReverse(true);
							pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
							pulse.play();
							btn.setDisable(true);
							CompletableFuture.runAsync(() -> {
								leftDeck.loadTrack(t);
								Platform.runLater(() -> {
									pulse.stop();
									btn.setEffect(null);
									btn.setStyle("");
									btn.setDisable(false);
									playlistTable.refresh();
								});
							});
						}
					}
				});
			}
			@Override
			protected void updateItem(Tracks item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					if (!btn.getStyleClass().contains("deck-button")) {
						btn.getStyleClass().add("deck-button");
					}
					if (leftDeck != null && leftDeck.getCurrentTrack() == item) {
						if (!btn.getStyleClass().contains("deck-button-active")) {
							btn.getStyleClass().add("deck-button-active");
						}
					} else {
						btn.getStyleClass().remove("deck-button-active");
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
				btn.getStyleClass().add("deck-button");
				btn.setMaxWidth(Double.MAX_VALUE);
				btn.setMaxHeight(Double.MAX_VALUE);
				btn.setOnAction(e -> {
					Tracks t = getItem();
					if (t != null && (t.getState() == Tracks.TrackState.READY || t.getState() == Tracks.TrackState.LOCAL)) {
						if (rightDeck != null) {
							javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
							glow.setColor(javafx.scene.paint.Color.web("#007aff"));
							btn.setEffect(glow);
							btn.setStyle("-fx-border-color: #007aff;");
							javafx.animation.Timeline pulse = new javafx.animation.Timeline(
									new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, new javafx.animation.KeyValue(glow.radiusProperty(), 5), new javafx.animation.KeyValue(glow.spreadProperty(), 0.2)),
									new javafx.animation.KeyFrame(javafx.util.Duration.millis(300), new javafx.animation.KeyValue(glow.radiusProperty(), 20), new javafx.animation.KeyValue(glow.spreadProperty(), 0.6))
							);
							pulse.setAutoReverse(true);
							pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
							pulse.play();
							btn.setDisable(true);
							CompletableFuture.runAsync(() -> {
								rightDeck.loadTrack(t);
								Platform.runLater(() -> {
									pulse.stop();
									btn.setEffect(null);
									btn.setStyle("");
									btn.setDisable(false);
									playlistTable.refresh();
								});
							});
						}
					}
				});
			}
			@Override
			protected void updateItem(Tracks item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					if (!btn.getStyleClass().contains("deck-button")) {
						btn.getStyleClass().add("deck-button");
					}
					if (rightDeck != null && rightDeck.getCurrentTrack() == item) {
						if (!btn.getStyleClass().contains("deck-button-active")) {
							btn.getStyleClass().add("deck-button-active");
						}
					} else {
						btn.getStyleClass().remove("deck-button-active");
					}
					setGraphic(btn);
					setAlignment(javafx.geometry.Pos.CENTER);
				}
			}
		});
		colTitle.prefWidthProperty().bind(playlistTable.widthProperty().multiply(0.42));
		colDuration.prefWidthProperty().bind(playlistTable.widthProperty().multiply(0.15));
		colState.prefWidthProperty().bind(playlistTable.widthProperty().multiply(0.17));
		colLeft.prefWidthProperty().bind(playlistTable.widthProperty().multiply(0.12));
		colRight.prefWidthProperty().bind(playlistTable.widthProperty().multiply(0.12));
		playlistTable.styleProperty().bind(
				javafx.beans.binding.Bindings.createStringBinding(() ->
								String.format(java.util.Locale.US, "-fx-font-size: %.2fpx;", playlistTable.getWidth() / 32.0),
						playlistTable.widthProperty()
				)
		);
		playlistTable.setItems(trackList.getObservableTracks());
		javafx.scene.control.Label placeholder = new javafx.scene.control.Label("add a song");
		placeholder.setStyle("-fx-text-fill: #a0a0a0; -fx-font-size: 1em;");
		playlistTable.setPlaceholder(placeholder);
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
		searchList.setCellFactory(lv -> new javafx.scene.control.ListCell<Tracks>() {
			{
				prefWidthProperty().bind(lv.widthProperty().subtract(20));
				hoverProperty().addListener((obs, oldVal, newVal) -> {
					Tracks item = getItem();
					if (item != null) {
						boolean added = item.getPreviewUrl() != null && addedTrackUrls.contains(item.getPreviewUrl());
						if (!added) {
							setText(newVal ? "+ " + item.toString() : item.toString());
							setCursor(newVal ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);
						} else {
							setCursor(javafx.scene.Cursor.DEFAULT);
						}
					}
				});
			}
			@Override
			protected void updateItem(Tracks item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setStyle("");
					setCursor(javafx.scene.Cursor.DEFAULT);
				} else {
					boolean added = item.getPreviewUrl() != null && addedTrackUrls.contains(item.getPreviewUrl());
					if (added) {
						setText(item.toString());
						setStyle("-fx-background-color: #007aff; -fx-text-fill: white;");
						setCursor(javafx.scene.Cursor.DEFAULT);
					} else {
						setText(isHover() ? "+ " + item.toString() : item.toString());
						setStyle("");
						setCursor(isHover() ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);
					}
					setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
				}
			}
		});
		searchList.setOnMouseClicked(e -> {
			if (e.getClickCount() == 1) {
				Tracks selected = searchList.getSelectionModel().getSelectedItem();
				if (selected != null && selected.getPreviewUrl() != null) {
					if (!addedTrackUrls.contains(selected.getPreviewUrl())) {
						addedTrackUrls.add(selected.getPreviewUrl());
						addTrackFromApi(selected);
						searchList.refresh();
					}
				}
			}
		});
		playlistTable.setOnMouseClicked(e -> {
			Tracks selected = playlistTable.getSelectionModel().getSelectedItem();
			if (selected != null && (selected.getState() == Tracks.TrackState.READY || selected.getState() == Tracks.TrackState.LOCAL)) {
				if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY && e.getClickCount() == 2) {
					if (leftDeck != null) leftDeck.loadTrack(selected);
				} else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
					if (rightDeck != null) rightDeck.loadTrack(selected);
				}
			}
		});
		localButton.setOnAction(e -> {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3"));
			File file = fc.showOpenDialog(playlistTable.getScene().getWindow());
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
	}
}
