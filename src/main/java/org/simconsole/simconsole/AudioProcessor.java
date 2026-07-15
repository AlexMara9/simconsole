package org.simconsole.simconsole;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import org.simconsole.simconsole.audio.external.Sonic;

public class AudioProcessor {
    private Deck deck;
    private SourceDataLine speakerLine;
    private Thread playbackThread;

    private static final double FADE_SPEED = 0.0005;

    // Biquad filters for EQ
    private final BiquadFilter eqLowL = new BiquadFilter(BiquadFilter.FilterType.LOW_SHELF, 44100, 150, 0.707, 0.0);
    private final BiquadFilter eqLowR = new BiquadFilter(BiquadFilter.FilterType.LOW_SHELF, 44100, 150, 0.707, 0.0);
    private final BiquadFilter eqMidL = new BiquadFilter(BiquadFilter.FilterType.PEAKING, 44100, 1000, 0.707, 0.0);
    private final BiquadFilter eqMidR = new BiquadFilter(BiquadFilter.FilterType.PEAKING, 44100, 1000, 0.707, 0.0);
    private final BiquadFilter eqHighL = new BiquadFilter(BiquadFilter.FilterType.HIGH_SHELF, 44100, 4000, 0.707, 0.0);
    private final BiquadFilter eqHighR = new BiquadFilter(BiquadFilter.FilterType.HIGH_SHELF, 44100, 4000, 0.707, 0.0);

    // Nuovi effetti: Flanger e Reverb
    private final Flanger flangerL = new Flanger(44100);
    private final Flanger flangerR = new Flanger(44100);
    private final Reverb reverb = new Reverb(44100);
    private final Echo eco = new Echo(44100);

    public AudioProcessor(Deck deck) {
        this.deck = deck;
    }

    public void startPlayback() {
        try {
            if (speakerLine == null) {
                AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                speakerLine = (SourceDataLine) AudioSystem.getLine(info);
                speakerLine.open(format, 16384);
                speakerLine.start();
            } else {
                speakerLine.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (playbackThread == null || !playbackThread.isAlive()) {
            startPlaybackThread();
        }
    }

    private void startPlaybackThread() {
        playbackThread = new Thread(() -> {
            int framesPerWrite = 512;
            byte[] outputBuffer = new byte[framesPerWrite * 4];
            float[] inputFloatBuffer = new float[framesPerWrite * 2];
            float[] outputFloatBuffer = new float[framesPerWrite * 2];
            Sonic sonic = new Sonic(44100, 2);

            while (!Thread.currentThread().isInterrupted()) {
                double[] audioData = deck.getAudioData();
                DeckControls controls = deck.getControls();
                
                double targetPitch = controls != null ? controls.getPitch() : 1.0;
                boolean keyLock = controls != null && controls.isKeyLock();
                boolean isPlaying = deck.isPlaying();
                double playhead = deck.getPlayheadDouble();
                double internalVolume = deck.getInternalVolume();
                
                // Aggiorna i filtri Biquad con i valori da controls
                if (controls != null) {
                    eqLowL.setGain(controls.getEqLow());
                    eqLowR.setGain(controls.getEqLow());
                    eqMidL.setGain(controls.getEqMid());
                    eqMidR.setGain(controls.getEqMid());
                    eqHighL.setGain(controls.getEqHigh());
                    eqHighR.setGain(controls.getEqHigh());
                    
                    // Aggiorna Flanger e Reverb
                    flangerL.setEnabled(controls.isFlangerEnabled());
                    flangerR.setEnabled(controls.isFlangerEnabled());
                    flangerL.setWet(controls.getFlangerWet());
                    flangerR.setWet(controls.getFlangerWet());
                    flangerL.setBellEnabled(controls.isFlangerBellEnabled());
                    flangerR.setBellEnabled(controls.isFlangerBellEnabled());

                    reverb.setEnabled(controls.isReverbEnabled());
                    reverb.setWet(controls.getReverbWet());
                    
                    eco.setEnabled(controls.isEcoEnabled());
                    eco.setWet(controls.getEcoWet());
                }

                // Configura Sonic: se Key Lock è acceso cambia la 'speed', altrimenti il 'rate' (effetto vinile)
                if (keyLock) {
                    sonic.setSpeed((float) targetPitch);
                    sonic.setRate(1.0f);
                } else {
                    sonic.setSpeed(1.0f);
                    sonic.setRate((float) targetPitch);
                }

                // Continua a dare in pasto audio a Sonic finché non ha abbastanza campioni per la cassa
                while (sonic.samplesAvailable() < framesPerWrite && (isPlaying || internalVolume > 0)) {
                    int inputFramesRead = 0;
                    
                    for (int i = 0; i < framesPerWrite && audioData != null && playhead < audioData.length - 3; i++) {
                        // 1. SMOOTH VOLUME RAMPING
                        double targetVolume = isPlaying && controls != null ? controls.getVolume() : 0.0;
                        if (internalVolume < targetVolume) {
                            internalVolume = Math.min(targetVolume, internalVolume + FADE_SPEED);
                        } else if (internalVolume > targetVolume) {
                            internalVolume = Math.max(targetVolume, internalVolume - FADE_SPEED);
                        }

                        int index = (int) playhead;
                        if (index % 2 != 0) index--; // Allineamento stereo
                        
                        double left = audioData[index];
                        double right = audioData[index + 1];
                        
                        // Avanziamo SEMPRE di 2 (1 frame = L+R). Il pitch/speed lo gestisce Sonic!
                        playhead += 2.0; 

                        // 2. APPLICAZIONE CONTROLLI DI BASE
                        left *= internalVolume;
                        right *= internalVolume;

                        // 3. APPLICAZIONE DSP (EQ E PAN)
                        left = eqLowL.process(left);
                        left = eqMidL.process(left);
                        left = eqHighL.process(left);

                        right = eqLowR.process(right);
                        right = eqMidR.process(right);
                        right = eqHighR.process(right);

                        if (controls != null) {
                            double pan = controls.getPan();
                            double leftGain = Math.min(1.0, Math.max(0.0, 1.0 + pan));
                            double rightGain = Math.min(1.0, Math.max(0.0, 1.0 - pan));
                            left *= leftGain;
                            right *= rightGain;
                        }

                        // Applica Flanger
                        left = flangerL.process(left);
                        right = flangerR.process(right);

                        // Applica Reverb
                        double[] revOut = reverb.process(left, right);
                        left = revOut[0];
                        right = revOut[1];

                        // Applica Echo
                        double[] ecoOut = eco.process(left, right);
                        left = ecoOut[0];
                        right = ecoOut[1];

                        // 4. HARD LIMITER
                        left = Math.max(-1.0, Math.min(1.0, left));
                        right = Math.max(-1.0, Math.min(1.0, right));

                        inputFloatBuffer[inputFramesRead * 2] = (float) left;
                        inputFloatBuffer[inputFramesRead * 2 + 1] = (float) right;
                        inputFramesRead++;
                    }
                    
                    // Salva le variazioni di stato sul deck (playhead e internalVolume)
                    deck.setPlayheadDouble(playhead);
                    deck.setInternalVolume(internalVolume);

                    if (inputFramesRead > 0) {
                        sonic.writeFloatToStream(inputFloatBuffer, inputFramesRead);
                    } else {
                        break; // Fine file o deck fermo
                    }
                }

                if (audioData != null && deck.getPlayheadDouble() >= audioData.length - 3 && deck.isPlaying()) {
                    deck.setPlaying(false);
                    sonic.flushStream();
                }

                // 5. ESTRAZIONE DA SONIC E CONVERSIONE PCM
                int framesToRead = Math.min(framesPerWrite, sonic.samplesAvailable());
                if (framesToRead > 0) {
                    int read = sonic.readFloatFromStream(outputFloatBuffer, framesToRead);
                    int bufferIndex = 0;
                    
                    for (int i = 0; i < read * 2; i += 2) {
                        short pcmL = (short) (outputFloatBuffer[i] * 32767.0f);
                        short pcmR = (short) (outputFloatBuffer[i + 1] * 32767.0f);

                        outputBuffer[bufferIndex++] = (byte) (pcmL & 0xFF);
                        outputBuffer[bufferIndex++] = (byte) ((pcmL >> 8) & 0xFF);
                        outputBuffer[bufferIndex++] = (byte) (pcmR & 0xFF);
                        outputBuffer[bufferIndex++] = (byte) ((pcmR >> 8) & 0xFF);
                    }
                    
                    if (speakerLine != null && speakerLine.isOpen()) {
                        speakerLine.write(outputBuffer, 0, bufferIndex);
                    }
                } else {
                    // Scriviamo zeri se il lettore è in pausa per mantenere la scheda audio "calda"
                    if (speakerLine != null && speakerLine.isOpen()) {
                        speakerLine.write(new byte[framesPerWrite * 4], 0, framesPerWrite * 4);
                    }
                }
            }
        });

        playbackThread.setPriority(Thread.MAX_PRIORITY);
        playbackThread.start();
    }
}
