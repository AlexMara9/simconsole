package org.simconsole.simconsole;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BPMAnalyzer {

    // Esegue l'analisi BPM in modo asincrono per non bloccare la UI
    public static void detectBpmAsync(Tracks track, Runnable onComplete) {
        CompletableFuture.runAsync(() -> {
            try {
                File audioFile = new File(track.getFilePath());
                // Dispatcher che legge il file a 44100Hz
                AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(audioFile, 2048, 1024);
                
                // Setup del gestore nativo BeatRoot per la predizione dei BPM
                be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler beatRootHandler = new be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler();
                
                // Setup dell'OnsetDetector ottimizzato
                be.tarsos.dsp.onsets.ComplexOnsetDetector onsetDetector = new be.tarsos.dsp.onsets.ComplexOnsetDetector(2048);
                onsetDetector.setHandler(beatRootHandler);
                        
                // Aggiungiamo i processori
                dispatcher.addAudioProcessor(onsetDetector);
                
                // Esegue l'elaborazione (blocca questo thread asincrono finché non finisce l'intero file)
                dispatcher.run();
                
                // Fine del file: chiediamo a TarsosDSP di indovinare i battiti!
                System.out.println("Elaborazione onsets terminata. Calcolo BPM automatico TarsosDSP...");
                
                java.util.List<Double> beatTimes = new java.util.ArrayList<>();
                beatRootHandler.trackBeats((time, salience) -> beatTimes.add(time));
                
                double bpm = 120.0;
                if (beatTimes.size() > 1) {
                    // Tarsos restituisce i tempi esatti in secondi dei battiti
                    double totalDuration = beatTimes.get(beatTimes.size() - 1) - beatTimes.get(0);
                    double avgInterval = totalDuration / (beatTimes.size() - 1);
                    bpm = 60.0 / avgInterval;
                    
                    while (bpm < 70) bpm *= 2;
                    while (bpm > 150) bpm /= 2;
                }
                
                bpm = Math.round(bpm * 10.0) / 10.0;
                track.setOriginalBpm(bpm);
                
                System.out.println("BPM Stimato TarsosDSP (BeatRoot): " + track.getOriginalBpm());
                if (onComplete != null) {
                    onComplete.run();
                }
                
            } catch (Exception e) {
                System.err.println("Errore durante l'analisi BPM nativa con TarsosDSP: " + e.getMessage());
                track.setOriginalBpm(120.0); // Fallback
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }
}
