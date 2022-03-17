package com.andonova.singit;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import com.andonova.singit.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //user input values
    private String name = " ";
    private String email = " ";
    private String password = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        setEventListeners();
    }

    private void setEventListeners() {

        binding.goToLogin.setOnClickListener(view -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        binding.signupBtn.setOnClickListener(view -> {
            validateData();
        });
    }

    /***
     * Checking the input data before registering a user.
     */
    private void validateData() {
        name = binding.usernameEditText.getText().toString().trim();
        email = binding.emailEditText.getText().toString().trim();
        password = binding.passwordEditText.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            binding.usernameEditText.setError("Please provide username.");
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEditText.setError("Please provide valid email.");
        }
        else if(TextUtils.isEmpty(password)){
            binding.passwordEditText.setError("Please provide password.");
        }
        else if(password.length() < 6){
            binding.passwordEditText.setError("Password must have minimum 6 characters.");
        }
        else{
            createUserAccount();
        }
    }

    /***
     * Create user account with firebase authentication.
     */
    private void createUserAccount() {

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    addUserToDatabase();
                    // TODO: check if we need to add the user to a realtime database
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addUserToDatabase() {

        //timestamp for a database record
        long timestamp = System.currentTimeMillis();

        //get current user id, since the user is registered
        String uid = firebaseAuth.getUid();

        //setup data for user
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);         // user id
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("userType", "user"); // possible values: "user", "admin"
        hashMap.put("timestamp", timestamp);

        //set user data to the realtime database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(MainActivity.this, "User has been registered successfully!", Toast.LENGTH_SHORT).show();
                    // go to login page
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}