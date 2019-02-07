package com.example.snapchat_clone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    public void onClick (View view) {
        Button buttonPressed = (Button) view;
        Log.i("Button Pressed", buttonPressed.getText().toString());
        Intent intent = new Intent (getApplicationContext(), AuthActivity.class);
        intent.putExtra("function", buttonPressed.getText().toString());
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // to check if there is a user currently signed in -> will go straight to useractivity
                FirebaseUser currentuser = firebaseAuth.getCurrentUser();
                if (currentuser != null) {
                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                    startActivity(intent);
                    Log.i("User", "there is currently a user logged in");
                }
            }
        };
    }

    /*
        Enables fullscreen mode in the app
        - hides the notification bar and navigation bar
        - will show again if you swipe down from the top
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    /*
    Sign out any user that may be logged in on the start of the app
 */
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and if there a user already signed in to go straight to the useractivity page
        mAuth.addAuthStateListener(mAuthListener);

    }
}
