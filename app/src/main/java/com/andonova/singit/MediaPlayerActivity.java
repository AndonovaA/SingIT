package com.andonova.singit;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.andonova.singit.LrcView.ILrcBuilder;
import com.andonova.singit.LrcView.ILrcView;
import com.andonova.singit.LrcView.LrcBuilder;
import com.andonova.singit.LrcView.LrcRow;
import com.andonova.singit.databinding.ActivityMediaPlayerBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MediaPlayerActivity extends AppCompatActivity {

    public final static String TAG = "MediaPlayerActivity";
    ActivityMediaPlayerBinding binding;

    ILrcView mLrcView;
    MediaPlayer mPlayer;
    private final int mPlayTimerDuration = 1000;
    private Timer mTimer;
    private TimerTask mTask;

    private String currentSongName;
    private Uri currentSongUrl;


    public String getFromAssets(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            StringBuilder Result = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                if (line.trim().equals(""))
                    continue;
                Result.append(line).append("\r\n");
            }
            return Result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent i = getIntent();
        if (i != null) {
            Bundle extras = i.getExtras();
            if (extras != null) {
                String songName = extras.getString("songItemName");
                String songUrl = extras.getString("songItemUrl");
                if (songName != null && songUrl != null) {
                    currentSongName = songName;
                    currentSongUrl = Uri.parse(songUrl);
                }
            }
        }

        // mLrcView = new LrcView(this, null);
        // setContentView((View) mLrcView);
        mLrcView = binding.lrcLyricsView;
        binding.textLyricsView.setVisibility(View.GONE);

        String lrc = getFromAssets("test.lrc");
        Log.d(TAG, "lrc:" + lrc);

        ILrcBuilder builder = new LrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(lrc);

        mLrcView.setLrc(rows);
        beginLrcPlay();

        mLrcView.setListener((newPosition, row) -> {
            if (mPlayer != null) {
                Log.d(TAG, "onLrcSought:" + row.time);
                mPlayer.seekTo((int) row.time);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }


    public void beginLrcPlay() {

        mPlayer = new MediaPlayer();
        mPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
        try {
            mPlayer.setDataSource(this, Uri.parse("https://firebasestorage.googleapis.com/v0/b/singit-98387.appspot.com/o/qjQ2S4VeiTbQSZCKtgJxNBfkYHh2%2Fsongs%2FThe%20Beatles%20%20%20Something.mp3?alt=media&token=de14e175-ca70-4c5b-92c8-d1aa152b64f8"));
            mPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "onPrepared");
                mp.start();
                if (mTimer == null) {
                    mTimer = new Timer();
                    mTask = new LrcTask();
                    mTimer.scheduleAtFixedRate(mTask, 0, mPlayTimerDuration);
                }
            });
            mPlayer.setOnCompletionListener(mp -> stopLrcPlay());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IllegalArgumentException | IOException | IllegalStateException e) {
            e.printStackTrace();
        }

    }

    public void stopLrcPlay() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    class LrcTask extends TimerTask {

        long beginTime = -1;

        @Override
        public void run() {
            if (beginTime == -1) {
                beginTime = System.currentTimeMillis();
            }

            final long timePassed = mPlayer.getCurrentPosition();
            MediaPlayerActivity.this.runOnUiThread(() -> mLrcView.seekLrcToTime(timePassed));

        }
    }
}