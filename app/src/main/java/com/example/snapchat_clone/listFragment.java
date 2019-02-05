package com.example.snapchat_clone;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



/**
 * A simple {@link Fragment} subclass.
 */
public class listFragment extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mRootRef = database.getReference();
    DatabaseReference mUserRef = mRootRef.child("Users");
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ListView usersListView;
    ArrayList<String> usersDisplayName;
    static String userClicked;
    ArrayAdapter<String> arrayAdapter;

    public listFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View listView = inflater.inflate(R.layout.fragment_list, container, false);

        usersListView = listView.findViewById(R.id.usersListView);
        usersDisplayName = new ArrayList<>();

        Query userQuery = mUserRef.orderByKey();
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                	usersDisplayName.clear();
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
							usersDisplayName.add(snapshot.child("displayName").getValue(String.class));
					}
					Log.i("Users", usersDisplayName.toString());
	                arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, usersDisplayName);
	                usersListView.setAdapter(arrayAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        return listView;
    }

	@Override
	public void onStart() {
		super.onStart();
		usersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getContext(), ChatActivity.class);
				userClicked = usersDisplayName.get(position);
				Log.i("User clicked", userClicked);
				startActivity(intent);
				return true;
			}
		});
    }
}
