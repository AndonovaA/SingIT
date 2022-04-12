package com.andonova.singit;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(this::checkUserCredentials, 2000);
    }

    private void checkUserCredentials(){
        //get the current user if logged it
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            //the user is not logged in => go to sing up screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else {
            //the user is already logged => check the user type
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //get user type
                            String userType = "" + snapshot.child("userType").getValue();
                            //check user type
                            if (userType.equals("user")){
                                Log.d("SplashActivity", "user " + firebaseUser.getUid() + " logged!");
                                // open Songs Library
                                startActivity(new Intent(SplashActivity.this, LibraryActivity.class));
                                finish();
                            }
                            else if(userType.equals("admin")){
                                //TODO: open admin page
                                Log.d("SplashActivity", "admin logged!");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SplashActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}