package com.andonova.singit.helpers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.loader.content.AsyncTaskLoader;

import com.andonova.singit.SongsOptionsActivity;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class ConvertToKaraoke extends AsyncTaskLoader<HashMap<String, Boolean>> {

    private static final String TAG = "ConvertToKaraoke";
    private final SongsOptionsActivity mActivity;
    private final String mSongURI;


    public ConvertToKaraoke(@NonNull Context context, String songURI, SongsOptionsActivity activity) {
        super(context);
        mSongURI = songURI;
        mActivity = activity;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public HashMap<String, Boolean> loadInBackground() {

        boolean isSongConverted = songToInstrumental();
        Log.d(TAG, "loadInBackground: songToInstrumental() finished. Result: " + isSongConverted);
        boolean isLyricDownloaded = downloadLyrics(false);
        Log.d(TAG, "loadInBackground: downloadLyrics() finished.Result: " + isLyricDownloaded);

        HashMap<String, Boolean> converted = new HashMap<>();
        converted.put("isSongConverted", isSongConverted);
        converted.put("isLyricDownloaded", isLyricDownloaded);
        return converted;
    }

    /**
     * @return
     */
    private boolean songToInstrumental() {

        try {
            InputStream inputStream = new FileInputStream(mSongURI);
            byte[] songBytes = getBytesFromInputStream(inputStream);
            String songBytesString = Base64.getEncoder().encodeToString(songBytes);

            Python mPython = Python.getInstance();
            PyObject pythonFile = mPython.getModule("removeVocals");
            PyObject result = pythonFile.callAttr("main", songBytesString);
            String instrumentalSongBytesString = result.toString();
            byte[] instrumentalSongBytes = Base64.getDecoder().decode(instrumentalSongBytesString);
            // Upload instrumental song to firebase storage:
            return uploadSongToFirebaseStorage(instrumentalSongBytes);

        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
    }

    /***
     *
     * @return
     */
    private boolean downloadLyrics(boolean notLrc) {

        String lyric;
        String lyricType;

        String songFileName = getSongName();
        if (notLrc) {
            lyric = LyricsDownloader.textLyrics(songFileName);
            lyricType = "txt";
        } else {
            lyric = LyricsDownloader.findLyrics(songFileName).get("lyrics");
            lyricType = LyricsDownloader.findLyrics(songFileName).get("type");
        }

        if (lyric != null && !lyric.equals("")) {
            // Upload the lyric to firebase storage in format of a txt file:
            return uploadLyricsToFirebaseStorage(lyric, songFileName, lyricType);
        }

        return false;
    }


    private String getSongName() {
        String[] pathElements = mSongURI.split("/");
        String fileName = pathElements[pathElements.length - 1];
        return fileName.substring(0, fileName.lastIndexOf('.')).replaceAll("[^A-Za-z0-9]", " ");
    }


    /**
     * @param is For reading bytes from audio file.
     * @return Byte array with the bytes from the audio file.
     * @throws IOException If the buffer is empty.
     */
    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }


    private boolean uploadSongToFirebaseStorage(byte[] songBytes) {

        String songName = getSongName();
        //initialize firebase auth
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        //get the current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            // Set up Cloud Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child(firebaseUser.getUid());
            StorageReference songsStorageReferenceSongs = storageRef.child("songs");
            // Create a reference to "user_id/songs/song_name.mp3"
            StorageReference songRef = songsStorageReferenceSongs.child(songName + ".mp3");

            UploadTask uploadTask = songRef.putBytes(songBytes);
            try {
                Tasks.await(uploadTask, 10, TimeUnit.MINUTES);
                Log.d(TAG, "Song uploaded successfully!");
                return true;

            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                Log.d(TAG, "Song failed to upload: " + e.getMessage());
            }
        }
        return false;
    }


    private boolean uploadLyricsToFirebaseStorage(String lyric, String songName, String lyricType) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child(firebaseUser.getUid());
            StorageReference lyricsStorageReferenceSongs = storageRef.child("lyrics");
            StorageReference lyricsRef = lyricsStorageReferenceSongs.child(songName + ".txt");

            UploadTask uploadTask = lyricsRef.putBytes(lyric.getBytes(StandardCharsets.UTF_8));
            try {
                Tasks.await(uploadTask, 5, TimeUnit.MINUTES);
                Log.d(TAG, "Lyrics uploaded successfully!");

                // Create file metadata
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setCustomMetadata("lyricType", lyricType)
                        .build();
                // Update metadata properties
                Task<StorageMetadata> uploadMetadataTask = lyricsRef.updateMetadata(metadata);
                Tasks.await(uploadMetadataTask, 3, TimeUnit.MINUTES);
                return true;

            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                Log.d(TAG, "Lyrics failed to upload or(and) couldn't update metadata: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
}
