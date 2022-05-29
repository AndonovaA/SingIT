package com.andonova.singit.helpers;

import com.andonova.singit.models.SongItem;

import java.util.List;

/**
 * Singleton class for storing the retrieved song items from the firebase storage.
 */
public class Common {

    static Common common;

    public static Common getInstance() {
        if (common == null) {
            common = new Common();
        }
        return common;
    }

    public List<SongItem> songs;
}
