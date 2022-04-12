package com.andonova.singit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.andonova.singit.databinding.ActivitySongsOptionsBinding;


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
                        // example for AudioUri: content://com.android.providers.downloads.documents/document/374
                        // TODO: start converting the uploaded song to karaoke
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

    private void uploadAudioFileFromPhone() {
        Intent audio = new Intent();
        audio.setType("audio/*");
        audio.setAction(Intent.ACTION_OPEN_DOCUMENT);
        mStartForResult.launch(Intent.createChooser(audio, "Select Audio"));
    }

}