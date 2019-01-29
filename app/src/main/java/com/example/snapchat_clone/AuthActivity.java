package com.example.snapchat_clone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    EditText emailText;
    EditText passwordText;
    Button loginButton;
    ImageView backImage;
    TextView titleText;

    private FirebaseAuth mAuth;

    /*
        OnClick method for button
     */
    public void onClick (View view) {
        Button buttonPressed = (Button) view;
        String buttonText = buttonPressed.getText().toString();

        if (buttonText.equals("LOG IN")) {
            Log.i("Auth", "LOG IN");

            // Logging in existing users
            mAuth.signInWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AuthActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AuthActivity.this, "Could Not Log In :(", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } else if (buttonText.equals("SIGN UP")) {
            Log.i("Auth", "SIGN UP");

            // Signing up new users
            mAuth.createUserWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AuthActivity.this, "Signed Up Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AuthActivity.this, "Could Not Sign Up :(", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);
        backImage = findViewById(R.id.backImage);
        titleText = findViewById(R.id.titleText);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        /*
            OnClickListener for the back button to back to the home screen
         */
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // Disable the log in button in app create
        loginButton.setEnabled(false);

        // Setting the text of the title and button depending if log in or sign up was clicked
        Intent intent = getIntent();
        String function = intent.getStringExtra("function");
        loginButton.setText(function);
        titleText.setText(function);


        /*
            EditText listener to enable/disable the log in button depending on if the EditTexts are filled out or not
        */
        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().trim().length() > 0 && !emailText.getText().toString().equals("")) {
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !emailText.getText().toString().equals("")) {
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        emailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().trim().length() > 0 && !passwordText.getText().toString().equals("")) {
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !passwordText.getText().toString().equals("")) {
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
}
