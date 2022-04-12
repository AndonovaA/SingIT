package com.andonova.singit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andonova.singit.adapters.SongsRecyclerAdapter;
import com.andonova.singit.databinding.ActivityLibraryBinding;
import com.andonova.singit.models.SongItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.FilenameUtils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class LibraryActivity extends AppCompatActivity {

    String TAG = "LibraryActivity";

    ActivityLibraryBinding binding;

    // Object of the Firebase Storage
    private StorageReference storageRef;
    // Object of the Adapter class
    SongsRecyclerAdapter adapter;
    List<SongItem> songsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setComponents();
        setEventListeners();
    }

    private void init() {
        //initialize firebase auth
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        //get the current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        // Set up Cloud Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (firebaseUser == null) {
            redirectToLoginPage();
        } else {
            // Create a Cloud Storage reference from the app fpr the specific user if logged
            storageRef = storage.getReference().child(firebaseUser.getUid());

            // Initializing recycler view adapter
            songsList = new ArrayList<>();
            if (storageRef != null) {
                storageRef.listAll()
                        .addOnSuccessListener(listResult -> {
                            for (StorageReference item : listResult.getItems()) {
                                // All the items under storageRef
                                String songName = FilenameUtils.removeExtension(item.getName());
                                songsList.add(new SongItem(songName));
                            }
                            adapter = new SongsRecyclerAdapter(songsList, binding.getRoot().getContext());
                            binding.recyclerView.setAdapter(adapter);
                        })
                        .addOnFailureListener(e -> Log.d(TAG, e.getMessage()));
            } else {
                // Display no songs
                adapter = new SongsRecyclerAdapter(songsList, binding.getRoot().getContext());
                binding.recyclerView.setAdapter(adapter);
            }
        }
    }

    private void setComponents() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                binding.recyclerView.getContext(),
                ((LinearLayoutManager) mLayoutManager).getOrientation()
        );
        binding.recyclerView.addItemDecoration(dividerItemDecoration);
    }


    private void setEventListeners() {

        binding.addSong.setOnClickListener(view -> {
            startActivity(new Intent(LibraryActivity.this, SongsOptionsActivity.class));
        });
    }

    private void redirectToLoginPage() {
        // When no user is logged, redirect to Login page
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}