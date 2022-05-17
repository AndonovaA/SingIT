package com.andonova.singit;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.andonova.singit.databinding.ActivitySongsOptionsBinding;
import com.andonova.singit.helpers.ConvertToKaraoke;

import java.io.File;
import java.util.HashMap;


public class SongsOptionsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<HashMap<String, Boolean>> {

    String TAG = "SongsOptionsActivity";
    ActivitySongsOptionsBinding binding;

    Uri AudioUri;

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Audio is Picked in format of URI
                        assert data != null;
                        AudioUri = data.getData();
                        Log.d(TAG, AudioUri.getPath());
                        // example for AudioUri: content://com.android.providers.downloads.documents/document/374
                        convertToKaraoke(AudioUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySongsOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setEventListeners();

        //Check if a Loader is running, if yes, then reconnect to it
        if (getSupportLoaderManager().getLoader(0) != null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    private void setEventListeners() {
        binding.goBackBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, LibraryActivity.class));
            finish();
        });

        binding.uploadSongBtn.setOnClickListener(view -> {
            uploadAudioFileFromPhone();
        });
    }

    /**
     * Send intent for picking a song from external memory.
     */
    private void uploadAudioFileFromPhone() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        mStartForResult.launch(Intent.createChooser(intent, "Select Audio"));
    }

    /**
     * Establish connection to a backend that converts a song with audioUri, to karaoke.
     * This is done using AsyncTaskLoader.
     *
     * @param audioUri The Uri to query.
     */
    private void convertToKaraoke(Uri audioUri) {
        String fullPath = getAudioPath(audioUri);
        if (fullPath == null) {
            Toast.makeText(SongsOptionsActivity.this, "Failed to upload song.", Toast.LENGTH_SHORT).show();
        } else {
            File songFile = new File(fullPath);
            if (songFile.exists()) {
                Log.d(TAG, "File exists. Path: " + fullPath);
                Bundle queryBundle = new Bundle();
                queryBundle.putString("songURI", fullPath);
                //start the loader (pass the bundle as parameter):
                getSupportLoaderManager().restartLoader(0, queryBundle, this);
                //open the songs library
                Intent toLibrary = new Intent(SongsOptionsActivity.this, LibraryActivity.class);
                String songName = getSongName(fullPath);
                toLibrary.putExtra("loadingSong", songName);
                startActivity(toLibrary);
                finish();   // TODO: not sure?
            } else {
                Toast.makeText(SongsOptionsActivity.this, "Failed to upload song.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Get an audio file path from a Uri.
     *
     * @param uri The Uri to query.
     */
    private String getAudioPath(Uri uri) {
        String[] data = {MediaStore.Audio.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = 0;
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return null;
    }

    private String getSongName(String songUrl) {
        String[] pathElements = songUrl.split("/");
        String fileName = pathElements[pathElements.length - 1];
        return fileName.substring(0, fileName.lastIndexOf('.')).replaceAll("[^A-Za-z0-9]", " ");
    }


    /*
      Loader Callbacks
     */

    /**
     * The LoaderManager calls this method when the loader is created.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Returns a new ConvertToKaraoke loader containing the song uri.
     */
    @NonNull
    @Override
    public Loader<HashMap<String, Boolean>> onCreateLoader(int id, @Nullable Bundle args) {
        assert args != null;
        return new ConvertToKaraoke(this, args.getString("songURI"), SongsOptionsActivity.this);
    }

    /**
     * Called when the data has been loaded. As parameter, gets Null or OK message.
     * Used for updating the UI.
     *
     * @param loader The loader that has finished
     * @param data   The message response (Null or OK)
     */
    @Override
    public void onLoadFinished(@NonNull Loader<HashMap<String, Boolean>> loader, @NonNull HashMap<String, Boolean> data) {
        // ne stiga do ovaa funkcija
        if (data.getOrDefault("isSongConverted", false) && data.getOrDefault("isLyricDownloaded", false)) {
            Toast.makeText(this, "Successfully converted!", Toast.LENGTH_SHORT).show();
            //TODO: update the recycler view
        } else {
            Toast.makeText(this, "Failed to convert the song!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * In this case there are no variables to clean up when the loader is reset.
     *
     * @param loader The loader that was reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<HashMap<String, Boolean>> loader) {
    }

}