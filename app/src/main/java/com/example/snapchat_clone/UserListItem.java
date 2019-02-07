package com.example.snapchat_clone;

import android.widget.ImageView;
import android.widget.TextView;

public class UserListItem {

	private int ghostImageViewID;
	private String usernameTextView;
	private String snapCountTextView;
	private String messagesCountTextView;

	public UserListItem(int ghostImageViewID, String usernameTextView, String snapCountTextView, String messagesCountTextView) {
		this.ghostImageViewID = ghostImageViewID;
		this.usernameTextView = usernameTextView;
		this.snapCountTextView = snapCountTextView;
		this.messagesCountTextView = messagesCountTextView;
	}

	public int getGhostImageViewID() {
		return ghostImageViewID;
	}

	public void setGhostImageViewID(int ghostImageViewID) {
		this.ghostImageViewID = ghostImageViewID;
	}

	public String getUsernameTextView() {
		return usernameTextView;
	}

	public void setUsernameTextView(String usernameTextView) {
		this.usernameTextView = usernameTextView;
	}

	public String getSnapCountTextView() {
		return snapCountTextView;
	}

	public void setSnapCountTextView(String snapCountTextView) {
		this.snapCountTextView = snapCountTextView;
	}

	public String getMessagesCountTextView() {
		return messagesCountTextView;
	}

	public void setMessagesCountTextView(String messagesCountTextView) {
		this.messagesCountTextView = messagesCountTextView;
	}
}
