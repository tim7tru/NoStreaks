package com.example.snapchat_clone;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

	// UI Declarations
	TextView titleTextView;
	ImageView backImageView;
	ListView messageListView;
	EditText messageEditText;
	Button sendButton;
	ConstraintLayout constraint;

	// Firebase Declarations
	FirebaseAuth mAuth = FirebaseAuth.getInstance();
	FirebaseDatabase database = FirebaseDatabase.getInstance();
	DatabaseReference mRootRef = database.getReference();

	// MISC Declarations
	String message, uniqueKey,currentUserUID, clickedUserDisplay, currentUserDisplay, clickedUserUID;   // USERS INFO & MESSAGE
	ArrayList<String> messages, usernames;                                                              // Messages and usernames for the listView
	ArrayList<ChatListItem> chat;
	ChatAdapter chatAdapter;
	boolean closing, firstTime;                                                                         // Switches only if the user presses back

	// When the "SEND" button is pressed, takes editText and sends it to the database
	// Database - Stores into: Clicked User's Received & Current User's Sent
	public void sendMessage(View view) {
		if (!messageEditText.getText().toString().isEmpty()) {
			message = messageEditText.getText().toString();
			uniqueKey = mRootRef.push().getKey();
			// Current User's Store
			mRootRef.child("Users").child(currentUserDisplay).child("sentMessages").child(clickedUserUID).child(uniqueKey).setValue(message);
			// Clicked User's Store
			mRootRef.child("Users").child(clickedUserDisplay).child("receivedMessages").child(currentUserUID).child(uniqueKey).setValue(message);
			messageEditText.setText("");
		} else {
			Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
		}
	}

	// RUNS ONLY WHEN THE BACK BUTTON IS PRESSED
	// DELETES THE CORRECT MESSAGES FROM THE DATABASE
	// Deletes ALL from:
	//      Users/clickedUser/sentMessages/currentUID/messages
	//      Users/currentUser/receivedMessages/clickedUID/messages
	@Override
	public void onClick(View v) {

		// hides keyboard if the listview is clicked
		if (v.getId() == titleTextView.getId() || v.getId() == constraint.getId()) {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}

		switch (v.getId()) {
			case R.id.backImageView:
				closing = true;
				chat.clear();

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

		// Defining UI elements
		messageEditText = findViewById(R.id.messageEditText);
		messageListView = findViewById(R.id.messageListView);
		sendButton = findViewById(R.id.sendButton);
		backImageView = findViewById(R.id.backImageView);
		backImageView.setOnClickListener(this);
		titleTextView = findViewById(R.id.titleTextView1);
		titleTextView.setText(clickedUserDisplay);
		titleTextView.setOnClickListener(this);
		constraint = findViewById(R.id.layout);

		// MISC Definitions
		messages = new ArrayList<>();                               // Initialize arraylist
		usernames = new ArrayList<>();                              // Initialize arraylist
		chat = new ArrayList<>();
		closing = false;                                            // Default the close to false
		firstTime = true;                                           // Default to true everytime the activity is opened

		// QUERY to get the clicked on user's UID
		Query receiverUIDQuery = mRootRef.child("Users").orderByChild("displayName").equalTo(clickedUserDisplay);
		receiverUIDQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						clickedUserUID = snapshot.child("uid").getValue(String.class);
						// UpdateListInitial() - Line: 237
						updateListInitial();
					}
				}

				/*
				 * The following references and childEventListeners are in this method because they require clickedUserUID to run
				 * Returns null if outside of this method - don't know why but dont remove these two references!
				*/

				// Reference to check if there has been any additions to Users/CurrentDisp/ReceivedMessages/ClickedUID
				// Checks for any received messages from the clicked user and displays it
				mRootRef.child("Users").child(currentUserDisplay).child("receivedMessages").child(clickedUserUID).addChildEventListener(new ChildEventListener() {
					@Override
					public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
						if (!firstTime) {
							chat.add(new ChatListItem(clickedUserDisplay, dataSnapshot.getValue(String.class), false));
							// Update() - Line: 262
							update();
						}
					}
					@Override
					public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
					@Override
					public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
					@Override
					public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {}
				});

				// Reference to check if there has been any additions to Users/CurrentDisp/SentMessages/ClickedUID
				// Checks if the user sent any messages to the clicked user and displays it
				mRootRef.child("Users").child(currentUserDisplay).child("sentMessages").child(clickedUserUID).addChildEventListener(new ChildEventListener() {
					@Override
					public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
						if (!firstTime) {
							chat.add(new ChatListItem(currentUserDisplay, dataSnapshot.getValue(String.class), true));

							// Update() - Line: 262
							update();
						}
					}
					@Override
					public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
					@Override
					public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
					@Override
					public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) { }
				});

				// Changes after the first run to be able to run the updates
				firstTime = false;
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) { }
		});

		// Methods to make the keyboard show on start
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(messageEditText, 0);
	}

	// Updates the listView and searches the database for any messages the user has not seen
	// Also displays the last sent messages to the clicked user if the clicked user is still viewing the message - for context
	public void updateListInitial() {
		// Clears the arraylists
		chat.clear();
		// QUERY TO GET ALL INITIAL CLICKED PAST MESSAGES
		Query messagesQuery = mRootRef.child("Users").orderByChild("displayName").equalTo(currentUserDisplay);
		messagesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists() && firstTime) {
					// ALL RECEIVED MESSAGES FIRST (Users/clickedUser/sentMessages/currentUID/messages)
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						for (DataSnapshot children : snapshot.child("receivedMessages").child(clickedUserUID).getChildren()) {
							chat.add(new ChatListItem(clickedUserDisplay, children.getValue(String.class), false));
						}
					}
					// Update() - Line: 262
					update();
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {} });
	}

	// Puts together the map to be displayed in the simpleAdapter
	// Arranges the adapter and sets it to the listview
	public void update() {
		// Setting the adapters
		chatAdapter = new ChatAdapter(ChatActivity.this, chat);
		messageListView.setAdapter(chatAdapter);
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

	// hides the keyboard if enter is clicked
	@Override
	public boolean onKey(View view, int i, KeyEvent keyEvent) {
		if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
			keyBoardDown(view);
		}
		return false;
	}

	// Keyboard down method
	public void keyBoardDown(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}
}
