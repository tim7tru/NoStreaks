package com.example.snapchat_clone;

public class User {
	public String displayName, email, uid, recievedPhotos, recievedMessages;
	public float latitude, longitude;

	public User() {

	}

	public User(String displayName, String email, String uid) {
		this.displayName = displayName;
		this.email = email;
		this.uid = uid;
		this.latitude = 0;
		this.longitude = 0;
		this.recievedMessages = "";
		this.recievedPhotos = "";
	}

	public String getEmail() {
		return email;
	}

	public String getDisplayName() { return displayName; }

	public String getUid() { return uid; }
}
