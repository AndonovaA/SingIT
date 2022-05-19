package com.andonova.singit.LrcView;

import java.util.List;


/**
 * Use ILrcView to display lyric, seek and scale.
 * Copyright 2012 (c) Xiaoyuan Lau
 */
public interface ILrcView {

    /**
     * set the lyric rows to display
     */
    void setLrc(List<LrcRow> lrcRows);

    /**
     * seek lyric row to special time
     *
     * @time time to be seek
     */
    void seekLrcToTime(long time);

    void setListener(LrcViewListener l);

    interface LrcViewListener {

        /**
         * when lyric line was seeked by user
         */
        void onLrcSeeked(int newPosition, LrcRow row);
    }
}
