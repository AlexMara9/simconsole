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
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayList;
import java.util.List;

public class CDJDeckSkin extends SkinBase<DynamicDeck> {

    // ─── Gruppi principali ───────────────────────────────────────────────────
    private final Group rotatingBase;       // outerRim + skirt + mainPlatter
    private final Group rotatingBumps;      // fossette circolari sulla skirt
    private final Group fixedLightingBumps; // wrapper fisso per InnerShadow sulle fossette
    private final Group rotatingDisplay;    // bezel + righe radiali + hub

    // ─── Cerchi base ─────────────────────────────────────────────────────────
    private final Circle outerRim;          // bordo esterno
    private final Circle rimHighlightRing;  // anello highlight chiaro appena dentro il bordo
    private final Circle skirt;             // zona fossette
    private final Circle mainPlatter;       // piatto rialzato centrale

    // ─── Fossette sulla skirt (bumps circolari) ───────────────────────────────
    private static final int NUM_BUMPS = 24;
    private final List<Circle> bumpList = new ArrayList<>();

    // ─── Display centrale ────────────────────────────────────────────────────
    private final Circle displayBezel;
    private final Circle innerDisplay;

    // Righe radiali fitte per la texture "vinile concavo"
    private static final int NUM_STROBE = 240;   // molto piu' fitte = vinile vero
    private final List<Line> strobeTicks = new ArrayList<>();

    // Anelli concentrici sul display (simulano le solcature del vinile)
    private final Circle grooveRingOuter;
    private final Circle grooveRingInner;

    private final Circle centerHub;

    // ─── Overlay fissi ───────────────────────────────────────────────────────
    private final Arc outerRimGlow;    // riflesso chiaro sul bordo esterno (fisso)

    // ─── Effetti ──────────────────────────────────────────────────────────────────────────
    private final DropShadow platterShadow;
    private final InnerShadow platterInner;
    private final DropShadow rimShadow;
    private final InnerShadow bumpShadow;
    private final InnerShadow bumpHighlight;
    private final DropShadow bezelShadow;
    private final InnerShadow displayCavity;
    private final InnerShadow displayCavity2;  // secondo layer concavita'
    private final DropShadow markerGlow;        // glow rosso sui marker

    private double oldMouseAngle;

    // ─────────────────────────────────────────────────────────────────────────

    public CDJDeckSkin(DynamicDeck deck) {
        super(deck);

        rotatingBase    = new Group();
        rotatingBumps   = new Group();
        rotatingDisplay = new Group();

        // ── Outer Rim ────────────────────────────────────────────────────────
        // Sfumatura radiale: bordo grigio chiaro → quasi nero verso il centro
        outerRim = new Circle();
        outerRim.setFill(new RadialGradient(
                0, 0,          // focusAngle, focusDist
                0.5, 0.5, 0.5, // centerX, centerY, radius (proporzioni)
                true,          // proportional
                CycleMethod.NO_CYCLE,
                new Stop(0.0,  Color.web("#5a5a5a")),
                new Stop(0.70, Color.web("#2c2c2c")),
                new Stop(1.0,  Color.web("#111111"))
        ));
        rimShadow = new DropShadow(BlurType.GAUSSIAN, Color.web("#00000088"), 12, 0.2, 0, 3);
        outerRim.setEffect(rimShadow);

        // Anello highlight sottile (simula il bordo lucido rialzato)
        rimHighlightRing = new Circle();
        rimHighlightRing.setFill(Color.TRANSPARENT);
        rimHighlightRing.setStroke(Color.web("#6a6a6aaa"));

        // ── Skirt (zona fossette) ─────────────────────────────────────────────
        skirt = new Circle();
        skirt.setFill(new RadialGradient(
                0, 0, 0.5, 0.5, 0.5,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#282828")),
                new Stop(1.0, Color.web("#111111"))
        ));

        // ── Main Platter (rialzato) ───────────────────────────────────────────
        mainPlatter = new Circle();
        mainPlatter.setFill(new RadialGradient(
                0, 0, 0.46, 0.43, 0.5, // focus leggermente in alto-sinistra
                true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#3a3a3a")),
                new Stop(0.6, Color.web("#252525")),
                new Stop(1.0, Color.web("#181818"))
        ));
        // Ombra esterna per il rialzo 3D
        platterShadow = new DropShadow(BlurType.GAUSSIAN, Color.web("#000000cc"), 20, 0.1, 0, 7);
        // Inner shadow per il bordo concavo tra skirt e piatto
        platterInner  = new InnerShadow(BlurType.GAUSSIAN, Color.web("#00000099"), 10, 0, 0, -4);
        platterShadow.setInput(platterInner);
        mainPlatter.setEffect(platterShadow);

        rotatingBase.getChildren().addAll(outerRim, rimHighlightRing, skirt, mainPlatter);

        // ── Fossette (bumps circolari sulla skirt) ────────────────────────────
        for (int i = 0; i < NUM_BUMPS; i++) {
            Circle bump = new Circle();
            // Le fossette sono scure con bordo leggermente più chiaro
            bump.setFill(new RadialGradient(
                    0, 0, 0.4, 0.35, 0.5,
                    true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#1a1a1a")),
                    new Stop(0.6, Color.web("#111111")),
                    new Stop(1.0, Color.web("#0a0a0a"))
            ));
            bumpList.add(bump);
            rotatingBumps.getChildren().add(bump);
        }

        // InnerShadow applicato al gruppo fisso (la direzione luce non ruota)
        bumpHighlight = new InnerShadow(BlurType.GAUSSIAN, Color.web("#ffffff1a"), 2, 0, 0, -1);
        bumpShadow    = new InnerShadow(BlurType.GAUSSIAN, Color.web("#000000cc"), 5, 0, 0, 3);
        bumpShadow.setInput(bumpHighlight);

        fixedLightingBumps = new Group(rotatingBumps);
        fixedLightingBumps.setEffect(bumpShadow);

        // ── Display Bezel ─────────────────────────────────────────────────────
        displayBezel = new Circle();
        displayBezel.setFill(new RadialGradient(
                0, 0, 0.5, 0.5, 0.5,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#3c3c3c")),
                new Stop(1.0, Color.web("#0d0d0d"))
        ));
        bezelShadow = new DropShadow(BlurType.GAUSSIAN, Color.web("#000000bb"), 12, 0.1, 0, 4);
        displayBezel.setEffect(bezelShadow);

        // ── Inner Display (concavo con righe radiali) ─────────────────────
        innerDisplay = new Circle();
        // Gradiente leggermente asimmetrico: il vinile riflette luce in alto a sinistra
        innerDisplay.setFill(new RadialGradient(
                -25, 0.35,
                0.46, 0.43, 0.5,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#282828")),
                new Stop(0.30, Color.web("#181818")),
                new Stop(0.65, Color.web("#0e0e0e")),
                new Stop(1.00, Color.web("#060606"))
        ));
        // Due livelli di InnerShadow incatenati → cavita' piu' profonda
        displayCavity2 = new InnerShadow(BlurType.GAUSSIAN, Color.web("#000000cc"), 6, 0.3, 0, 5);
        displayCavity  = new InnerShadow(BlurType.GAUSSIAN, Color.web("#00000088"), 18, 0, 0, 2);
        displayCavity.setInput(displayCavity2);
        innerDisplay.setEffect(displayCavity);

        // Anelli concentrici "solcature vinile"
        grooveRingOuter = new Circle();
        grooveRingOuter.setFill(Color.TRANSPARENT);
        grooveRingOuter.setStroke(Color.web("#00000060"));

        grooveRingInner = new Circle();
        grooveRingInner.setFill(Color.TRANSPARENT);
        grooveRingInner.setStroke(Color.web("#00000055"));

        // Righe radiali (240 solchi, 3 livelli opacita' per variazione naturale)
        markerGlow = new DropShadow(BlurType.GAUSSIAN, Color.web("#ff0000cc"), 6, 0.5, 0, 0);
        for (int i = 0; i < NUM_STROBE; i++) {
            Line tick = new Line();
            tick.setStrokeLineCap(StrokeLineCap.BUTT);
            if (i == 0 || i == 1) {
                tick.setStroke(Color.web("#ff2222ff"));
                tick.setStrokeWidth(2.0);
                tick.setEffect(markerGlow);
            } else {
                int mod = i % 3;
                if (mod == 0) {
                    tick.setStroke(Color.web("#44444488"));
                } else if (mod == 1) {
                    tick.setStroke(Color.web("#33333366"));
                } else {
                    tick.setStroke(Color.web("#2a2a2a55"));
                }
            }
            strobeTicks.add(tick);
            rotatingDisplay.getChildren().add(tick);
        }

        // ── Center Hub ────────────────────────────────────────────────────────
        centerHub = new Circle();
        centerHub.setFill(new RadialGradient(
                0, 0, 0.42, 0.38, 0.5,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#2e2e2e")),
                new Stop(1.0, Color.web("#0a0a0a"))
        ));
        DropShadow hubShadow = new DropShadow(BlurType.GAUSSIAN, Color.web("#000000cc"), 8, 0.15, 0, 2);
        centerHub.setEffect(hubShadow);

        rotatingDisplay.getChildren().addAll(0, List.of(displayBezel, innerDisplay, grooveRingOuter, grooveRingInner));
        rotatingDisplay.getChildren().add(centerHub);

        // ── Highlight Arc sul bordo (fisso, non ruota) ────────────────────────
        // Simula il riflesso ambientale sul bordo metallico superiore-sinistro
        outerRimGlow = new Arc();
        outerRimGlow.setType(ArcType.OPEN);
        outerRimGlow.setStartAngle(100);
        outerRimGlow.setLength(130);
        outerRimGlow.setFill(Color.TRANSPARENT);
        outerRimGlow.setStroke(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#ffffff00")),
                new Stop(0.4, Color.web("#ffffff33")),
                new Stop(1.0, Color.web("#ffffff00"))
        ));
        outerRimGlow.setMouseTransparent(true);

        // ── Montaggio finale ──────────────────────────────────────────────────
        getChildren().addAll(rotatingBase, fixedLightingBumps, rotatingDisplay, outerRimGlow);

        // ── Binding rotazione con pivot fisso (0,0) ───────────────────────────
        javafx.scene.transform.Rotate rotBase = new javafx.scene.transform.Rotate(0, 0, 0);
        rotBase.angleProperty().bind(deck.rotationAngleProperty());
        rotatingBase.getTransforms().add(rotBase);

        javafx.scene.transform.Rotate rotBumps = new javafx.scene.transform.Rotate(0, 0, 0);
        rotBumps.angleProperty().bind(deck.rotationAngleProperty());
        rotatingBumps.getTransforms().add(rotBumps);

        javafx.scene.transform.Rotate rotDisplay = new javafx.scene.transform.Rotate(0, 0, 0);
        rotDisplay.angleProperty().bind(deck.rotationAngleProperty());
        rotatingDisplay.getTransforms().add(rotDisplay);

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
        double minDim = Math.min(w, h);
        double R = Math.max(1, minDim * 0.48);

        // Raggi principali
        double outerRimR   = R;
        double skirtR      = R * 0.965;  // quasi al bordo → zona bumps più ampia
        double platterR    = R * 0.775;  // piatto leggermente più piccolo

        // Fossette sulla skirt
        // La zona skirt va da platterR (0.775) a skirtR (0.965) → larghezza = 0.19 * R
        // Il bump deve riempire quasi tutta quella larghezza
        double bumpOrbit   = (skirtR + platterR) / 2.0;  // centro geometrico della corona
        double bumpRadius  = (skirtR - platterR) * 0.46; // ~88% della semi-larghezza → quasi a filo

        // Display centrale
        double bezelR        = R * 0.36;
        double innerDisplayR = R * 0.325;

        // Tick radiali
        double tickOuterR  = R * 0.275;
        double tickInnerR  = R * 0.130;
        double tickWidth   = Math.max(0.5, R * 0.010);

        // Hub
        double hubR        = R * 0.110;

        // Highlight arc sul rim
        double rimGlowR    = outerRimR - R * 0.015;

        double cx = x + w / 2.0;
        double cy = y + h / 2.0;

        // ── Aggiorna effetti dinamicamente ────────────────────────────────────
        platterShadow.setRadius(Math.max(1, R * 0.08));
        platterShadow.setOffsetY(R * 0.028);
        platterInner.setRadius(Math.max(1, R * 0.04));

        rimShadow.setRadius(Math.max(1, R * 0.05));

        bumpShadow.setRadius(Math.max(1, R * 0.025));
        bumpShadow.setOffsetY(R * 0.012);
        bumpHighlight.setRadius(Math.max(1, R * 0.010));

        bezelShadow.setRadius(Math.max(1, R * 0.05));
        bezelShadow.setOffsetY(R * 0.016);

        displayCavity.setRadius(Math.max(1, R * 0.072));
        displayCavity.setOffsetY(R * 0.008);
        displayCavity2.setRadius(Math.max(1, R * 0.024));
        displayCavity2.setOffsetY(R * 0.020);

        markerGlow.setRadius(Math.max(2, R * 0.028));

        // ── Pivot globale ─────────────────────────────────────────────────────
        rotatingBase.setLayoutX(cx);         rotatingBase.setLayoutY(cy);
        fixedLightingBumps.setLayoutX(cx);   fixedLightingBumps.setLayoutY(cy);
        rotatingBumps.setLayoutX(0);         rotatingBumps.setLayoutY(0);
        rotatingDisplay.setLayoutX(cx);      rotatingDisplay.setLayoutY(cy);
        outerRimGlow.setLayoutX(cx);         outerRimGlow.setLayoutY(cy);

        // ── Cerchi base ───────────────────────────────────────────────────────
        outerRim.setRadius(outerRimR);
        rimHighlightRing.setRadius(outerRimR - R * 0.005);
        rimHighlightRing.setStrokeWidth(Math.max(0.5, R * 0.008));
        skirt.setRadius(skirtR);
        mainPlatter.setRadius(platterR);

        // ── Fossette ──────────────────────────────────────────────────────────
        double angleStepBumps = 360.0 / NUM_BUMPS;
        for (int i = 0; i < NUM_BUMPS; i++) {
            Circle bump = bumpList.get(i);
            double angleRad = Math.toRadians(i * angleStepBumps);
            bump.setCenterX(bumpOrbit * Math.cos(angleRad));
            bump.setCenterY(bumpOrbit * Math.sin(angleRad));
            bump.setRadius(bumpRadius);
        }

        // ── Display ───────────────────────────────────────────────────────────
        displayBezel.setRadius(bezelR);
        innerDisplay.setRadius(innerDisplayR);

        // Anelli concentrici solcature
        grooveRingOuter.setRadius(innerDisplayR * 0.82);
        grooveRingOuter.setStrokeWidth(Math.max(0.5, R * 0.005));
        grooveRingInner.setRadius(innerDisplayR * 0.60);
        grooveRingInner.setStrokeWidth(Math.max(0.5, R * 0.004));

        // ── Tick radiali ──────────────────────────────────────────────────────────────────────────
        double angleStepTicks = 360.0 / NUM_STROBE;
        for (int i = 0; i < NUM_STROBE; i++) {
            Line tick = strobeTicks.get(i);
            double angleRad = Math.toRadians(i * angleStepTicks);
            if (i == 0 || i == 1) {
                // Marker rossi: piu' lunghi e sporgenti verso l'esterno
                tick.setStartX(tickInnerR * 0.80 * Math.cos(angleRad));
                tick.setStartY(tickInnerR * 0.80 * Math.sin(angleRad));
                tick.setEndX((tickOuterR + R * 0.045) * Math.cos(angleRad));
                tick.setEndY((tickOuterR + R * 0.045) * Math.sin(angleRad));
                tick.setStrokeWidth(Math.max(1.5, R * 0.009));
            } else {
                // Ogni 5 solchi uno leggermente piu' lungo
                double innerScale = (i % 5 == 0) ? 0.90 : 1.0;
                tick.setStartX(tickInnerR * innerScale * Math.cos(angleRad));
                tick.setStartY(tickInnerR * innerScale * Math.sin(angleRad));
                tick.setEndX(tickOuterR * Math.cos(angleRad));
                tick.setEndY(tickOuterR * Math.sin(angleRad));
                tick.setStrokeWidth(tickWidth);
            }
        }

        // ── Hub ───────────────────────────────────────────────────────────────
        centerHub.setRadius(hubR);

        // ── Highlight arc ─────────────────────────────────────────────────────
        outerRimGlow.setRadiusX(rimGlowR);
        outerRimGlow.setRadiusY(rimGlowR);
        outerRimGlow.setStrokeWidth(Math.max(1, R * 0.018));
    }
}
