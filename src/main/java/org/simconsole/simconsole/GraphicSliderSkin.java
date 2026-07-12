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
        
        // Position texts
        songNameText.setTextOrigin(VPos.TOP);
        songNameText.setLayoutX(10);
        songNameText.setLayoutY(10);

        startTimeText.setTextOrigin(VPos.BOTTOM);
        startTimeText.setLayoutX(10);
        startTimeText.setLayoutY(h - 5);

        endTimeText.setTextOrigin(VPos.BOTTOM);
        endTimeText.setLayoutY(h - 5);
        endTimeText.setLayoutX(w - endTimeText.getLayoutBounds().getWidth() - 10);

        playhead.setStartY(0);
        playhead.setEndY(h);

        drawWaveform();
        updatePlayhead();
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

        // Mono color as requested by user (CDJ style blue)
        gc.setFill(Color.web("#007aff")); 

        double barWidth = w / data.length;
        for (int i = 0; i < data.length; i++) {
            double barHeight = data[i] * h;
            double x = i * barWidth;
            double y = (h - barHeight) / 2; // Center vertically
            gc.fillRect(x, y, Math.max(1, barWidth - 0.5), barHeight);
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
