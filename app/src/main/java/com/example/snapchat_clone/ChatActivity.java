package com.example.snapchat_clone;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

	TextView titleTextView;
	ImageView backImageView;
	ListView messageListView;
	EditText messageEditText;
	Button sendButton;
	FirebaseAuth mAuth = FirebaseAuth.getInstance();
	FirebaseDatabase database = FirebaseDatabase.getInstance();
	DatabaseReference mRootRef = database.getReference();
	String message, senderUID, receiverDisplay, senderDisplay;

	public void sendMessage(View view) {
		if (!messageEditText.getText().toString().isEmpty()) {
			message = messageEditText.getText().toString();
			senderUID = mAuth.getCurrentUser().getUid();
			receiverDisplay = listFragment.userClicked;
			senderDisplay = UserActivity.username;

			// Sending the message to the received user
			mRootRef.child("Users").child(receiverDisplay).child("receivedMessages").child(senderUID).child(senderDisplay).setValue(message);
			messageEditText.setText("");
		} else {
			Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.backImageView:
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
