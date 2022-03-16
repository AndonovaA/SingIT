package com.andonova.singit;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.andonova.singit.databinding.ActivityLoginBinding;


public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setEventListeners();
    }

    private void setEventListeners() {

        binding.goToSignup.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class));
        });
    }
}