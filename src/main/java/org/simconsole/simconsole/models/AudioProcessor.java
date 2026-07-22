package org.simconsole.simconsole.models;

import org.simconsole.simconsole.models.audio.external.Sonic;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import org.simconsole.simconsole.models.audio.external.Sonic;

public class AudioProcessor {
    private Deck deck;
    private SourceDataLine speakerLine;
    private Thread playbackThread;

    private static final double FADE_SPEED = 0.0005;
    public static volatile double masterVolume = 1.0;

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
            boolean wasScrubbing = false;
            double scratchLocalPlayhead = 0;
            double scratchVelocity = 0;
            double scratchVolume = 0;
            Sonic sonic = new Sonic(44100, 2);

            while (!Thread.currentThread().isInterrupted()) {
                double[] audioData = deck.getAudioData();
                DeckControls controls = deck.getControls();
                
                if (deck.isScrubbing()) {
                    if (!wasScrubbing) {
                        scratchLocalPlayhead = deck.getPlayheadDouble();
                        scratchVelocity = 0;
                        scratchVolume = 1.0; // Start at full volume since we are interrupting playback
                    }
                    wasScrubbing = true;

                    double targetPlayhead = deck.getPlayheadDouble();
                    int inputFramesRead = 0;

                    // Calcolo della velocità bersaglio (targetVelocity) per questo blocco audio (512 campioni).
                    // Vogliamo coprire la distanza (diff) in circa 3000 campioni (circa 60-70ms)
                    // per un effetto vinile molto fluido e pastoso.
                    double diff = targetPlayhead - scratchLocalPlayhead;
                    double targetVelocity = diff / 1500.0;

                    // Cap rimosso per permettere velocità di scratch realistiche (nessun limite)
                    if (targetVelocity > 200.0) targetVelocity = 200.0; // Solo un limite estremo di sicurezza
                    if (targetVelocity < -200.0) targetVelocity = -200.0;

                    for (int i = 0; i < framesPerWrite && audioData != null; i++) {

                        // Interpolazione morbidissima della velocità per ogni singolo campione audio (44100Hz)
                        // Questo elimina totalmente i transienti e i click!
                        scratchVelocity += (targetVelocity - scratchVelocity) * 0.002;

                        scratchLocalPlayhead += scratchVelocity;

                        if (scratchLocalPlayhead < 0) { scratchLocalPlayhead = 0; scratchVelocity = 0; }
                        if (scratchLocalPlayhead >= audioData.length - 2) { scratchLocalPlayhead = audioData.length - 2; scratchVelocity = 0; }

                        // Anti DC-offset: se siamo praticamente fermi, facciamo fade out del volume
                        if (Math.abs(scratchVelocity) > 0.02 || Math.abs(targetPlayhead - scratchLocalPlayhead) > 5.0) {
                            scratchVolume = Math.min(1.0, scratchVolume + 0.005); // Fade in veloce
                        } else {
                            scratchVolume = Math.max(0.0, scratchVolume - 0.002); // Fade out dolce
                        }

                        int index = (int) scratchLocalPlayhead;
                        if (index % 2 != 0) index--;

                        double left = 0;
                        double right = 0;

                        if (index >= 0 && index < audioData.length - 3) {
                            // Linear interpolation for sub-sample accuracy (smooth pitch)
                            double frac = (scratchLocalPlayhead - index) / 2.0;
                            left = audioData[index];
                            right = audioData[index + 1];
                            left = left + frac * (audioData[index + 2] - left);
                            right = right + frac * (audioData[index + 3] - right);
                        }

                        double baseVolume = controls != null ? controls.getVolume() : 1.0;
                        left *= scratchVolume * baseVolume;
                        right *= scratchVolume * baseVolume;

                        // Applica EQ per coerenza timbrica
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

                        left *= masterVolume;
                        right *= masterVolume;

                        // Hard limiter come in normal playback
                        left = Math.max(-1.0, Math.min(1.0, left));
                        right = Math.max(-1.0, Math.min(1.0, right));

                        outputFloatBuffer[i * 2] = (float) left;
                        outputFloatBuffer[i * 2 + 1] = (float) right;
                        inputFramesRead++;
                    }

                    if (inputFramesRead > 0) {
                        int bufferIndex = 0;
                        for (int i = 0; i < inputFramesRead * 2; i += 2) {
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
                    }

                } else {
                    if (wasScrubbing) {
                        deck.setPlayheadDouble(scratchLocalPlayhead);
                        sonic = new Sonic(44100, 2); // Reset sonic completely
                        wasScrubbing = false;
                    }

                    double targetPitch = controls != null ? controls.getPitch() : 1.0;
                    boolean keyLock = controls != null && controls.isKeyLock();
                    boolean isPlaying = deck.isPlaying();
                    double playhead = deck.getPlayheadDouble();
                    double internalVolume = deck.getInternalVolume();

                    if (controls != null) {
                        eqLowL.setGain(controls.getEqLow());
                        eqLowR.setGain(controls.getEqLow());
                        eqMidL.setGain(controls.getEqMid());
                        eqMidR.setGain(controls.getEqMid());
                        eqHighL.setGain(controls.getEqHigh());
                        eqHighR.setGain(controls.getEqHigh());

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

                    if (keyLock) {
                        sonic.setSpeed((float) targetPitch);
                        sonic.setRate(1.0f);
                    } else {
                        sonic.setSpeed(1.0f);
                        sonic.setRate((float) targetPitch);
                    }

                    while (sonic.samplesAvailable() < framesPerWrite && (isPlaying || internalVolume > 0)) {
                        int inputFramesRead = 0;

                        for (int i = 0; i < framesPerWrite && audioData != null && playhead < audioData.length - 3; i++) {
                            double targetVolume = isPlaying && controls != null ? controls.getVolume() : 0.0;
                            if (internalVolume < targetVolume) {
                                internalVolume = Math.min(targetVolume, internalVolume + FADE_SPEED);
                            } else if (internalVolume > targetVolume) {
                                internalVolume = Math.max(targetVolume, internalVolume - FADE_SPEED);
                            }

                            int index = (int) playhead;
                            if (index % 2 != 0) index--;

                            double left = audioData[index];
                            double right = audioData[index + 1];

                            playhead += 2.0;

                            left *= internalVolume;
                            right *= internalVolume;

                            // 1. Applica EQ per coerenza timbrica
                            left = eqLowL.process(left);
                            left = eqMidL.process(left);
                            left = eqHighL.process(left);
                            right = eqLowR.process(right);
                            right = eqMidR.process(right);
                            right = eqHighR.process(right);

                            // 2. Pan
                            if (controls != null) {
                                double pan = controls.getPan();
                                double leftGain = Math.min(1.0, Math.max(0.0, 1.0 + pan));
                                double rightGain = Math.min(1.0, Math.max(0.0, 1.0 - pan));
                                left *= leftGain;
                                right *= rightGain;
                            }

                            left *= masterVolume;
                            right *= masterVolume;

                            // 3. Applica Effetti
                            left = flangerL.process(left);
                            right = flangerR.process(right);

                            double[] revOut = reverb.process(left, right);
                            left = revOut[0];
                            right = revOut[1];

                            double[] ecoOut = eco.process(left, right);
                            left = ecoOut[0];
                            right = ecoOut[1];

                            // 4. Hard limiter
                            left = Math.max(-1.0, Math.min(1.0, left));
                            right = Math.max(-1.0, Math.min(1.0, right));

                            inputFloatBuffer[inputFramesRead * 2] = (float) left;
                            inputFloatBuffer[inputFramesRead * 2 + 1] = (float) right;
                            inputFramesRead++;
                        }

                        deck.setPlayheadDouble(playhead);
                        deck.setInternalVolume(internalVolume);

                        if (inputFramesRead > 0) {
                            sonic.writeFloatToStream(inputFloatBuffer, inputFramesRead);
                        } else {
                            break;
                        }
                    }

                    if (audioData != null && deck.getPlayheadDouble() >= audioData.length - 3 && deck.isPlaying()) {
                        deck.setPlaying(false);
                        sonic.flushStream();
                    }

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
                        if (speakerLine != null && speakerLine.isOpen()) {
                            speakerLine.write(new byte[framesPerWrite * 4], 0, framesPerWrite * 4);
                        }
                    }
                }
            }
        });

        playbackThread.setPriority(Thread.MAX_PRIORITY);
        playbackThread.start();
    }
}
