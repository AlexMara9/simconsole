# SimConsole

SimConsole è un'applicazione per DJ Console a doppio deck, completamente responsive, costruita da zero in Java e JavaFX. Permette agli utenti di caricare, mixare e manipolare tracce audio in tempo reale.

## Obiettivi del Progetto

L'obiettivo principale di SimConsole è fornire un'esperienza di digital DJing completa all'interno di un ambiente desktop. Le funzionalità chiave includono:

- **Playback a Doppio Deck:** Due giradischi (deck) interattivi con stati di riproduzione indipendenti, manipolazione della testina di riproduzione e visualizzazione della forma d'onda (waveform).
- **Effetti DSP in Tempo Reale:** Effetti sonori integrati tra cui Echo, Flanger e Riverbero algoritmico.
- **Equalizzatore a 3 Bande:** Manipolazione in tempo reale delle frequenze basse, medie e alte utilizzando filtri digitali Biquad.
- **Mixer & Crossfader:** Volumi dei canali indipendenti, bilanciamento (pan) e un crossfader DJ con curva a potenza costante (constant-power).
- **Controllo del Pitch:** Manipolazione in tempo reale del tempo e del pitch della traccia, con supporto per il Key Lock.
- **Gestione delle Tracce:** Ricerca e streaming dinamico delle tracce utilizzando le API di Audius, oppure caricamento diretto di file locali.
- **UI Completamente Responsive:** Un'interfaccia grafica vettoriale che si ridimensiona proporzionalmente, mantenendo un aspect ratio perfetto e l'integrità del layout a qualsiasi dimensione della finestra.

## Decisioni di Design

- **Architettura MVC:** Il codice aderisce rigorosamente al pattern di progettazione Model-View-Controller. La logica per l'elaborazione audio e la gestione dello stato è completamente separata dal rendering dell'interfaccia utente e dalle interazioni dell'utente.
- **Componenti JavaFX Custom:** Invece di affidarsi esclusivamente ai controlli predefiniti di JavaFX, SimConsole utilizza componenti pesantemente personalizzati (es. `LedButton`, `KnobBase`, `DynamicDeck`, `MasterSlider`). Questi componenti utilizzano Skin JavaFX custom per renderizzare un equipaggiamento da studio skeuomorfico ed estremamente realistico.
- **Grafica Vettoriale (SVG):** Per garantire un rendering perfetto e coerente su tutte le piattaforme e una scalabilità infinita, tutte le icone (come quelle di ricerca o della cartella) sono implementate utilizzando percorsi SVG iniettati direttamente tramite CSS (`-fx-shape`), piuttosto che fare affidamento su emoji incostanti del sistema operativo o su immagini rasterizzate.
- **Ridimensionamento Dinamico del Layout e dei Font:** L'interfaccia utente evita vincoli rigidi. Le dimensioni dei font e le larghezze delle colonne nei pannelli della playlist e degli effetti sono legate matematicamente alle dimensioni del loro contenitore padre. Questo crea un effetto di ridimensionamento "come una fotografia", in cui nulla viene mai tagliato o troncato.
- **Pipeline Audio Grezza:** L'`AudioProcessor` manipola i byte audio raw PCM in tempo reale, eseguendo operazioni matematiche DSP (Digital Signal Processing) frame per frame prima di inviare il buffer mixato finale alla linea di output audio.

## Struttura Interna

Il codice sorgente è organizzato in una struttura a moduli pulita sotto il package `org.simconsole.simconsole`:

- **`controllers/`**: Contiene tutte le classi Controller di JavaFX. Queste classi gestiscono gli input dell'utente, i binding dei file FXML e sincronizzano l'interfaccia utente con i modelli sottostanti.
- **`models/`**: Il core della logica di business e del motore audio. Questo package contiene l'`AudioProcessor`, i filtri di segnale digitale (`BiquadFilter`, `Reverb`, `Echo`, `Flanger`), i modelli delle tracce e il `MusicApiService` per il recupero delle tracce online.
- **`components/`**: Ospita tutti i nodi dell'interfaccia utente JavaFX custom e le rispettive implementazioni delle `Skin`. Qui è definita la rappresentazione visiva di manopole, fader e piatti del giradischi.
- **`models/audio/`**: Contiene utilità specifiche per la lettura dei file WAV e l'interfacciamento con motori di elaborazione audio esterni.

## Esecuzione dell'Applicazione

Questo progetto utilizza Maven. Puoi compilare ed eseguire l'applicazione lanciando il seguente comando nel terminale:

```bash
./mvnw clean javafx:run
```
