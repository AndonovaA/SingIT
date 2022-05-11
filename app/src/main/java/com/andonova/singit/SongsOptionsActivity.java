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
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.andonova.singit.databinding.ActivitySongsOptionsBinding;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;


public class SongsOptionsActivity extends AppCompatActivity {

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

                try {
                    InputStream inputStream = new FileInputStream(fullPath);
                    byte[] songBytes = getBytesFromInputStream(inputStream);
                    String songBytesString = Base64.getEncoder().encodeToString(songBytes);

                    Python mPython = Python.getInstance();
                    PyObject pythonFile = mPython.getModule("removeVocals");
                    PyObject result = pythonFile.callAttr("main", songBytesString);
                    String instrumentalSongBytesString = result.toString();
                    byte[] instrumentalSongBytes = Base64.getDecoder().decode(instrumentalSongBytesString);
                    // TODO: upload instrumental song to firebase storage,
                    // but first put python script in a background thread !!!

                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
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

}