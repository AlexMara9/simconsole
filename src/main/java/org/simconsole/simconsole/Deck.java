package org.simconsole.simconsole;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class Deck extends DeckControls {
    private double[] audioData;
    private volatile int playhead = 0;
    private volatile boolean isPlaying = false;

    private volatile double internalVolume = 0.0;

    private SourceDataLine speakerLine;
    private Thread playbackThread;

    // Fade più lento per evitare distorsioni armoniche (circa 50ms)
    private static final double FADE_SPEED = 0.0005;

    public void loadTrack(Tracks track) {
        isPlaying = false;

        double[] newData = AudioDecoder.readWavFileAsDoubles(track.getFilePath());

        if (newData != null) {
            this.audioData = newData;
            this.playhead = 0;
            this.internalVolume = 0.0;
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
    }

    public void play() {
        if (audioData == null) return;
        isPlaying = true;
    }

    public void pause() {
        isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private void startPlaybackThread() {
        playbackThread = new Thread(() -> {
            int framesPerWrite = 512;
            byte[] outputBuffer = new byte[framesPerWrite * 4];

            while (!Thread.currentThread().isInterrupted()) {
                int bufferIndex = 0;

                for (int i = 0; i < framesPerWrite; i++) {
                    // 1. SMOOTH VOLUME RAMPING
                    double targetVolume = isPlaying ? getVolume() : 0.0;
                    if (internalVolume < targetVolume) {
                        internalVolume = Math.min(targetVolume, internalVolume + FADE_SPEED);
                    } else if (internalVolume > targetVolume) {
                        internalVolume = Math.max(targetVolume, internalVolume - FADE_SPEED);
                    }

                    double left = 0, right = 0;

                    boolean advancing = (isPlaying || internalVolume > 0);

                    // 2. GENERAZIONE SEGNALE E APPLICAZIONE CONTROLLI
                    if (advancing && playhead < audioData.length - 2) {
                        left = audioData[playhead] * internalVolume;
                        right = audioData[playhead + 1] * internalVolume;
                        playhead += 2;

                        left = processLeft(left);
                        right = processRight(right);
                    } else if (advancing && playhead >= audioData.length - 2) {
                        isPlaying = false;
                    }

                    // 3. HARD LIMITER (Evita la distorsione da clipping)
                    left = Math.max(-1.0, Math.min(1.0, left));
                    right = Math.max(-1.0, Math.min(1.0, right));

                    // 4. CONVERSIONE PCM 16-BIT
                    short pcmL = (short) (left * 32767.0);
                    short pcmR = (short) (right * 32767.0);

                    outputBuffer[bufferIndex++] = (byte) (pcmL & 0xFF);
                    outputBuffer[bufferIndex++] = (byte) ((pcmL >> 8) & 0xFF);
                    outputBuffer[bufferIndex++] = (byte) (pcmR & 0xFF);
                    outputBuffer[bufferIndex++] = (byte) ((pcmR >> 8) & 0xFF);
                }

                // Scriviamo SEMPRE, anche se sono zeri. Mantiene la scheda audio "calda".
                if (speakerLine != null && speakerLine.isOpen()) {
                    speakerLine.write(outputBuffer, 0, outputBuffer.length);
                }
            }
        });

        playbackThread.setPriority(Thread.MAX_PRIORITY);
        playbackThread.start();
    }

    public double[] getAudioData() {
        return audioData;
    }
    public int getPlayhead() {
        return playhead;
    }
}