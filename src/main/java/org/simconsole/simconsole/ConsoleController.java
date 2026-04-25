package org.simconsole.simconsole;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

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
	@FXML private ListView songList;

	@FXML private StackPane slidersGrillContainer;
	@FXML private GridPane slidersGrill;

	@FXML private HBox trackButtonsContainer;
	@FXML private Button playButton;
	@FXML private Button skipButton;
	@FXML private Button unskipButton;

	private Deck deck;

	/**
	 * init function that sets the responsivity
	 */
	public void initialize(){
		initResponsiveness();
		initControls();

		//setDebug();
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
		Button[] arr = {playButton, skipButton, unskipButton};
		for (Button b : arr) {
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

	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	private void initControls(){
		playButton.setOnAction(e -> {
			if(!deck.isPlaying()){
				deck.getPlayer().play();
			}else {
				deck.pause();
			}
		});
	}
	private void setDebug(){
		grid.setStyle("-fx-border-color: #FF0000;");
		slidersGrill.setStyle("-fx-border-color: #00FF00;");
		trackButtonsContainer.setStyle("-fx-border-color: #0000FF;");
	}

}
