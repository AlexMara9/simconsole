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
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

/**
 * CDJDeckSkin — skin che replica il jog wheel Pioneer CDJ-3000.
 *
 * ─── Strategia "ombre fisse" ────────────────────────────────────────────────
 * In JavaFX gli effetti (InnerShadow, DropShadow) vengono calcolati nello
 * spazio locale del nodo, PRIMA che le trasformazioni vengano applicate.
 * Questo significa che un InnerShadow con offsetY=5 su un nodo che ruota di
 * 90° mostrerà l'ombra a DESTRA invece che in basso → l'ombra ruota.
 *
 * Soluzione: ogni gruppo rotante è avvolto in un Group FISSO che porta l'effetto.
 * L'effetto è calcolato sullo spazio schermo del gruppo fisso → non ruota mai.
 *
 * Schema:
 *   fixedRimShading   (fisso, ha rimDropShadow)
 *     └─ rotatingRimSkirt  (ruota: outerRim, skirt, grooveRing)
 *
 *   fixedPlatterShading (fisso, ha platterDropShadow + platterEdgeDark)
 *     └─ rotatingPlatter  (ruota: mainPlatter)
 *
 *   fixedBumpShading  (fisso, ha bumpShadow + bumpHighlight)
 *     └─ rotatingBumps    (ruota: 24 bumps)
 *
 *   fixedDisplayShading (fisso, ha displayCavityInner + displayCavityOuter)
 *     └─ rotatingDisplay  (ruota: bezel, chrome, innerDisplay, lines, hub)
 *
 *   Archi fissi overlay (non ruotano: riflessi ambientali top-left)
 */
public class CDJDeckSkin extends SkinBase<DynamicDeck> {

    // ── Gruppi rotanti (solo geometria, nessun effetto direzionale) ───────────
    private final Group rotatingRimSkirt;  // outerRim, skirt, grooveRing
    private final Group rotatingPlatter;   // mainPlatter
    private final Group rotatingBumps;     // 24 fossette
    private final Group rotatingDisplay;   // bezel + chrome + innerDisplay + linee + hub

    // ── Wrapper fissi (portano gli effetti in coordinate schermo) ─────────────
    private final Group fixedRimShading;      // Group(rotatingRimSkirt)
    private final Group fixedPlatterShading;  // Group(rotatingPlatter)
    private final Group fixedBumpShading;     // Group(rotatingBumps)
    private final Group fixedDisplayShading;  // Group(rotatingDisplay)

    // ── Forme ─────────────────────────────────────────────────────────────────
    private final Circle outerRim;
    private final Circle skirt;
    private final Circle grooveRing;
    private final Circle mainPlatter;

    private static final int NUM_BUMPS = 24;
    private final List<Circle> bumpList = new ArrayList<>();

    private final Circle displayBezel;
    private final Circle chromeRing;
    private final Circle innerDisplay;
    private final Circle hubBody;
    private final Circle hubSpecular;

    private static final int NUM_LINES = 12;
    private final List<Line> strobeLines = new ArrayList<>();
    private final Line redMarker;

    // ── Overlay illuminazione globale bumps (fisso, non ruota) ──────────────
    // Gradiente lineare top-left → bottom-right:
    // Bright a top-left (bumps che "guardano" la luce) → dark a bottom-right
    // Matematicamente: linear_value(bx, by) ∝ dot((bx,by)/r, (-1,-1)/√2)
    // che è esattamente il fattore di illuminazione lambertiana per ogni bump.
    private final Circle bumpLightingOverlay;

    // ── Overlay archi fissi ───────────────────────────────────────────────────────
    private final Arc rimHighlightArc;
    private final Arc rimShadowArc;
    private final Arc platterHighlightArc;
    private final Arc bezelChromeArc;

    // ── Effetti (aggiornati nel layout per essere responsivi) ─────────────────
    private final DropShadow rimDropShadow;
    private final DropShadow platterDropShadow;
    private final InnerShadow platterEdgeDark;
    private final InnerShadow bumpShadow;
    private final InnerShadow bumpHighlight;
    private final InnerShadow displayCavityInner;
    private final InnerShadow displayCavityOuter;
    private final DropShadow  markerGlow;

    private double oldMouseAngle;

    // ─────────────────────────────────────────────────────────────────────────

    public CDJDeckSkin(DynamicDeck deck) {
        super(deck);

        rotatingRimSkirt = new Group();
        rotatingPlatter  = new Group();
        rotatingBumps    = new Group();
        rotatingDisplay  = new Group();

        // ════════════════════════════════════════════════════════════════════
        // OUTER RIM — gradiente focus top-left, stop extra per bordo rialzato
        // ════════════════════════════════════════════════════════════════════
        outerRim = new Circle();
        outerRim.setFill(new RadialGradient(
                130, 0.20,
                0.50, 0.50, 0.50,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#484848")),
                new Stop(0.55, Color.web("#282828")),
                new Stop(0.88, Color.web("#1a1a1a")),
                new Stop(0.93, Color.web("#323232")),  // flash bordo rialzato
                new Stop(1.00, Color.web("#0e0e0e"))
        ));
        // NESSUN effetto sul rim: l'effetto va sul wrapper fisso
        rimDropShadow = new DropShadow(BlurType.GAUSSIAN, Color.web("#000000cc"), 14, 0.25, 0, 4);

        // SKIRT
        skirt = new Circle();
        skirt.setFill(new RadialGradient(
                0, 0, 0.50, 0.50, 0.50,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#202020")),
                new Stop(1.0, Color.web("#131313"))
        ));

        // GROOVE RING — anello scuro di separazione skirt/piatto
        grooveRing = new Circle();
        grooveRing.setFill(Color.web("#090909"));

        rotatingRimSkirt.getChildren().addAll(outerRim, skirt, grooveRing);

        // fixedRimShading: wrapper fisso → DropShadow sempre verso il basso
        fixedRimShading = new Group(rotatingRimSkirt);
        fixedRimShading.setEffect(rimDropShadow);

        // ════════════════════════════════════════════════════════════════════
        // MAIN PLATTER — rialzato sopra la skirt
        //   platterDropShadow: ombra proiettata sul groove ring (sempre in basso)
        //   platterEdgeDark: bordo inferiore-destro scuro (luce da top-left)
        //   Entrambi sul wrapper fisso → non ruotano
        // ════════════════════════════════════════════════════════════════════
        mainPlatter = new Circle();
        mainPlatter.setFill(new RadialGradient(
                130, 0.25,
                0.46, 0.42, 0.50,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#3e3e3e")),
                new Stop(0.30, Color.web("#313131")),
                new Stop(0.65, Color.web("#242424")),
                new Stop(0.90, Color.web("#1c1c1c")),
                new Stop(1.00, Color.web("#151515"))
        ));
        // NESSUN effetto sul mainPlatter: gli effetti vanno sul wrapper fisso
        platterEdgeDark   = new InnerShadow(BlurType.GAUSSIAN, Color.web("#000000bb"), 14, 0.0, 4, 8);
        platterDropShadow = new DropShadow(BlurType.GAUSSIAN, Color.web("#000000ee"), 20, 0.15, 0, 7);
        platterDropShadow.setInput(platterEdgeDark);  // chain: prima InnerShadow poi DropShadow

        rotatingPlatter.getChildren().add(mainPlatter);

        // fixedPlatterShading: wrapper fisso → ombra e bordo scuro sempre fissi
        fixedPlatterShading = new Group(rotatingPlatter);
        fixedPlatterShading.setEffect(platterDropShadow);

        // ════════════════════════════════════════════════════════════════════
        // BUMPS — fossette sferiche concave sulla skirt
        //   Fill: centro scuro (profondo), bordi più chiari (pareti fossetta)
        //   fixedBumpShading già garantiva la direzione fissa → invariato
        // ════════════════════════════════════════════════════════════════════
        for (int i = 0; i < NUM_BUMPS; i++) {
            Circle bump = new Circle();
            bump.setFill(new RadialGradient(
                    130, 0.18,
                    0.42, 0.38, 0.50,
                    true, CycleMethod.NO_CYCLE,
                    new Stop(0.00, Color.web("#0e0e0e")),
                    new Stop(0.50, Color.web("#1a1a1a")),
                    new Stop(0.80, Color.web("#252525")),
                    new Stop(1.00, Color.web("#1c1c1c"))
            ));
            bumpList.add(bump);
            rotatingBumps.getChildren().add(bump);
        }
        bumpHighlight = new InnerShadow(BlurType.GAUSSIAN, Color.web("#ffffff18"), 3, 0.6, -2, -2);
        bumpShadow    = new InnerShadow(BlurType.GAUSSIAN, Color.web("#000000d0"), 5, 0.4, 2, 3);
        bumpShadow.setInput(bumpHighlight);

        fixedBumpShading = new Group(rotatingBumps);
        fixedBumpShading.setEffect(bumpShadow);

        // Overlay illuminazione globale: modula la luminosità di ogni bump
        // in base alla sua posizione angolare rispetto alla fonte di luce.
        // Gradient: (0,0)→(1,1) = top-left→bottom-right nel bbox del cerchio.
        bumpLightingOverlay = new Circle();
        bumpLightingOverlay.setFill(new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#ffffff1e")),  // top-left: bump più illuminati
                new Stop(0.50, Color.web("#00000000")),  // neutro al centro
                new Stop(1.00, Color.web("#0000002e"))   // bottom-right: bump in ombra
        ));
        bumpLightingOverlay.setMouseTransparent(true);

        // ════════════════════════════════════════════════════════════════════
        // DISPLAY — bezel + chrome ring + innerDisplay + linee + hub
        //   displayCavityInner: ombra direzionale sul bordo bottom-right
        //   displayCavityOuter: ombra diffusa globale (profondità cavità)
        //   Entrambi sul wrapper fisso fixedDisplayShading → non ruotano
        // ════════════════════════════════════════════════════════════════════
        displayBezel = new Circle();
        displayBezel.setFill(new RadialGradient(
                0, 0, 0.50, 0.50, 0.50,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#252525")),
                new Stop(0.8, Color.web("#141414")),
                new Stop(1.0, Color.web("#0c0c0c"))
        ));
        // NESSUN effetto sul displayBezel: va sul wrapper fisso

        chromeRing = new Circle();
        chromeRing.setFill(Color.TRANSPARENT);
        chromeRing.setStroke(Color.web("#2e2e2e"));

        innerDisplay = new Circle();
        innerDisplay.setFill(new RadialGradient(
                130, 0.12,
                0.48, 0.45, 0.50,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#1e1e1e")),
                new Stop(0.35, Color.web("#101010")),
                new Stop(0.70, Color.web("#070707")),
                new Stop(1.00, Color.web("#020202"))
        ));
        // NESSUN effetto su innerDisplay: va sul wrapper fisso
        displayCavityInner = new InnerShadow(BlurType.GAUSSIAN, Color.web("#000000f0"), 8, 0.5, 3, 5);
        displayCavityOuter = new InnerShadow(BlurType.GAUSSIAN, Color.web("#000000aa"), 20, 0, 0, 0);
        displayCavityInner.setInput(displayCavityOuter);

        // Marker rosso
        markerGlow = new DropShadow(BlurType.GAUSSIAN, Color.web("#ff000099"), 10, 0.5, 0, 0);
        redMarker  = new Line();
        redMarker.setStroke(Color.web("#ff2020ff"));
        redMarker.setStrokeLineCap(StrokeLineCap.ROUND);
        redMarker.setEffect(markerGlow);  // glow centrato: ok anche se ruota

        // Strobe lines
        for (int i = 0; i < NUM_LINES; i++) {
            Line line = new Line();
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            line.setStroke(i % 3 == 0 ? Color.web("#4a4a4a99") : Color.web("#3a3a3a77"));
            strobeLines.add(line);
            rotatingDisplay.getChildren().add(line);
        }

        // Hub
        hubBody = new Circle();
        hubBody.setFill(new RadialGradient(
                130, 0.30,
                0.40, 0.35, 0.50,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#424242")),
                new Stop(0.45, Color.web("#1e1e1e")),
                new Stop(1.00, Color.web("#080808"))
        ));
        // Hub shadow: piccolo e centrato → rotazione non percepibile
        DropShadow hubShadow = new DropShadow(BlurType.GAUSSIAN, Color.web("#000000cc"), 5, 0.2, 0, 1);
        hubBody.setEffect(hubShadow);

        hubSpecular = new Circle();
        hubSpecular.setFill(new RadialGradient(
                0, 0, 0.40, 0.35, 0.50,
                true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#ffffff28")),
                new Stop(1.0, Color.TRANSPARENT)
        ));
        hubSpecular.setMouseTransparent(true);

        rotatingDisplay.getChildren().addAll(0, List.of(displayBezel, chromeRing, innerDisplay));
        rotatingDisplay.getChildren().add(0, redMarker);
        rotatingDisplay.getChildren().addAll(hubBody, hubSpecular);

        // fixedDisplayShading: wrapper fisso → cavità sempre con ombra fissa
        fixedDisplayShading = new Group(rotatingDisplay);
        fixedDisplayShading.setEffect(displayCavityInner);

        // ════════════════════════════════════════════════════════════════════
        // ARCHI OVERLAY FISSI — riflessi ambientali che non ruotano mai
        // ════════════════════════════════════════════════════════════════════
        rimHighlightArc = makeArc(105, 115, new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#ffffff00")),
                new Stop(0.4, Color.web("#ffffff66")),
                new Stop(1.0, Color.web("#ffffff00"))));

        rimShadowArc = makeArc(285, 120, new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#00000000")),
                new Stop(0.5, Color.web("#00000055")),
                new Stop(1.0, Color.web("#00000000"))));

        platterHighlightArc = makeArc(115, 95, new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#ffffff00")),
                new Stop(0.5, Color.web("#ffffff44")),
                new Stop(1.0, Color.web("#ffffff00"))));

        bezelChromeArc = makeArc(100, 130, new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#ffffff00")),
                new Stop(0.4, Color.web("#ffffff99")),
                new Stop(1.0, Color.web("#ffffff00"))));

        // ════════════════════════════════════════════════════════════════════
        // MONTAGGIO — ordine z-stacking dal basso verso l'alto
        // ════════════════════════════════════════════════════════════════════
        getChildren().addAll(
                fixedRimShading,       // rim + skirt + groove
                fixedBumpShading,      // bumps (concavità con ombra fissa)
                bumpLightingOverlay,   // overlay globale: modula luminosità bumps per posizione
                fixedPlatterShading,   // piatto rialzato (copre la zona interna dei bumps)
                fixedDisplayShading,   // display concavo
                rimHighlightArc,
                rimShadowArc,
                platterHighlightArc,
                bezelChromeArc
        );

        // ════════════════════════════════════════════════════════════════════
        // ROTAZIONI — pivot a (0,0) del sistema locale del gruppo
        // ════════════════════════════════════════════════════════════════════
        Rotate rRimSkirt = new Rotate(0, 0, 0);
        rRimSkirt.angleProperty().bind(deck.rotationAngleProperty());
        rotatingRimSkirt.getTransforms().add(rRimSkirt);

        Rotate rPlatter = new Rotate(0, 0, 0);
        rPlatter.angleProperty().bind(deck.rotationAngleProperty());
        rotatingPlatter.getTransforms().add(rPlatter);

        Rotate rBumps = new Rotate(0, 0, 0);
        rBumps.angleProperty().bind(deck.rotationAngleProperty());
        rotatingBumps.getTransforms().add(rBumps);

        Rotate rDisplay = new Rotate(0, 0, 0);
        rDisplay.angleProperty().bind(deck.rotationAngleProperty());
        rotatingDisplay.getTransforms().add(rDisplay);

        // ════════════════════════════════════════════════════════════════════
        // INTERAZIONE
        // ════════════════════════════════════════════════════════════════════
        deck.setOnScroll(this::onScroll);
        deck.setOnMousePressed(this::onMousePressed);
        deck.setOnMouseDragged(this::onMouseDragged);
        deck.setOnTouchPressed(this::onTouchPressed);
        deck.setOnTouchMoved(this::onTouchMoved);
    }

    /** Crea un arco stilizzato (overlay fisso) con gradiente di stroke. */
    private static Arc makeArc(double startAngle, double length, javafx.scene.paint.Paint stroke) {
        Arc arc = new Arc();
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.setStartAngle(startAngle);
        arc.setLength(length);
        arc.setStroke(stroke);
        arc.setMouseTransparent(true);
        return arc;
    }

    // ─── Event handlers ───────────────────────────────────────────────────────

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

    private void onTouchPressed(TouchEvent e) {
        oldMouseAngle = mouseAngle(e.getTouchPoint().getX(), e.getTouchPoint().getY());
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

    // ─── Sizing ───────────────────────────────────────────────────────────────
    @Override protected double computeMinWidth(double h, double t, double r, double b, double l)  { return 50; }
    @Override protected double computeMinHeight(double w, double t, double r, double b, double l) { return 50; }
    @Override protected double computePrefWidth(double h, double t, double r, double b, double l)  { return 250; }
    @Override protected double computePrefHeight(double w, double t, double r, double b, double l) { return 250; }
    @Override protected double computeMaxWidth(double h, double t, double r, double b, double l)  { return Double.MAX_VALUE; }
    @Override protected double computeMaxHeight(double w, double t, double r, double b, double l) { return Double.MAX_VALUE; }

    // ─── Layout responsivo ────────────────────────────────────────────────────
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        final double R  = Math.max(1, Math.min(w, h) * 0.48);
        final double cx = x + w / 2.0;
        final double cy = y + h / 2.0;

        // ── Raggi ─────────────────────────────────────────────────────────────
        final double outerRimR     = R;
        final double skirtR        = R * 0.962;
        final double grooveRingR   = R * 0.792;
        final double platterR      = R * 0.775;

        final double bumpOrbit     = (skirtR + platterR) / 2.0;
        final double bumpRadius    = (skirtR - platterR) * 0.44;

        final double bezelR        = R * 0.360;
        final double chromeRingR   = R * 0.338;
        final double innerDisplayR = R * 0.322;

        final double strobeOuter   = innerDisplayR * 0.88;
        final double strobeInner   = R * 0.115;

        final double hubR          = R * 0.095;
        final double hubSpecR      = hubR * 0.55;

        // ── Effetti scalati ───────────────────────────────────────────────────
        rimDropShadow.setRadius(Math.max(1, R * 0.060));

        platterDropShadow.setRadius(Math.max(1, R * 0.080));
        platterDropShadow.setOffsetY(R * 0.028);
        platterEdgeDark.setRadius(Math.max(1, R * 0.055));
        platterEdgeDark.setOffsetX(R * 0.016);
        platterEdgeDark.setOffsetY(R * 0.032);

        bumpShadow.setRadius(Math.max(1, R * 0.024));
        bumpShadow.setOffsetX(R * 0.010);
        bumpShadow.setOffsetY(R * 0.014);
        bumpHighlight.setRadius(Math.max(1, R * 0.014));
        bumpHighlight.setOffsetX(-R * 0.008);
        bumpHighlight.setOffsetY(-R * 0.010);

        displayCavityInner.setRadius(Math.max(1, R * 0.032));
        displayCavityInner.setOffsetX(R * 0.012);
        displayCavityInner.setOffsetY(R * 0.022);
        displayCavityOuter.setRadius(Math.max(1, R * 0.082));

        markerGlow.setRadius(Math.max(2, R * 0.040));

        // ── Pivot dei wrapper fissi (al centro del component) ─────────────────
        // Pattern: fixedXxx.setLayoutX(cx) → il wrapper si muove al centro
        //          rotatingXxx.setLayoutX(0) → il contenuto sta all'origine del wrapper
        fixedRimShading.setLayoutX(cx);     fixedRimShading.setLayoutY(cy);
        rotatingRimSkirt.setLayoutX(0);     rotatingRimSkirt.setLayoutY(0);

        fixedPlatterShading.setLayoutX(cx); fixedPlatterShading.setLayoutY(cy);
        rotatingPlatter.setLayoutX(0);      rotatingPlatter.setLayoutY(0);

        fixedBumpShading.setLayoutX(cx);    fixedBumpShading.setLayoutY(cy);
        rotatingBumps.setLayoutX(0);        rotatingBumps.setLayoutY(0);

        // L'overlay è centrato sul disco e non ruota
        bumpLightingOverlay.setLayoutX(cx); bumpLightingOverlay.setLayoutY(cy);

        fixedDisplayShading.setLayoutX(cx); fixedDisplayShading.setLayoutY(cy);
        rotatingDisplay.setLayoutX(0);      rotatingDisplay.setLayoutY(0);

        rimHighlightArc.setLayoutX(cx);     rimHighlightArc.setLayoutY(cy);
        rimShadowArc.setLayoutX(cx);        rimShadowArc.setLayoutY(cy);
        platterHighlightArc.setLayoutX(cx); platterHighlightArc.setLayoutY(cy);
        bezelChromeArc.setLayoutX(cx);      bezelChromeArc.setLayoutY(cy);

        // ── Strati base ───────────────────────────────────────────────────────
        outerRim.setRadius(outerRimR);
        skirt.setRadius(skirtR);
        grooveRing.setRadius(grooveRingR);
        // L'overlay occupa tutta la zona skirt (include il groove ring)
        bumpLightingOverlay.setRadius(skirtR);
        mainPlatter.setRadius(platterR);

        // ── Bumps ─────────────────────────────────────────────────────────────
        final double bumpStep = 360.0 / NUM_BUMPS;
        for (int i = 0; i < NUM_BUMPS; i++) {
            double ang = Math.toRadians(i * bumpStep);
            Circle b = bumpList.get(i);
            b.setCenterX(bumpOrbit * Math.cos(ang));
            b.setCenterY(bumpOrbit * Math.sin(ang));
            b.setRadius(bumpRadius);
        }

        // ── Display ───────────────────────────────────────────────────────────
        displayBezel.setRadius(bezelR);
        chromeRing.setRadius(chromeRingR);
        chromeRing.setStrokeWidth(Math.max(0.5, R * 0.007));
        innerDisplay.setRadius(innerDisplayR);

        // ── Strobe lines ──────────────────────────────────────────────────────
        final double lineStep  = 360.0 / NUM_LINES;
        final double lineWidth = Math.max(0.5, R * 0.0045);
        for (int i = 0; i < NUM_LINES; i++) {
            double ang = Math.toRadians(i * lineStep);
            Line l = strobeLines.get(i);
            l.setStartX(strobeInner * Math.cos(ang));
            l.setStartY(strobeInner * Math.sin(ang));
            l.setEndX(strobeOuter * Math.cos(ang));
            l.setEndY(strobeOuter * Math.sin(ang));
            l.setStrokeWidth(lineWidth);
        }

        // Marker rosso: verticale
        redMarker.setStartX(0); redMarker.setStartY(-strobeOuter * 1.08);
        redMarker.setEndX(0);   redMarker.setEndY( strobeOuter * 1.08);
        redMarker.setStrokeWidth(Math.max(1.2, R * 0.008));

        // ── Hub ───────────────────────────────────────────────────────────────
        hubBody.setRadius(hubR);
        hubSpecular.setRadius(hubSpecR);
        hubSpecular.setCenterX(-hubR * 0.20);
        hubSpecular.setCenterY(-hubR * 0.24);

        // ── Archi fissi ───────────────────────────────────────────────────────
        double rimArcR  = outerRimR - R * 0.014;
        rimHighlightArc.setRadiusX(rimArcR);   rimHighlightArc.setRadiusY(rimArcR);
        rimHighlightArc.setStrokeWidth(Math.max(1.0, R * 0.024));
        rimShadowArc.setRadiusX(rimArcR);       rimShadowArc.setRadiusY(rimArcR);
        rimShadowArc.setStrokeWidth(Math.max(1.0, R * 0.024));

        double platArcR = platterR - R * 0.010;
        platterHighlightArc.setRadiusX(platArcR); platterHighlightArc.setRadiusY(platArcR);
        platterHighlightArc.setStrokeWidth(Math.max(0.5, R * 0.012));

        double bezArcR  = chromeRingR + R * 0.004;
        bezelChromeArc.setRadiusX(bezArcR);    bezelChromeArc.setRadiusY(bezArcR);
        bezelChromeArc.setStrokeWidth(Math.max(0.8, R * 0.016));
    }
}
