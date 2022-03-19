package com.andonova.singit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;
import com.andonova.singit.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //user input values
    private String email = " ";
    private String password = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        setEventListeners();
    }

    private void setEventListeners() {

        binding.goToSignup.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        binding.loginBtn.setOnClickListener(view -> {
            validateData();
        });

        binding.forgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    /***
     * Checking the input data before registering a user.
     */
    private void validateData() {
        email = binding.emailEditText.getText().toString().trim();
        password = binding.passwordEditText.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEditText.setError("Please provide valid email.");
        }
        else if(TextUtils.isEmpty(password)){
            binding.passwordEditText.setError("Please provide password.");
        }
        else{
            loginUser();
        }
    }

    /***
     * Login user with firebase authentication.
     */
    private void loginUser() {

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    //login success, check if the user is "user" or "admin"
                    checkUserType();
                })
                .addOnFailureListener(e -> {
                    //login failed
                    Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /***
     * Checks if the logged user is "user" or "admin".
     */
    private void checkUserType() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user type
                        String userType = "" + snapshot.child("userType").getValue();
                        //check user type
                        if (userType.equals("user")){
                            Log.d("LoginActivity", "user logged!");
                            // open Songs Library
                            startActivity(new Intent(LoginActivity.this, LibraryActivity.class));
                            finish();
                        }
                        else if(userType.equals("admin")){
                            //TODO: open admin page
                            Log.d("LoginActivity", "admin logged!");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}