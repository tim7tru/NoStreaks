package com.example.snapchat_clone;

public class ChatListItem {

	private String name;
	private String message;
	private Boolean isSender;

	public ChatListItem(String name, String message, boolean isSender) {
		this.name = name;
		this.message = message;
		this.isSender = isSender;
	}

	public void setIsSender(boolean isSender) {
		this.isSender = isSender;
	}

	public boolean getIsSender() {
		return isSender;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
