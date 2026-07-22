package org.simconsole.simconsole.controllers;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Controller for rendering the signal graph visualization canvas responsive to window resizing.
 */
public class SignalGraphController {
    @FXML private StackPane signalGraphContainer;
    @FXML private Canvas signalGraph;
    @FXML
    public void initialize(){
        responsive();
    }
    private void responsive(){
        signalGraph.widthProperty().bind(signalGraphContainer.widthProperty());
        signalGraph.heightProperty().bind(signalGraphContainer.heightProperty());
        signalGraph.widthProperty().addListener((val, oldWidth, newWidth) -> drawPlaceholder());
        signalGraph.heightProperty().addListener((val, oldHeight, newHeight) -> drawPlaceholder());
    }
    private void drawPlaceholder() {
        double w = Math.max(1, signalGraph.getWidth());
        double h = Math.max(1, signalGraph.getHeight());
        GraphicsContext gc = signalGraph.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        Color bg = Color.web("#1e1e1e");
        Color border = Color.web("#2b2b2b");
        Color cross = Color.web("#333333");
        Color textColor = Color.web("#a0a0a0");
        gc.setFill(bg);
        gc.fillRect(0, 0, w, h);
        double minDim = Math.min(w, h);
        double borderWidth = Math.max(1.0, minDim * 0.005);
        double crossWidth = Math.max(1.0, minDim * 0.003);
        gc.setStroke(border);
        gc.setLineWidth(borderWidth);
        double halfStroke = borderWidth / 2.0;
        gc.strokeRect(halfStroke, halfStroke, Math.max(0, w - borderWidth), Math.max(0, h - borderWidth));
        double margin = Math.min(w, h) * 0.05;
        gc.setStroke(cross);
        gc.setLineWidth(crossWidth);
        gc.strokeLine(margin, margin, w - margin, h - margin);
        gc.strokeLine(margin, h - margin, w - margin, margin);
        String text = "Signal Visualizer";
        double fontSize = Math.max(10, minDim * 0.07);
        gc.setFill(textColor);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
        Text meas = new Text(text);
        meas.setFont(gc.getFont());
        double textWidth = meas.getLayoutBounds().getWidth();
        double textHeight = meas.getLayoutBounds().getHeight();
        double x = (w - textWidth) / 2.0;
        double y = (h + textHeight / 2.0) / 2.0;
        gc.fillText(text, x, y);
    }
}
