package com.example.snapchat_clone;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends ArrayAdapter<UserListItem> {

	private Context userContext;
	private List<UserListItem> userListItems = new ArrayList<>();

	public UserAdapter(@NonNull Context context, @LayoutRes ArrayList<UserListItem> list) {
		super(context, 0, list);
		userListItems = list;
	}

	@NonNull
	@Override
	public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
		View listItem = convertView;
		if (listItem == null) {
			listItem = LayoutInflater.from(userContext).inflate(R.layout.user_list_view_item_yes, parent,false);
		}

		UserListItem currentUserListItem = userListItems.get(position);

		ImageView ghostImageView = listItem.findViewById(R.id.ghostImageView);
		ghostImageView.setImageResource(currentUserListItem.getGhostImageViewID());

		TextView usernameTextView = listItem.findViewById(R.id.usernameTextView);
		usernameTextView.setText(currentUserListItem.getUsernameTextView());

		TextView snapCountTextView = listItem.findViewById(R.id.snapsCountTextView);
		snapCountTextView.setText(currentUserListItem.getSnapCountTextView());

		TextView messagesCountTextView = listItem.findViewById(R.id.messagesCountTextView);
		messagesCountTextView.setText(currentUserListItem.getMessagesCountTextView());

		return listItem;
	}
}
