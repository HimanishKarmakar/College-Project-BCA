package com.example.musicplayer;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoriteSong {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String songTitle;
    public String songPath;
    public String songDuration;
    public String songArtist;

    public FavoriteSong(String songTitle, String songPath, String songDuration, String songArtist) {
        this.songTitle = songTitle;
        this.songPath = songPath;
        this.songDuration = songDuration;
        this.songArtist = songArtist;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongPath() {
        return songPath;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public String getSongArtist() {
        return songArtist;
    }
}





