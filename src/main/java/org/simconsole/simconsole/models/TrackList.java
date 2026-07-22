package org.simconsole.simconsole.models;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.util.Comparator;
/**
 * Manages a named collection of music tracks backed by an observable list for UI binding, sorting, and shuffling.
 */
public class TrackList {
    private String name;
    private ObservableList<Tracks> tracks;
    public TrackList(String name) {
        this.name = name;
        this.tracks = FXCollections.observableArrayList();
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ObservableList<Tracks> getObservableTracks() {
        return tracks;
    }
    public void addTrack(Tracks t) {
        tracks.add(t);
    }
    public void removeTrack(Tracks t) {
        tracks.remove(t);
    }
    public void sortByName() {
        FXCollections.sort(tracks, Comparator.comparing(t -> new File(t.getFilePath()).getName().toLowerCase()));
    }
    public void shuffle() {
        FXCollections.shuffle(tracks);
    }
    public void clear() {
        tracks.clear();
    }
}
