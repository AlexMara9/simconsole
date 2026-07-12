package org.simconsole.simconsole;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.Random;

/**
 * Skin for GraphicSlider. Handles Canvas rendering of the waveform
 * and mouse interactivity for seeking.
 */
public class GraphicSliderSkin extends SkinBase<GraphicSlider> {

    private final Pane container = new Pane();
    private final Canvas canvas = new Canvas();
    private final Line playhead = new Line();
    
    private final Text songNameText = new Text();
    private final Text startTimeText = new Text("0:00");
    private final Text endTimeText = new Text();

    private final java.util.Map<String, javafx.scene.Group> cueNodes = new java.util.HashMap<>();
    private float[] dummyWaveform;

    public GraphicSliderSkin(GraphicSlider control) {
        super(control);

        generateDummyWaveform();

        // Style classes
        container.getStyleClass().add("graphic-slider-container");
        playhead.getStyleClass().add("graphic-slider-playhead");
        songNameText.getStyleClass().add("graphic-slider-title");
        startTimeText.getStyleClass().add("graphic-slider-time");
        endTimeText.getStyleClass().add("graphic-slider-time");

        // Bind text
        songNameText.textProperty().bind(control.songNameProperty());
        
        container.getChildren().addAll(canvas, playhead, songNameText, startTimeText, endTimeText);
        getChildren().add(container);

        // Responsive resizing
        container.layoutBoundsProperty().addListener((obs, oldB, newB) -> {
            updateLayout(newB.getWidth(), newB.getHeight());
        });

        // Properties bindings
        control.totalTimeProperty().addListener((obs, old, val) -> updateTimeLabels());
        control.currentTimeProperty().addListener((obs, old, val) -> updatePlayhead());
        control.waveformProperty().addListener((obs, old, val) -> drawWaveform());

        updateTimeLabels();
        
        // Interactivity
        container.setOnMousePressed(e -> seekTo(e.getX()));
        container.setOnMouseDragged(e -> seekTo(e.getX()));

        // Cue Points listener
        control.getCuePoints().addListener((javafx.collections.MapChangeListener<String, GraphicSlider.CuePoint>) change -> {
            if (change.wasRemoved()) {
                javafx.scene.Group oldNode = cueNodes.remove(change.getKey());
                if (oldNode != null) {
                    container.getChildren().remove(oldNode);
                }
            }
            if (change.wasAdded()) {
                addCueNode(change.getValueAdded());
            }
        });

        // Setup cues that were added before the skin was initialized
        for (GraphicSlider.CuePoint cue : control.getCuePoints().values()) {
            addCueNode(cue);
        }
    }

    private void addCueNode(GraphicSlider.CuePoint cue) {
        javafx.scene.Group group = new javafx.scene.Group();
        
        Line line = new Line();
        line.setStroke(cue.color);
        line.getStyleClass().add("graphic-slider-cue");
        
        javafx.scene.shape.Polygon topTriangle = new javafx.scene.shape.Polygon();
        topTriangle.setFill(cue.color);
        
        javafx.scene.shape.Polygon bottomTriangle = new javafx.scene.shape.Polygon();
        bottomTriangle.setFill(cue.color);
        
        group.getChildren().addAll(line, topTriangle, bottomTriangle);
        
        // Add to container just after canvas (index 1) so it's under playhead
        container.getChildren().add(1, group);
        cueNodes.put(cue.id, group);
        
        positionCueNode(group, cue.position);
    }

    private void generateDummyWaveform() {
        Random rand = new Random();
        dummyWaveform = new float[500]; // 500 data points for a smooth dummy waveform
        for (int i = 0; i < dummyWaveform.length; i++) {
            dummyWaveform[i] = 0.1f + rand.nextFloat() * 0.8f;
        }
    }

    private void updateLayout(double w, double h) {
        canvas.setWidth(w);
        canvas.setHeight(h);
        
        double fontSize = Math.min(h * 0.25, w * 0.08);
        if (fontSize > 0) {
            String fontStyle = String.format(java.util.Locale.US, "-fx-font-size: %.1fpx;", fontSize);
            songNameText.setStyle(fontStyle);
            startTimeText.setStyle(fontStyle);
            endTimeText.setStyle(fontStyle);
            
            songNameText.applyCss();
            startTimeText.applyCss();
            endTimeText.applyCss();
        }

        double marginX = w * 0.01;
        double marginY = h * 0.05;
        
        // Position texts
        songNameText.setTextOrigin(VPos.TOP);
        songNameText.setLayoutX(marginX);
        songNameText.setLayoutY(marginY);

        startTimeText.setTextOrigin(VPos.BOTTOM);
        startTimeText.setLayoutX(marginX);
        startTimeText.setLayoutY(h - marginY);

        endTimeText.setTextOrigin(VPos.BOTTOM);
        endTimeText.setLayoutY(h - marginY);
        endTimeText.setLayoutX(w - endTimeText.getLayoutBounds().getWidth() - marginX);

        // Responsive playhead
        double playheadWidth = Math.max(1, w * 0.003);
        playhead.setStyle(String.format(java.util.Locale.US, "-fx-stroke-width: %.1fpx;", playheadWidth));
        playhead.setStartY(0);
        playhead.setEndY(h);

        drawWaveform();
        updatePlayhead();
        
        // Update all cue lines
        for (java.util.Map.Entry<String, javafx.scene.Group> entry : cueNodes.entrySet()) {
            GraphicSlider.CuePoint cue = getSkinnable().getCuePoints().get(entry.getKey());
            if (cue != null) {
                positionCueNode(entry.getValue(), cue.position);
            }
        }
    }

    private void positionCueNode(javafx.scene.Group group, double positionSeconds) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;
        
        GraphicSlider control = getSkinnable();
        double total = control.getTotalTime();
        if (total <= 0) return;
        
        double progress = positionSeconds / total;
        double x = progress * w;
        
        Line line = (Line) group.getChildren().get(0);
        javafx.scene.shape.Polygon topTriangle = (javafx.scene.shape.Polygon) group.getChildren().get(1);
        javafx.scene.shape.Polygon bottomTriangle = (javafx.scene.shape.Polygon) group.getChildren().get(2);
        
        line.setStartX(x);
        line.setEndX(x);
        line.setStartY(0);
        line.setEndY(h);
        
        // Make cue lines thinner than playhead
        double cueWidth = Math.max(1, w * 0.0015);
        line.setStyle(String.format(java.util.Locale.US, "-fx-stroke-width: %.1fpx;", cueWidth));
        
        // Triangle logic
        double triWidth = Math.max(6, Math.min(15, w * 0.015));
        double halfTri = triWidth / 2.0;
        double triHeight = triWidth;
        
        // Top triangle (pointing down)
        topTriangle.getPoints().setAll(
            x - halfTri, 0.0,
            x + halfTri, 0.0,
            x, triHeight
        );
        
        // Bottom triangle (pointing up)
        bottomTriangle.getPoints().setAll(
            x - halfTri, h,
            x + halfTri, h,
            x, h - triHeight
        );
    }

    private void drawWaveform() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        GraphicSlider control = getSkinnable();
        float[] data = control.getWaveform();
        if (data == null) {
            data = dummyWaveform; // Fallback to mockup data
        }

        gc.setStroke(Color.web("#007aff"));
        
        double barWidth = w / data.length;
        double strokeWidth = Math.max(0.5, barWidth * 0.8);
        gc.setLineWidth(strokeWidth);

        for (int i = 0; i < data.length; i++) {
            double barHeight = data[i] * h * 0.8; // 80% max height
            double x = i * barWidth;
            double y1 = (h - barHeight) / 2; 
            double y2 = y1 + barHeight;
            gc.strokeLine(x, y1, x, y2);
        }
    }

    private void updateTimeLabels() {
        double totalSeconds = getSkinnable().getTotalTime();
        endTimeText.setText(formatTime(totalSeconds));
        if (canvas.getWidth() > 0) {
            endTimeText.setLayoutX(canvas.getWidth() - endTimeText.getLayoutBounds().getWidth() - 10);
        }
    }

    private void updatePlayhead() {
        GraphicSlider control = getSkinnable();
        double total = control.getTotalTime();
        if (total <= 0) return;
        
        double current = control.getCurrentTime();
        double progress = current / total;
        
        double x = progress * canvas.getWidth();
        playhead.setStartX(x);
        playhead.setEndX(x);
    }

    private void seekTo(double x) {
        double w = canvas.getWidth();
        if (w <= 0) return;
        
        double progress = x / w;
        progress = Math.max(0, Math.min(1, progress));
        
        GraphicSlider control = getSkinnable();
        control.setCurrentTime(progress * control.getTotalTime());
    }

    private String formatTime(double totalSeconds) {
        int m = (int) (totalSeconds / 60);
        int s = (int) (totalSeconds % 60);
        return String.format("%d:%02d", m, s);
    }
}
