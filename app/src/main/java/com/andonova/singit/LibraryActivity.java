package com.andonova.singit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andonova.singit.adapters.SongsRecyclerAdapter;
import com.andonova.singit.databinding.ActivityLibraryBinding;
import com.andonova.singit.helpers.Common;
import com.andonova.singit.models.SongItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.FilenameUtils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class LibraryActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    String TAG = "LibraryActivity";
    ActivityLibraryBinding binding;

    // Object of the Firebase Storage
    private StorageReference storageRef;
    // Object of the Adapter class
    SongsRecyclerAdapter adapter;
    List<SongItem> songsList;
    SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);

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
            // Create a Cloud Storage reference from the app for the specific user if logged
            storageRef = storage.getReference().child(firebaseUser.getUid());

            // Initializing recycler view adapter
            songsList = new ArrayList<>();
            adapter = new SongsRecyclerAdapter(songsList, binding.getRoot().getContext());
            binding.recyclerView.setAdapter(adapter);

            if (storageRef != null) {
                fetchSongsFromFirebaseStorage();
            }
            Intent convertingSong = getIntent();
            if (convertingSong != null) {
                Bundle extras = convertingSong.getExtras();
                if (extras != null) {
                    String loadingSongName = extras.getString("loadingSong");
                    if (loadingSongName != null) {
                        // TODO: add item to the adapter with loadingSongName and a progress bar
                        // loadingSongName is the song which is currently converting to instrumental
                        // at the end refresh the adapter
                        Toast.makeText(this, "Song in progress: " + loadingSongName, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        // SwipeRefreshLayout
        mSwipeRefreshLayout = binding.swipe;
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void fetchSongsFromFirebaseStorage() {
        songsList = new ArrayList<>();
        StorageReference songsStorageReference = storageRef.child("songs");
        songsStorageReference.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        String songName = FilenameUtils.removeExtension(item.getName());
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            songsList.add(new SongItem(songName, uri));
                            songsList.sort((lsongItem, rsongItem) ->
                                    lsongItem.getSongName().compareToIgnoreCase(rsongItem.getSongName()));
                            adapter.updateList(songsList);
                            // Save the songs in array from the singleton class Common
                            Common.getInstance().songs = new ArrayList<>();
                            Common.getInstance().songs.addAll(songsList);
                        }).addOnFailureListener(e -> Log.d(TAG, e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.d(TAG, e.getMessage()));
    }

    private void setComponents() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                binding.recyclerView.getContext(),
                mLayoutManager.getOrientation()
        );
        binding.recyclerView.addItemDecoration(dividerItemDecoration);
    }


    private void setEventListeners() {

        binding.addSong.setOnClickListener(view -> startActivity(new Intent(LibraryActivity.this, SongsOptionsActivity.class)));
    }

    private void redirectToLoginPage() {
        // When no user is logged, redirect to Login page
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                // TODO: search through the recycler view
                return true;
            case R.id.settings:
                startActivity(new Intent(this, AccountSettingsActivity.class));
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        fetchSongsFromFirebaseStorage();
        mSwipeRefreshLayout.setRefreshing(false);
    }
}