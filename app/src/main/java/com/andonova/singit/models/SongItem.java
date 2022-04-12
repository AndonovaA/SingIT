package com.andonova.singit.models;

public class SongItem {

    private String songName;

    public SongItem(String songName) {
        this.songName = songName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }
}
