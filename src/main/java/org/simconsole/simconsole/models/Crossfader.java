package org.simconsole.simconsole.models;
public class Crossfader {
    private DeckControls deckAControls;
    private DeckControls deckBControls;
    public Crossfader(DeckControls deckAControls, DeckControls deckBControls) {
        this.deckAControls = deckAControls;
        this.deckBControls = deckBControls;
    }
    public void crossfade(double pos) {
        pos = Math.max(-1.0, Math.min(1.0, pos));
        double angle = (pos + 1.0) * (Math.PI / 4.0);
        double gainA = Math.cos(angle);
        double gainB = Math.sin(angle);
        if (deckAControls != null) {
            deckAControls.setCrossfaderGain(gainA);
        }
        if (deckBControls != null) {
            deckBControls.setCrossfaderGain(gainB);
        }
    }
    public void setDeckAControls(DeckControls deckAControls) {
        this.deckAControls = deckAControls;
    }
    public void setDeckBControls(DeckControls deckBControls) {
        this.deckBControls = deckBControls;
    }
}