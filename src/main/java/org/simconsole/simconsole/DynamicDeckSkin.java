package org.simconsole.simconsole;


import javafx.scene.control.SkinBase;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;

public class DynamicDeckSkin extends SkinBase<DynamicDeck> {

    private final Circle vinyl;
    private final Circle vinylCenter;

    private double oldMouseAngle;

    public DynamicDeckSkin(DynamicDeck deck) {
        super(deck);

        vinyl = new Circle();
        vinyl.getStyleClass().add("vinyl");
        vinyl.setStroke(Color.web("#111111"));
        vinyl.setStrokeType(StrokeType.INSIDE);

        vinylCenter = new Circle();
        vinylCenter.setFill(Color.web("#1a1a1a"));
        vinylCenter.setStroke(Color.web("#007aff"));
        vinylCenter.setStrokeType(StrokeType.INSIDE);

        getChildren().addAll(vinyl, vinylCenter);

        // Binding visivo: il nodo ruota in base alla proprietà del componente
        vinyl.rotateProperty().bind(deck.rotationAngleProperty());

        // Listener interattivi: modificano la proprietà invece di manipolare il nodo
        deck.setOnScroll(this::onScroll);
        deck.setOnMousePressed(this::onMousePressed);
        deck.setOnMouseDragged(this::onMouseDragged);
        deck.setOnTouchPressed(this::onTouchPressed);
        deck.setOnTouchMoved(this::onTouchMoved);
    }

    private void onScroll(ScrollEvent event) {
        double delta = event.getDeltaY();
        getSkinnable().setRotationAngle(getSkinnable().getRotationAngle() + delta * 0.3);
    }

    private void onMousePressed(MouseEvent event) {
        oldMouseAngle = mouseAngle(event.getX(), event.getY());
    }

    private void onMouseDragged(MouseEvent event) {
        double angle = mouseAngle(event.getX(), event.getY());
        double delta = angle - oldMouseAngle;
        
        // Evita salti di angolo quando si attraversa il limite di atan2 (-180 / +180)
        if (delta > 180) delta -= 360;
        else if (delta < -180) delta += 360;

        getSkinnable().setRotationAngle(getSkinnable().getRotationAngle() + delta);
        oldMouseAngle = angle;
    }

    private void onTouchPressed(TouchEvent event) {
        oldMouseAngle = mouseAngle(event.getTouchPoint().getX(), event.getTouchPoint().getY());
    }

    private void onTouchMoved(TouchEvent event) {
        double angle = mouseAngle(event.getTouchPoint().getX(), event.getTouchPoint().getY());
        double delta = angle - oldMouseAngle;
        
        if (delta > 180) delta -= 360;
        else if (delta < -180) delta += 360;

        getSkinnable().setRotationAngle(getSkinnable().getRotationAngle() + delta);
        oldMouseAngle = angle;
    }

    private double mouseAngle(double x, double y) {
        // x e y sono relative all'area del componente
        double centerX = getSkinnable().getWidth() / 2.0;
        double centerY = getSkinnable().getHeight() / 2.0;
        return Math.toDegrees(Math.atan2(y - centerY, x - centerX));
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        // Responsive Layout basato sul minor lato disponibile
        double minDim = Math.min(w, h);

        double vinylRadius = Math.max(1, minDim * 0.45);
        double centerRadius = Math.max(1, vinylRadius * 0.2);
        double strokeWidth = Math.max(0, vinylRadius * 0.04);

        double centerX = x + w / 2.0;
        double centerY = y + h / 2.0;

        // Impostiamo il raggio e centriamo i cerchi
        vinyl.setRadius(vinylRadius);
        vinyl.setCenterX(centerX);
        vinyl.setCenterY(centerY);

        vinylCenter.setRadius(centerRadius);
        vinylCenter.setStrokeWidth(strokeWidth);
        vinylCenter.setCenterX(centerX);
        vinylCenter.setCenterY(centerY);
    }
}
