/**
 * Log in/ sign up for the app
 */

package com.example.snapchat_clone;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AuthActivity extends AppCompatActivity {

    EditText emailText;
    EditText passwordText;
    Button loginButton;
    ImageView backImage;
    TextView titleText, displayNameTextView, loginNameTextView;
    EditText displayNameEditText;
    public static FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
	DatabaseReference mRootRef = database.getReference();
	DatabaseReference mUsersRef = mRootRef.child("Users");
    /*
        OnClick method for button
     */
    public void onClick (View view) {
        Button buttonPressed = (Button) view;
        String buttonText = buttonPressed.getText().toString();

        if (buttonText.equals("LOG IN")) {
            // Logging in existing users

	        // If an email is inputted to the login edit text
            if (emailText.getText().toString().contains("@")) {
            	// sign in method with email and password
                mAuth.signInWithEmailAndPassword(emailText.getText().toString().toLowerCase(), passwordText.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AuthActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(AuthActivity.this, "Could Not Log In :(", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            // If a username is inputted into the login edit text
            else {
            	/*
            	 * No firebase default method to log in with a username
            	 * Therefore must get email using username as a query
            	 */
            	final String inputUsername = emailText.getText().toString().toLowerCase();

            	// Query to get email
	            Query nameQuery = mUsersRef.orderByChild("displayName").equalTo(inputUsername);
	            nameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
		            @Override
		            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
			            if (dataSnapshot.exists()) {
			            	String email = "";
			            	// Getting the email
			            	for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
			            		email = (String) snapshot.child("email").getValue();
				            }

				            // Logging in with the queried email
				            mAuth.signInWithEmailAndPassword(email.toLowerCase(), passwordText.getText().toString()).addOnCompleteListener(AuthActivity.this, new OnCompleteListener<AuthResult>() {
					            @Override
					            public void onComplete(@NonNull Task<AuthResult> task) {
						            if (task.isSuccessful()) {
							            Toast.makeText(AuthActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
							            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
							            startActivity(intent);
						            } else {
							            Toast.makeText(AuthActivity.this, "Could Not Log In :(", Toast.LENGTH_SHORT).show();
						            }
					            }
				            });
			            } else {
							Toast.makeText(AuthActivity.this, "There is no account under this username.", Toast.LENGTH_SHORT).show();
			            }
		            }
		            @Override
		            public void onCancelled(@NonNull DatabaseError databaseError) { }
	            });
            }

        } else if (buttonText.equals("SIGN UP")) {
            Log.i("Auth", "SIGN UP");
            displayNameTextView.setVisibility(View.VISIBLE);
            displayNameEditText.setVisibility(View.VISIBLE);
            loginNameTextView.setText(R.string.email);

            // Query to check if the inputted username is already taken!
            Query nameQuery = mUsersRef.orderByChild("displayName").equalTo(displayNameEditText.getText().toString());
            nameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
	            @Override
	            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
		            if (dataSnapshot.exists()) {
		            	// If the query finds a result, the username is already in the database and is taken
		            	Toast.makeText(AuthActivity.this, "This username already exists, try another.", Toast.LENGTH_SHORT).show();
		            } else {
		            	// if there is no result, sign up the user!
			            mAuth.createUserWithEmailAndPassword(emailText.getText().toString().toLowerCase(), passwordText.getText().toString())
					            .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<AuthResult>() {
						            @Override
						            public void onComplete(@NonNull Task<AuthResult> task) {
							            if (task.isSuccessful()) {
								            User user = new User(displayNameEditText.getText().toString().toLowerCase(), emailText.getText().toString().toLowerCase(), FirebaseAuth.getInstance().getUid());
								            database.getReference("Users").child(displayNameEditText.getText().toString().toLowerCase()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
									            @Override
									            public void onComplete(@NonNull Task<Void> task) {
										            if (task.isSuccessful()) {
											            Toast.makeText(AuthActivity.this, "Signed Up Successfully", Toast.LENGTH_SHORT).show();
											            Intent intent = new Intent (getApplicationContext(), UserActivity.class);
											            startActivity(intent);
										            } else {
											            Toast.makeText(AuthActivity.this, "There was a problem signing up!", Toast.LENGTH_SHORT).show();
										            }
									            }
								            });
							            } else {
								            Toast.makeText(AuthActivity.this, "Could Not Sign Up :(", Toast.LENGTH_SHORT).show();
							            }
						            }
					            });
		            }
	            }
	            @Override
	            public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // initializing widgets
        displayNameEditText = findViewById(R.id.displayNameEditText);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);
        backImage = findViewById(R.id.backImage);
        titleText = findViewById(R.id.titleText);
        displayNameTextView = findViewById(R.id.displayNameTextView);
        loginNameTextView = findViewById(R.id.loginNameTextView);

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

        // Disable the log in button on app create
        loginButton.setEnabled(false);

        // Setting the text of the title and button depending if log in or sign up was clicked
        Intent intent = getIntent();
        String function = intent.getStringExtra("function");
        if (function.equals("LOG IN")) {
            loginButton.setText(function);
            displayNameEditText.setVisibility(View.INVISIBLE);
            displayNameTextView.setVisibility(View.INVISIBLE);
            loginNameTextView.setText(R.string.email_or_username);
        } else if (function.equals("SIGN UP")){
            titleText.setText(function);
	        loginButton.setText(function);
	        displayNameTextView.setVisibility(View.VISIBLE);
            displayNameEditText.setVisibility(View.VISIBLE);
            loginNameTextView.setText(R.string.email);
        }

        /*
            EditText listener to enable/disable the log in button depending on if the EditTexts are filled out or not
        */
        displayNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (displayNameEditText.getVisibility() == View.VISIBLE) {
                    if (s.toString().trim().length() > 0 && !emailText.getText().toString().equals("") && !passwordText.getText().toString().equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (displayNameEditText.getVisibility() == View.VISIBLE) {
                    if (s.toString().trim().length() > 0 && !emailText.getText().toString().equals("") && !passwordText.getText().toString().equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (displayNameEditText.getVisibility() == View.VISIBLE) {
                    if (s.toString().trim().length() > 0 && !emailText.getText().toString().equals("") && !displayNameEditText.getText().toString().equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                } else {
                    if (s.toString().trim().length() > 0 && !emailText.getText().toString().equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (displayNameEditText.getVisibility() == View.VISIBLE) {
                    if (s.toString().trim().length() > 0 && !emailText.getText().toString().equals("") && !displayNameEditText.getText().toString().equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                } else {
                    if (s.toString().trim().length() > 0 && !emailText.getText().toString().equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        emailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (displayNameEditText.getVisibility() == View.VISIBLE) {
                    if (s.toString().trim().length() > 0 && !passwordText.getText().toString().equals("") && !displayNameEditText.getText().toString().equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                } else {
                    if (s.toString().trim().length() > 0 && !passwordText.getText().toString().equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (displayNameEditText.getVisibility() == View.VISIBLE) {
                    if (s.toString().trim().length() > 0 && !passwordText.getText().toString().equals("") && !displayNameEditText.getText().toString().equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                } else {
                    if (s.toString().trim().length() > 0 && !passwordText.getText().toString().equals("")) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
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
