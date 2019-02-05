package com.example.snapchat_clone;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

	TextView titleTextView;
	ImageView backImageView;
	ListView messageListView;
	EditText messageEditText;
	Button sendButton;
	FirebaseAuth mAuth = FirebaseAuth.getInstance();
	FirebaseDatabase database = FirebaseDatabase.getInstance();
	DatabaseReference mRootRef = database.getReference();
	String message, uniqueKey,currentUserUID, clickedUserDisplay, currentUserDisplay, clickedUserUID;
	ArrayList<String> messages;
	ArrayAdapter<String> arrayAdapter;
	boolean closing;
	public void sendMessage(View view) {
		if (!messageEditText.getText().toString().isEmpty()) {
			message = messageEditText.getText().toString();
			Log.i("Username", currentUserDisplay);
			uniqueKey = mRootRef.push().getKey();
			// Receiver
			mRootRef.child("Users").child(clickedUserDisplay).child("receivedMessages").child(currentUserUID).child(uniqueKey).setValue(message);
			// Sender
			mRootRef.child("Users").child(currentUserDisplay).child("sentMessages").child(clickedUserUID).child(uniqueKey).setValue(message);
			messageEditText.setText("");
			updateList();
		} else {
			Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
		}
	}

	public void updateList() {
		messages.clear();
		Query messagesQuery =  mRootRef.child("Users").orderByChild("displayName").equalTo(clickedUserDisplay);
		messagesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					// CURRENT MESSAGES
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						for (DataSnapshot children : snapshot.child("sentMessages").child(currentUserUID).getChildren()) {
							messages.add(children.getValue(String.class));
						}
					}
					arrayAdapter = new ArrayAdapter<>(ChatActivity.this, android.R.layout.simple_list_item_1, messages);
					messageListView.setAdapter(arrayAdapter);
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						for (DataSnapshot children : snapshot.child("receivedMessages").child(currentUserUID).getChildren()) {
							messages.add(children.getValue(String.class));
						}
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.backImageView:
				closing = true;
				messages.clear();

				// Deletes ClickedOn Sent
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

				// Deletes Current Received
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
		messageEditText = findViewById(R.id.messageEditText);
		messageListView = findViewById(R.id.messageListView);
		sendButton = findViewById(R.id.sendButton);
		backImageView = findViewById(R.id.backImageView);
		backImageView.setOnClickListener(this);
		titleTextView = findViewById(R.id.titleTextView1);
		titleTextView.setText(listFragment.userClicked);
		currentUserUID = mAuth.getCurrentUser().getUid();
		clickedUserDisplay = listFragment.userClicked;
		currentUserDisplay = UserActivity.username;
		messages = new ArrayList<>();
		arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
		closing = false;

		Query receiverUIDQuery = mRootRef.child("Users").orderByChild("displayName").equalTo(clickedUserDisplay);
		receiverUIDQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						clickedUserUID = snapshot.child("uid").getValue(String.class);
						Log.i("Current Disp", currentUserDisplay);
						Log.i("Current UID", currentUserUID);
						Log.i("Clicked Disp", clickedUserDisplay);
						Log.i("Clicked UID", clickedUserUID);
					}
					updateList();
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) { }
		});

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
