package org.simconsole.simconsole;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class HelloController {
	@FXML
	private Label welcomeText;

	@FXML
	protected void onHelloButtonClick() {
		welcomeText.setText("Welcome to JavaFX Application!");
	}

	@FXML private StackPane graphContainer;
	@FXML private Canvas graph;
	@FXML private ListView songList;
	@FXML private StackPane songListContainer;
	@FXML private GridPane grid;
	@FXML private Circle vinyl;

	public void initialize(){
		//songList.prefHeightProperty().bind(songListContainer.heightProperty());
		songList.prefWidthProperty().bind(songListContainer.widthProperty());
		graph.heightProperty().bind(graphContainer.heightProperty());
		graph.widthProperty().bind(graphContainer.widthProperty());

		graphContainer.heightProperty().addListener((o,old,n) -> {
			graph.resize(graphContainer.getWidth(),n.doubleValue());
		});
		graphContainer.widthProperty().addListener((o,old,n) -> {
			graph.resize(n.doubleValue(),graphContainer.getHeight());
			System.out.println(graphContainer.getHeight()+":"+n.doubleValue());
		});
		grid.heightProperty().addListener((o,n,j)->{
			if (grid.getWidth() > j.doubleValue()){
				vinyl.setRadius(j.doubleValue()/8);
			}
		});
		grid.widthProperty().addListener((o,n,j)->{
			if (grid.getHeight() > j.doubleValue()){
				vinyl.setRadius(j.doubleValue()/8);
			}
		});
//		graphContainer.widthProperty().addListener((a,b,c)->{
//			System.out.println("cambiooo");
//		});

		//graph.widthProperty().bind(graphContainer.widthProperty());
		//graph.heightProperty().bind(graphContainer.heightProperty());
		//graphContainer.widthProperty().addListener((o,old,n)-> graph.resize(n.doubleValue(), graph.getHeight()));
		//graphContainer.heightProperty().addListener((o,old,n)-> graph.resize( graph.getWidth(), n.doubleValue()));
		//graph.widthProperty().addListener((o,oldV,newV)-> redraw());
		//graph.heightProperty().addListener((o,oldV,newV)-> redraw());

		graph.widthProperty().addListener((o, oldV, newV) -> redraw());
		graph.heightProperty().addListener((o, oldV, newV) -> redraw());
		graph.widthProperty().addListener((o,old,n)-> System.out.println("graph W: "+old+" -> "+n));
		graph.heightProperty().addListener((o,old,n)-> System.out.println("graph H: "+old+" -> "+n));
		graphContainer.widthProperty().addListener((o,old,n)-> System.out.println("cont W: "+old+" -> "+n));
		graphContainer.heightProperty().addListener((o,old,n)-> System.out.println("cont H: "+old+" -> "+n));
		//drawPlaceholder(graph);
		//Platform.runLater(this::redraw);

	}

	@FXML
	private void redraw() {
		GraphicsContext gc = graph.getGraphicsContext2D();
		//gc.setTransform(new javafx.scene.transform.Affine()); // reset trasform
		gc.clearRect(0,0,graph.getWidth(), graph.getHeight());
		// disegno in base a myCanvas.getWidth()/getHeight()
		drawPlaceholder(graph);
	}

	private void drawPlaceholder(Canvas canvas) {
		double w = Math.max(1, canvas.getWidth());
		double h = Math.max(1, canvas.getHeight());
		GraphicsContext gc = canvas.getGraphicsContext2D();

		// migliorare performance: riusa oggetti locali
		gc.clearRect(0, 0, w, h);

		// colori
		Color bg = Color.web("#f5f5f5");
		Color border = Color.web("#cccccc");
		Color cross = Color.web("#dddddd");
		Color textColor = Color.web("#888888");

		// sfondo (riempie sempre l'intera canvas)
		gc.setFill(bg);
		gc.fillRect(0, 0, w, h);

		// spessori relativi
		double minDim = Math.min(w, h);
		double borderWidth = Math.max(1.0, minDim * 0.005);
		double crossWidth = Math.max(1.0, minDim * 0.003);

		// bordo con offset per lineWidth
		gc.setStroke(border);
		gc.setLineWidth(borderWidth);
		double halfStroke = borderWidth / 2.0;
		gc.strokeRect(halfStroke, halfStroke, Math.max(0, w - borderWidth), Math.max(0, h - borderWidth));

		// croce diagonale usando margine relativo (10%)
		double margin = Math.min(w, h) * 0.05;
		gc.setStroke(cross);
		gc.setLineWidth(crossWidth);
		gc.strokeLine(margin, margin, w - margin, h - margin);
		gc.strokeLine(margin, h - margin, w - margin, margin);

		// testo centrale: scala il font rispetto alla dimensione minore della canvas
		String text = "Canvas placeholder";
		double fontSize = Math.max(10, minDim * 0.07); // dimensione reattiva
		gc.setFill(textColor);
		gc.setFont(Font.font("System", FontWeight.NORMAL, fontSize));

		// misurazione testo (usa Text per ottenere width/height)
		Text meas = new Text(text);
		meas.setFont(gc.getFont());
		double textWidth = meas.getLayoutBounds().getWidth();
		double textHeight = meas.getLayoutBounds().getHeight();

		// posizionamento centrato (y coordinate: baseline adjustment)
		double x = (w - textWidth) / 2.0;
		double y = (h + textHeight / 2.0) / 2.0;
		gc.fillText(text, x, y);
	}

}
