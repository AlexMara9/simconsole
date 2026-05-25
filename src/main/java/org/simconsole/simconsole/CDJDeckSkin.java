package org.simconsole.simconsole;

import javafx.scene.Group;

import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayList;
import java.util.List;

public class CDJDeckSkin extends SkinBase<DynamicDeck> {

    // Gruppi principali separati per gestire l'illuminazione fissa sugli oggetti rotanti
    private final Group rotatingBase;
    private final Group rotatingNotches;
    private final Group fixedLightingNotches;
    private final Group rotatingDisplay;

    private final Circle outerRim;
    private final Circle notchesSkirt;
    private final Circle mainPlatter;

    private final List<Line> notchesList = new ArrayList<>();
    private final List<Line> miniDashesList = new ArrayList<>();

    private final Circle displayBezel;
    private final Circle innerDisplay;
    private final List<Line> strobeTicksList = new ArrayList<>();
    private final Circle centerHub;

    private final Arc glassReflection;
    
    // --- Effetti Ombra Dinamici ---
    private final DropShadow platterShadow;
    private final InnerShadow cavityShadow;
    private final InnerShadow cavityHighlight;
    private final DropShadow bezelShadow;

    private double oldMouseAngle;

    private static final int NUM_NOTCHES = 36;
    private static final int NUM_TICKS = 72;

    public CDJDeckSkin(DynamicDeck deck) {
        super(deck);

        rotatingBase = new Group();
        rotatingNotches = new Group();
        rotatingDisplay = new Group();

        // --- LAYER 1: Piatto Base (Rim e Skirt) ---
        outerRim = new Circle();
        Stop[] rimStops = new Stop[] { new Stop(0, Color.web("#555555")), new Stop(1, Color.web("#050505")) };
        outerRim.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, rimStops));

        notchesSkirt = new Circle();
        notchesSkirt.setFill(Color.web("#1c1c1c")); // Colore del fondo delle fossette

        // Il piatto principale (rialzato)
        mainPlatter = new Circle();
        mainPlatter.setFill(Color.web("#242424"));
        // Ombra per far sembrare il piatto rialzato rispetto alla gonna con le fossette
        platterShadow = new DropShadow(BlurType.GAUSSIAN, Color.web("#000000cc"), 15, 0, 0, 5);
        mainPlatter.setEffect(platterShadow);

        rotatingBase.getChildren().addAll(outerRim, notchesSkirt, mainPlatter);

        // --- LAYER 2: Notches & Mini Dashes ---
        for (int i = 0; i < NUM_NOTCHES; i++) {
            Line notch = new Line();
            notch.setStroke(Color.web("#161616")); // Le cavità sono più scure
            notch.setStrokeLineCap(StrokeLineCap.ROUND);
            notchesList.add(notch);
            rotatingNotches.getChildren().add(notch);

            for (int j = 1; j <= 3; j++) {
                Line dash = new Line();
                dash.setStroke(Color.web("#2e2e2e")); // I puntini in rilievo sono più chiari
                dash.setStrokeLineCap(StrokeLineCap.ROUND);
                miniDashesList.add(dash);
                rotatingNotches.getChildren().add(dash);
            }
        }

        // TRUCCO ARCHITETTURALE: Applichiamo l'InnerShadow a un contenitore FISSO.
        // I pixel dei notches ruoteranno al suo interno, ma l'angolo di luce della shadow
        // resterà sempre fisso dall'alto verso il basso (simulando una luce ambientale fissa).
        cavityShadow = new InnerShadow(BlurType.GAUSSIAN, Color.web("#000000ee"), 8, 0, 0, 4);
        cavityHighlight = new InnerShadow(BlurType.GAUSSIAN, Color.web("#ffffff33"), 3, 0, 0, -2);
        cavityShadow.setInput(cavityHighlight);

        fixedLightingNotches = new Group(rotatingNotches);
        fixedLightingNotches.setEffect(cavityShadow);

        // --- LAYER 3: Display Centrale ---
        displayBezel = new Circle();
        Stop[] bezelStops = new Stop[] { new Stop(0, Color.web("#444444")), new Stop(1, Color.web("#050505")) };
        displayBezel.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, bezelStops));
        bezelShadow = new DropShadow(BlurType.GAUSSIAN, Color.web("#000000aa"), 10, 0, 0, 3);
        displayBezel.setEffect(bezelShadow);

        innerDisplay = new Circle();
        innerDisplay.setFill(Color.web("#080808"));

        for (int i = 0; i < NUM_TICKS; i++) {
            Line tick = new Line();
            tick.setStrokeLineCap(StrokeLineCap.BUTT);
            // I primi due tick sono rossi neon
            if (i == 0 || i == 1) {
                tick.setStroke(Color.web("#ff1a1a"));
            } else {
                tick.setStroke(Color.web("#333333"));
            }
            strobeTicksList.add(tick);
            rotatingDisplay.getChildren().add(tick);
        }

        centerHub = new Circle();
        centerHub.setFill(Color.web("#121212"));

        rotatingDisplay.getChildren().addAll(0, List.of(displayBezel, innerDisplay));
        rotatingDisplay.getChildren().add(centerHub);

        // --- LAYER 4: Riflesso sul vetro (Fisso) ---
        glassReflection = new Arc();
        glassReflection.setType(ArcType.OPEN);
        glassReflection.setStartAngle(0);
        glassReflection.setLength(180); // Copre solo la metà superiore
        Stop[] glassStops = new Stop[] { new Stop(0, Color.web("#ffffff22")), new Stop(1, Color.web("#ffffff00")) };
        glassReflection.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, glassStops));
        glassReflection.setMouseTransparent(true);

        // Montaggio Finale
        getChildren().addAll(rotatingBase, fixedLightingNotches, rotatingDisplay, glassReflection);

        // --- BINDING ROTAZIONE CON PIVOT FISSO (Risolve il bug dell'orbita/wobbling) ---
        // Se usassimo node.rotateProperty(), JavaFX calcolerebbe il pivot basandosi sul Bounding Box.
        // Poiché i DropShadow rendono il bounding box asimmetrico, il gruppo orbiterebbe invece di ruotare sul posto.
        // Usando una trasformazione esplicita, forziamo il pivot esattamente a (0,0) in coordinate locali!
        javafx.scene.transform.Rotate rotBase = new javafx.scene.transform.Rotate(0, 0, 0);
        rotBase.angleProperty().bind(deck.rotationAngleProperty());
        rotatingBase.getTransforms().add(rotBase);

        javafx.scene.transform.Rotate rotNotches = new javafx.scene.transform.Rotate(0, 0, 0);
        rotNotches.angleProperty().bind(deck.rotationAngleProperty());
        rotatingNotches.getTransforms().add(rotNotches);

        javafx.scene.transform.Rotate rotDisplay = new javafx.scene.transform.Rotate(0, 0, 0);
        rotDisplay.angleProperty().bind(deck.rotationAngleProperty());
        rotatingDisplay.getTransforms().add(rotDisplay);

        // Eventi Interazione
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
        double centerX = getSkinnable().getWidth() / 2.0;
        double centerY = getSkinnable().getHeight() / 2.0;
        return Math.toDegrees(Math.atan2(y - centerY, x - centerX));
    }

    // --- SIZING RESPONSIVO (Impedisce al GridPane di farsi influenzare dalle ombre/rotazioni) ---
    @Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) { return 50; }
    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) { return 50; }
    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) { return 250; }
    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) { return 250; }
    @Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) { return Double.MAX_VALUE; }
    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) { return Double.MAX_VALUE; }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double minDim = Math.min(w, h);
        double R = Math.max(1, minDim * 0.48); // Raggio globale base

        double outerRimR = R;
        double skirtR = R * 0.98;
        double platterR = R * 0.81;

        double notchOuterR = R * 0.88;//0.95
        double notchInnerR = R * 0.92;//0.84
        double notchWidth = Math.max(2, R * 0.1);

        double miniDashOuterR = R * 0.92;
        double miniDashInnerR = R * 0.87;
        double miniDashWidth = Math.max(1, R * 0.008);

        double displayBezelR = R * 0.38;
        double innerDisplayR = R * 0.35;

        double tickOuterR = R * 0.28;
        double tickInnerR = R * 0.16;
        double tickWidth = Math.max(1, R * 0.012);

        double centerHubR = R * 0.14;

        double centerX = x + w / 2.0;
        double centerY = y + h / 2.0;

        // --- AGGIORNAMENTO DINAMICO OMBRE (100% RESPONSIVE) ---
        platterShadow.setRadius(Math.max(1, R * 0.06));
        platterShadow.setOffsetY(R * 0.02);

        cavityShadow.setRadius(Math.max(1, R * 0.03));
        cavityShadow.setOffsetY(R * 0.015);
        
        cavityHighlight.setRadius(Math.max(1, R * 0.01));
        cavityHighlight.setOffsetY(-R * 0.008);

        bezelShadow.setRadius(Math.max(1, R * 0.04));
        bezelShadow.setOffsetY(R * 0.012);

        // Impostiamo il pivot point globale per tutti i layer
        rotatingBase.setLayoutX(centerX);
        rotatingBase.setLayoutY(centerY);
        
        fixedLightingNotches.setLayoutX(centerX);
        fixedLightingNotches.setLayoutY(centerY);
        rotatingNotches.setLayoutX(0);
        rotatingNotches.setLayoutY(0);

        rotatingDisplay.setLayoutX(centerX);
        rotatingDisplay.setLayoutY(centerY);

        glassReflection.setLayoutX(centerX);
        glassReflection.setLayoutY(centerY);

        // Assegnazione raggi
        outerRim.setRadius(outerRimR);
        notchesSkirt.setRadius(skirtR);
        mainPlatter.setRadius(platterR);

        // Layout Notches e Dashes
        double angleStepNotches = 360.0 / NUM_NOTCHES;
        int dashIndex = 0;
        for (int i = 0; i < NUM_NOTCHES; i++) {
            Line notch = notchesList.get(i);
            double angleDeg = i * angleStepNotches;
            double angleRad = Math.toRadians(angleDeg);

            notch.setStartX(notchInnerR * Math.cos(angleRad));
            notch.setStartY(notchInnerR * Math.sin(angleRad));
            notch.setEndX(notchOuterR * Math.cos(angleRad));
            notch.setEndY(notchOuterR * Math.sin(angleRad));
            notch.setStrokeWidth(notchWidth);

            for (int j = 1; j <= 3; j++) {
                Line dash = miniDashesList.get(dashIndex++);
                double dashAngleDeg = angleDeg + j * (angleStepNotches / 4.0);
                double dashAngleRad = Math.toRadians(dashAngleDeg);

                dash.setStartX(miniDashInnerR * Math.cos(dashAngleRad));
                dash.setStartY(miniDashInnerR * Math.sin(dashAngleRad));
                dash.setEndX(miniDashOuterR * Math.cos(dashAngleRad));
                dash.setEndY(miniDashOuterR * Math.sin(dashAngleRad));
                dash.setStrokeWidth(miniDashWidth);
            }
        }

        displayBezel.setRadius(displayBezelR);
        innerDisplay.setRadius(innerDisplayR);

        // Layout Ticks
        double angleStepTicks = 360.0 / NUM_TICKS;
        for (int i = 0; i < NUM_TICKS; i++) {
            Line tick = strobeTicksList.get(i);
            double angleDeg = i * angleStepTicks;
            double angleRad = Math.toRadians(angleDeg);

            tick.setStartX(tickInnerR * Math.cos(angleRad));
            tick.setStartY(tickInnerR * Math.sin(angleRad));
            tick.setEndX(tickOuterR * Math.cos(angleRad));
            tick.setEndY(tickOuterR * Math.sin(angleRad));
            tick.setStrokeWidth(tickWidth);
        }

        centerHub.setRadius(centerHubR);

        // Layout Reflection Arc
        glassReflection.setRadiusX(displayBezelR);
        glassReflection.setRadiusY(displayBezelR);
    }
}
