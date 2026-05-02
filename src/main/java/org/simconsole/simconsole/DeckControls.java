package org.simconsole.simconsole;

public class DeckControls {
    private Deck deck;

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public void play() {
        if (deck != null) {
            deck.play();
        }
    }

    public void pause() {
        if (deck != null) {
            deck.pause();
        }
    }

    public boolean isPlaying() {
        return deck != null && deck.isPlaying();
    }

    private volatile double volume = 0.2;
    private volatile double crossfaderGain = 1.0; // Moltiplicatore assegnato dal crossfader
    private volatile double pan = 0.0; // 1.0 = tutto a sinistra, -1.0 = tutto a destra

    private final BiquadFilter eqLowL = new BiquadFilter(BiquadFilter.FilterType.LOW_SHELF, 44100, 150, 0.707, 0.0);
    private final BiquadFilter eqLowR = new BiquadFilter(BiquadFilter.FilterType.LOW_SHELF, 44100, 150, 0.707, 0.0);
    private final BiquadFilter eqMidL = new BiquadFilter(BiquadFilter.FilterType.PEAKING, 44100, 1000, 0.707, 0.0);
    private final BiquadFilter eqMidR = new BiquadFilter(BiquadFilter.FilterType.PEAKING, 44100, 1000, 0.707, 0.0);
    private final BiquadFilter eqHighL = new BiquadFilter(BiquadFilter.FilterType.HIGH_SHELF, 44100, 4000, 0.707, 0.0);
    private final BiquadFilter eqHighR = new BiquadFilter(BiquadFilter.FilterType.HIGH_SHELF, 44100, 4000, 0.707, 0.0);

    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
    }

    public void setCrossfaderGain(double gain) {
        this.crossfaderGain = Math.max(0.0, Math.min(1.0, gain));
    }

    public double getVolume() {
        return volume * crossfaderGain;
    }

    public void setPan(double value) {
        // Clampa il valore tra -1.0 e 1.0
        this.pan = Math.max(-1.0, Math.min(1.0, value));
    }

    public double getPan() {
        return pan;
    }

    private static final double MAX_EQ_GAIN_DB = 12.0;
    private static final double MAX_EQ_LOW_GAIN_DB = 6.0; // Bassi limitati a +6dB per evitare clipping eccessivo

    public void setEqLow(double gainDb) {
        // I bassi possono scendere a -12dB ma salire massimo a +6dB
        gainDb = Math.max(-MAX_EQ_GAIN_DB, Math.min(MAX_EQ_LOW_GAIN_DB, gainDb));
        eqLowL.setGain(gainDb);
        eqLowR.setGain(gainDb);
    }

    public void setEqMid(double gainDb) {
        gainDb = Math.max(-MAX_EQ_GAIN_DB, Math.min(MAX_EQ_GAIN_DB, gainDb));
        eqMidL.setGain(gainDb);
        eqMidR.setGain(gainDb);
    }

    public void setEqHigh(double gainDb) {
        gainDb = Math.max(-MAX_EQ_GAIN_DB, Math.min(MAX_EQ_GAIN_DB, gainDb));
        eqHighL.setGain(gainDb);
        eqHighR.setGain(gainDb);
    }

    public double processLeft(double sample) {
        sample = eqLowL.process(sample);
        sample = eqMidL.process(sample);
        sample = eqHighL.process(sample);
        
        // Calcolo gain per il panning (1.0 = sinistra, -1.0 = destra)
        double leftGain = Math.min(1.0, Math.max(0.0, 1.0 + pan));
        return sample * leftGain;
    }

    public double processRight(double sample) {
        sample = eqLowR.process(sample);
        sample = eqMidR.process(sample);
        sample = eqHighR.process(sample);

        // Calcolo gain per il panning (1.0 = sinistra, -1.0 = destra)
        double rightGain = Math.min(1.0, Math.max(0.0, 1.0 - pan));
        return sample * rightGain;
    }
}
