package com.andonova.singit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.andonova.singit.databinding.ActivityAccountSettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AccountSettingsActivity extends AppCompatActivity {

    ActivityAccountSettingsBinding binding;
    String TAG = "AccountSettingsActivity";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        setEventListeners();
    }

    private void setEventListeners() {

        binding.logoutBtn.setOnClickListener(v -> {
            signOut();
        });
    }

    public void getUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
        }
    }

    public void sendPasswordReset(String emailAddress) {
        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email sent.");
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        redirectToLoginPage();
    }

    private void redirectToLoginPage() {
        // When no user is logged, redirect to Login page
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}