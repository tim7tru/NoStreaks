package com.example.snapchat_clone;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends ArrayAdapter<ChatListItem> {

	private Context userContext;
	private List<ChatListItem> chatListItems = new ArrayList<>();

	public ChatAdapter(@NonNull Context context, ArrayList<ChatListItem> list) {
		super(context, 0, list);
		userContext = context;
		chatListItems = list;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		View listItem = convertView;
		ChatListItem currentChatListItems = chatListItems.get(position);

		if (currentChatListItems.getIsSender()) {
			if (listItem == null) {
				listItem = LayoutInflater.from(userContext).inflate(R.layout.chat_list_view_item_sent, parent,false);
			}

			TextView nameSentTextView = listItem.findViewById(R.id.nameSentTextView);
			nameSentTextView.setText(currentChatListItems.getName());

			TextView messageSentTextView = listItem.findViewById(R.id.messageSentTextView);
			messageSentTextView.setText(currentChatListItems.getMessage());
		} else {
			if (listItem == null) {
				listItem = LayoutInflater.from(userContext).inflate(R.layout.chat_list_view_item_received, parent,false);
			}

			TextView nameReceivedTextView= listItem.findViewById(R.id.nameReceivedTextView);
			nameReceivedTextView.setText(currentChatListItems.getName());

			TextView messageReceivedTextView = listItem.findViewById(R.id.messageReceivedTextView);
			messageReceivedTextView.setText(currentChatListItems.getMessage());
		}




		return listItem;
	}
}
