package org.simconsole.simconsole.components;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.transform.Rotate;
import java.util.Objects;
public class CDJDeckSkin extends SkinBase<DynamicDeck> {
    private final ImageView vinylView;
    private final Rotate rotateTransform;
    private double oldMouseAngle;
    private final javafx.animation.PauseTransition scrollFallbackTimer = new javafx.animation.PauseTransition(javafx.util.Duration.millis(80));
    public CDJDeckSkin(DynamicDeck deck) {
        super(deck);
        scrollFallbackTimer.setOnFinished(e -> getSkinnable().setScrubbing(false));
        Image vinylImage = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/org/simconsole/simconsole/image/vinyl_platter.png")
                )
        );
        vinylView = new ImageView(vinylImage);
        vinylView.setPreserveRatio(true);
        vinylView.setSmooth(true);
        rotateTransform = new Rotate(0);
        rotateTransform.angleProperty().bind(deck.rotationAngleProperty());
        vinylView.getTransforms().add(rotateTransform);
        getChildren().add(vinylView);
        deck.setOnScrollStarted(this::onScrollStarted);
        deck.setOnScroll(this::onScroll);
        deck.setOnScrollFinished(this::onScrollFinished);
        deck.setOnMousePressed(this::onMousePressed);
        deck.setOnMouseReleased(this::onMouseReleased);
        deck.setOnMouseDragged(this::onMouseDragged);
        deck.setOnTouchPressed(this::onTouchPressed);
        deck.setOnTouchReleased(this::onTouchReleased);
        deck.setOnTouchMoved(this::onTouchMoved);
    }
    private void onScrollStarted(ScrollEvent e) {
        getSkinnable().setScrubbing(true);
    }
    private void onScrollFinished(ScrollEvent e) {
        scrollFallbackTimer.stop();
        getSkinnable().setScrubbing(false);
    }
    private void onScroll(ScrollEvent e) {
        if (!getSkinnable().isScrubbing()) {
            getSkinnable().setScrubbing(true);
        }
        scrollFallbackTimer.playFromStart();
        getSkinnable().setRotationAngle(getSkinnable().getRotationAngle() + e.getDeltaY() * 0.3);
    }
    private void onMousePressed(MouseEvent e) {
        getSkinnable().setScrubbing(true);
        oldMouseAngle = mouseAngle(e.getX(), e.getY());
    }
    private void onMouseReleased(MouseEvent e) {
        getSkinnable().setScrubbing(false);
    }
    private void onMouseDragged(MouseEvent e) {
        double angle = mouseAngle(e.getX(), e.getY());
        double delta = angle - oldMouseAngle;
        if (delta >  180) delta -= 360;
        if (delta < -180) delta += 360;
        getSkinnable().setRotationAngle(getSkinnable().getRotationAngle() + delta);
        oldMouseAngle = angle;
    }
    private void onTouchPressed(TouchEvent e) {
        getSkinnable().setScrubbing(true);
        oldMouseAngle = mouseAngle(e.getTouchPoint().getX(), e.getTouchPoint().getY());
    }
    private void onTouchReleased(TouchEvent e) {
        getSkinnable().setScrubbing(false);
    }
    private void onTouchMoved(TouchEvent e) {
        double angle = mouseAngle(e.getTouchPoint().getX(), e.getTouchPoint().getY());
        double delta = angle - oldMouseAngle;
        if (delta >  180) delta -= 360;
        if (delta < -180) delta += 360;
        getSkinnable().setRotationAngle(getSkinnable().getRotationAngle() + delta);
        oldMouseAngle = angle;
    }
    private double mouseAngle(double mx, double my) {
        double cx = getSkinnable().getWidth()  / 2.0;
        double cy = getSkinnable().getHeight() / 2.0;
        return Math.toDegrees(Math.atan2(my - cy, mx - cx));
    }
    @Override protected double computeMinWidth(double h, double t, double r, double b, double l)  { return 50; }
    @Override protected double computeMinHeight(double w, double t, double r, double b, double l) { return 50; }
    @Override protected double computePrefWidth(double h, double t, double r, double b, double l)  { return 250; }
    @Override protected double computePrefHeight(double w, double t, double r, double b, double l) { return 250; }
    @Override protected double computeMaxWidth(double h, double t, double r, double b, double l)  { return Double.MAX_VALUE; }
    @Override protected double computeMaxHeight(double w, double t, double r, double b, double l) { return Double.MAX_VALUE; }
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double size = Math.min(w, h);
        double cx   = x + w / 2.0;
        double cy   = y + h / 2.0;
        vinylView.setFitWidth(size);
        vinylView.setFitHeight(size);
        vinylView.setLayoutX(cx - size / 2.0);
        vinylView.setLayoutY(cy - size / 2.0);
        rotateTransform.setPivotX(size / 2.0);
        rotateTransform.setPivotY(size / 2.0);
    }
}
