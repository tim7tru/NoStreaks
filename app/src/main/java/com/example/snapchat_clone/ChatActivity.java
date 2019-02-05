package com.example.snapchat_clone;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

	// UI Declarations
	TextView titleTextView;
	ImageView backImageView;
	ListView messageListView;
	EditText messageEditText;
	Button sendButton;

	// Firebase Declarations
	FirebaseAuth mAuth = FirebaseAuth.getInstance();
	FirebaseDatabase database = FirebaseDatabase.getInstance();
	DatabaseReference mRootRef = database.getReference();

	// MISC Declarations
	String message, uniqueKey,currentUserUID, clickedUserDisplay, currentUserDisplay, clickedUserUID;   // USER INFO
	ArrayList<String> messages;                                                                         // Messages for the listView
	ArrayAdapter<String> arrayAdapter;                                                                  // ArrayAdapter for the listView
	boolean closing;                                                                                    // Switches only if the user presses back


	// When the "SEND" button is pressed, takes editText and sends it to the database
	// Database - Stores into: Clicked User's Received & Current User's Sent
	// Updates the text
	public void sendMessage(View view) {
		if (!messageEditText.getText().toString().isEmpty()) {
			message = messageEditText.getText().toString();
			uniqueKey = mRootRef.push().getKey();
			// Clicked User's Store
			mRootRef.child("Users").child(clickedUserDisplay).child("receivedMessages").child(currentUserUID).child(uniqueKey).setValue(message);
			// Current User's Store
			mRootRef.child("Users").child(currentUserDisplay).child("sentMessages").child(clickedUserUID).child(uniqueKey).setValue(message);
			messageEditText.setText("");
			updateList();
		} else {
			Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
		}
	}

	// Updates the listView onCreate and when a message is sent
	// OnCreate - Takes all received messages at the user and displays
	// sendMessage - Takes all received messages at the user AND the one(s) that were just sent then displays them
	public void updateList() {
		// Clears the arrayList before anything else
		messages.clear();

		// QUERY TO GET ALL MESSAGES
		Query messagesQuery =  mRootRef.child("Users").orderByChild("displayName").equalTo(clickedUserDisplay);
		messagesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					// ALL RECEIVED MESSAGES FIRST (Users/clickedUser/sentMessages/currentUID/messages)
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						for (DataSnapshot children : snapshot.child("sentMessages").child(currentUserUID).getChildren()) {
							messages.add(children.getValue(String.class));
						}
					}

					// UPDATES THE LISTVIEW
					arrayAdapter = new ArrayAdapter<>(ChatActivity.this, android.R.layout.simple_list_item_1, messages);
					messageListView.setAdapter(arrayAdapter);

					// GETS THE MESSAGES THAT WERE JUST SENT (Users/clickedUser/receivedMessages/currentUID/messages)
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						for (DataSnapshot children : snapshot.child("receivedMessages").child(currentUserUID).getChildren()) {
							messages.add(children.getValue(String.class));
						}
					}
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) { }
		});
	}

	// RUNS ONLY WHEN THE BACK BUTTON IS PRESSED
	// DELETES THE CORRECT MESSAGES FROM THE DATABASE
	// Deletes ALL from:
	//      Users/clickedUser/sentMessages/currentUID/messages
	//      Users/currentUser/receivedMessages/clickedUID/messages
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.backImageView:
				closing = true;
				messages.clear();

				// Deletes ClickedOn Sent (Users/clickedUser/sentMessages/currentUID/messages)
				Query deleteSenderQuery = mRootRef.child("Users").orderByChild("displayName").equalTo(clickedUserDisplay);
				deleteSenderQuery.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if (closing) {
							if (dataSnapshot.exists()) {
								for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
									for (DataSnapshot children : snapshot.child("sentMessages").child(currentUserUID).getChildren()) {
										String key = children.getKey();
										mRootRef.child("Users").child(clickedUserDisplay).child("sentMessages").child(currentUserUID).child(key).removeValue();
									}
								}
							}
						}
					}
					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {}
				});

				// Deletes Current Received (Users/currentUser/receivedMessages/clickedUID/messages)
				Query deleteReceiverQuery = mRootRef.child("Users").orderByChild("displayName").equalTo(currentUserDisplay);
				deleteReceiverQuery.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if (closing) {
							if (dataSnapshot.exists()) {
								for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
									for (DataSnapshot children : snapshot.child("receivedMessages").child(clickedUserUID).getChildren()) {
										String key = children.getKey();
										mRootRef.child("Users").child(currentUserDisplay).child("receivedMessages").child(clickedUserUID).child(key).removeValue();
									}
								}
							}
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {}
				});

				// GOES BACK TO USER ACTIVITY
				finish();
				break;
			default:
				break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		Intent intent = getIntent();

		// Defining user's info
		currentUserUID = mAuth.getCurrentUser().getUid();
		clickedUserDisplay = intent.getStringExtra("userClicked");
		currentUserDisplay = UserActivity.username;

		// Defining UI elmeents
		messageEditText = findViewById(R.id.messageEditText);
		messageListView = findViewById(R.id.messageListView);
		sendButton = findViewById(R.id.sendButton);
		backImageView = findViewById(R.id.backImageView);
		backImageView.setOnClickListener(this);
		titleTextView = findViewById(R.id.titleTextView1);
		titleTextView.setText(clickedUserDisplay);

		// MISC Definitions
		messages = new ArrayList<>();                                                                       // Initialize arraylist
		arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);     // Initialize arrayAdapter
		closing = false;                                                                                    // Default the close to false

		// QUERY to get the clicked on user's UID
		// UPDATES THE LISTVIEW INITALLY
		Query receiverUIDQuery = mRootRef.child("Users").orderByChild("displayName").equalTo(clickedUserDisplay);
		receiverUIDQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						clickedUserUID = snapshot.child("uid").getValue(String.class);
					}
					updateList();
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) { }
		});

		// Methods to make the keyboard show on start
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(messageEditText, 0);
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
