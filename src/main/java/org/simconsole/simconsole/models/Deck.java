package org.simconsole.simconsole.models;
public class Deck {
    private DeckControls controls;
    private double[] audioData;
    private volatile double playhead;
    private volatile boolean isPlaying;
    private volatile boolean scrubbing = false;
    private Tracks currentTrack;
    private volatile double internalVolume;
    public void loadTrack(Tracks track) {
        isPlaying = false;
        double[] newData = track.getAudioData();
        if (newData != null) {
            this.currentTrack = track;
            this.audioData = newData;
            this.playhead = 0.0;
            this.internalVolume = 0.0;
        }
    }
    public void setControls(DeckControls controls) {
        this.controls = controls;
    }
    public DeckControls getControls() {
        return controls;
    }
    public Tracks getCurrentTrack(){return  currentTrack;}
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
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }
    public boolean isScrubbing() {
        return scrubbing;
    }
    public void setScrubbing(boolean scrubbing) {
        this.scrubbing = scrubbing;
    }
    public void seekTrack(double offset) {
        if (audioData == null) return;
        playhead += offset;
        if ((int)playhead % 2 != 0) {
            playhead -= 1;
        }
        if (playhead < 0) {
            playhead = 0;
        } else if (playhead >= audioData.length - 2) {
            playhead = audioData.length - 2;
            isPlaying = false;
        }
    }
    public double[] getAudioData() {
        return audioData;
    }
    public double getPlayheadDouble() {
        return playhead;
    }
    public void setPlayheadDouble(double ph) {
        this.playhead = ph;
    }
    public int getPlayhead() {
        return (int) playhead;
    }
    public double getInternalVolume() {
        return internalVolume;
    }
    public void setInternalVolume(double vol) {
        this.internalVolume = vol;
    }
}
