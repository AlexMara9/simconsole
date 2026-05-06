package org.simconsole.simconsole;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import org.simconsole.simconsole.audio.external.Sonic;

public class Deck {
    private DeckControls controls;

    public void setControls(DeckControls controls) {
        this.controls = controls;
    }
    private double[] audioData;
    private volatile double playhead = 0.0;
    private volatile boolean isPlaying = false;
    private Tracks currentTrack;

    private volatile double internalVolume = 0.0;

    private SourceDataLine speakerLine;
    private Thread playbackThread;

    // Fade più lento per evitare distorsioni armoniche (circa 50ms)
    private static final double FADE_SPEED = 0.0005;

    public void loadTrack(Tracks track) {
        isPlaying = false;

        double[] newData = AudioDecoder.readWavFileAsDoubles(track.getFilePath());

        if (newData != null) {
            this.currentTrack = track;
            this.audioData = newData;
            this.playhead = 0.0;
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
            float[] inputFloatBuffer = new float[framesPerWrite * 2];
            float[] outputFloatBuffer = new float[framesPerWrite * 2];
            Sonic sonic = new Sonic(44100, 2);

            while (!Thread.currentThread().isInterrupted()) {
                double targetPitch = controls != null ? controls.getPitch() : 1.0;
                boolean keyLock = controls != null && controls.isKeyLock();
                
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
                    
                    for (int i = 0; i < framesPerWrite && playhead < audioData.length - 3; i++) {
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

                        // 2. APPLICAZIONE CONTROLLI
                        left *= internalVolume;
                        right *= internalVolume;

                        if (controls != null) {
                            left = controls.processLeft(left);
                            right = controls.processRight(right);
                        }

                        // 3. HARD LIMITER
                        left = Math.max(-1.0, Math.min(1.0, left));
                        right = Math.max(-1.0, Math.min(1.0, right));

                        inputFloatBuffer[inputFramesRead * 2] = (float) left;
                        inputFloatBuffer[inputFramesRead * 2 + 1] = (float) right;
                        inputFramesRead++;
                    }

                    if (inputFramesRead > 0) {
                        sonic.writeFloatToStream(inputFloatBuffer, inputFramesRead);
                    } else {
                        break; // Fine file o deck fermo
                    }
                }

                if (playhead >= audioData.length - 3 && isPlaying) {
                    isPlaying = false;
                    sonic.flushStream();
                }

                // 4. ESTRAZIONE DA SONIC E CONVERSIONE PCM
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

    public double[] getAudioData() {
        return audioData;
    }
    public int getPlayhead() {
        return (int) playhead;
    }

    public double getCurrentBpm() {
        if (currentTrack == null || controls == null) return 0.0;
        return currentTrack.getOriginalBpm() * controls.getPitch();
    }
}