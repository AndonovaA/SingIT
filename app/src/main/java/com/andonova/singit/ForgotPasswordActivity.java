package com.andonova.singit;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import com.andonova.singit.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;


public class ForgotPasswordActivity extends AppCompatActivity {

    ActivityForgotPasswordBinding binding;
    private String email = " ";

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        setEventListeners();
    }

    private void setEventListeners() {
        binding.submitBtn.setOnClickListener(view -> {

            email = binding.emailEditText.getText().toString().trim();
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.emailEditText.setError("Please provide valid email.");
            }
            else{
                recoverPassword();
            }
        });
    }

    private void recoverPassword(){
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                        Toast.makeText(ForgotPasswordActivity.this, "Instructions sent to "+email, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                        finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ForgotPasswordActivity.this, "Failed!", Toast.LENGTH_SHORT).show());
    }
}