package com.andonova.singit.models;

import android.net.Uri;

public class SongItem {

    private String songName;
    private Uri songHTTPurl;

    public SongItem(String songName, Uri songHTTPurl) {
        this.songName = songName;
        this.songHTTPurl = songHTTPurl;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public Uri getSongHTTPurl() {
        return songHTTPurl;
    }

    public void setSongHTTPurl(Uri songHTTPurl) {
        this.songHTTPurl = songHTTPurl;
    }
}
