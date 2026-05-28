package org.simconsole.simconsole;

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

    public CDJDeckSkin(DynamicDeck deck) {
        super(deck);

        // ── Carica l'immagine del vinile (PNG con alpha circolare) ───────────
        Image vinylImage = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/org/simconsole/simconsole/image/vinyl_platter.png")
                )
        );

        vinylView = new ImageView(vinylImage);
        vinylView.setPreserveRatio(true);
        vinylView.setSmooth(true);
        // Nessun clip necessario: il PNG è già ritagliato a cerchio con alpha trasparente

        // ── Rotazione applicata all'ImageView ────────────────────────────────
        rotateTransform = new Rotate(0);
        rotateTransform.angleProperty().bind(deck.rotationAngleProperty());
        vinylView.getTransforms().add(rotateTransform);

        getChildren().add(vinylView);

        // ── Interazione ───────────────────────────────────────────────────────
        deck.setOnScroll(this::onScroll);
        deck.setOnMousePressed(this::onMousePressed);
        deck.setOnMouseDragged(this::onMouseDragged);
        deck.setOnTouchPressed(this::onTouchPressed);
        deck.setOnTouchMoved(this::onTouchMoved);
    }

    // ─── Handlers ────────────────────────────────────────────────────────────

    private void onScroll(ScrollEvent e) {
        getSkinnable().setRotationAngle(getSkinnable().getRotationAngle() + e.getDeltaY() * 0.3);
    }

    private void onMousePressed(MouseEvent e)  { oldMouseAngle = mouseAngle(e.getX(), e.getY()); }

    private void onMouseDragged(MouseEvent e) {
        double angle = mouseAngle(e.getX(), e.getY());
        double delta = angle - oldMouseAngle;
        if (delta >  180) delta -= 360;
        if (delta < -180) delta += 360;
        getSkinnable().setRotationAngle(getSkinnable().getRotationAngle() + delta);
        oldMouseAngle = angle;
    }

    private void onTouchPressed(TouchEvent e)  { oldMouseAngle = mouseAngle(e.getTouchPoint().getX(), e.getTouchPoint().getY()); }

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

    // ─── Sizing responsivo ────────────────────────────────────────────────────
    @Override protected double computeMinWidth(double h, double t, double r, double b, double l)  { return 50; }
    @Override protected double computeMinHeight(double w, double t, double r, double b, double l) { return 50; }
    @Override protected double computePrefWidth(double h, double t, double r, double b, double l)  { return 250; }
    @Override protected double computePrefHeight(double w, double t, double r, double b, double l) { return 250; }
    @Override protected double computeMaxWidth(double h, double t, double r, double b, double l)  { return Double.MAX_VALUE; }
    @Override protected double computeMaxHeight(double w, double t, double r, double b, double l) { return Double.MAX_VALUE; }

    // ─── Layout responsivo ────────────────────────────────────────────────────
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double size = Math.min(w, h);
        double cx   = x + w / 2.0;
        double cy   = y + h / 2.0;

        // Dimensiona l'immagine
        vinylView.setFitWidth(size);
        vinylView.setFitHeight(size);

        // Posiziona centrata
        vinylView.setLayoutX(cx - size / 2.0);
        vinylView.setLayoutY(cy - size / 2.0);

        // Pivot di rotazione al centro dell'immagine
        rotateTransform.setPivotX(size / 2.0);
        rotateTransform.setPivotY(size / 2.0);
    }
}
