package com.example.snapchat_clone;

public class User {
	public String displayName, email, uid;

	public User() {

	}

	public User(String displayName, String email, String uid) {
		this.displayName = displayName;
		this.email = email;
		this.uid = uid;
	}

	public String getEmail() {
		return email;
	}
}
