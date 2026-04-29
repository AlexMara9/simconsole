package org.simconsole.simconsole;

public class Crossfader {

    private Deck deckA;
    private Deck deckB;

    public Crossfader(Deck deckA, Deck deckB) {
        this.deckA = deckA;
        this.deckB = deckB;
    }


    public void crossfade(double pos) {
        // Clampa la posizione per sicurezza tra -1.0 e 1.0
        pos = Math.max(-1.0, Math.min(1.0, pos));
        
        // Curva a Potenza Costante (Trigonometrica): 
        // Evita che il volume raddoppi al centro (-3dB di abbassamento a pos=0)
        // Mappa la posizione da [-1, 1] a un angolo tra [0, PI/2]
        double angle = (pos + 1.0) * (Math.PI / 4.0);
        double gainA = Math.cos(angle);
        double gainB = Math.sin(angle);
        
        if (deckA != null) {
            deckA.setCrossfaderGain(gainA);
        }
        
        if (deckB != null) {
            deckB.setCrossfaderGain(gainB);
        }
    }

    public void setDeckA(Deck deckA) {
        this.deckA = deckA;
    }

    public void setDeckB(Deck deckB) {
        this.deckB = deckB;
    }
}