package com.example.snapchat_clone;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.content.Intent;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class listFragment extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mRootRef = database.getReference();
    DatabaseReference mUserRef = mRootRef.child("Users");
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ListView usersListView;
    //    ArrayList<String> usersDisplayName;
    static String userClicked;

    ArrayList<Map<String, String>> snaps;
    HashMap<String, String> userId;
    ArrayList<String> usernames;
    static String displayName;
    ArrayList<String> usersDisplayName;

    // HashMap for username and photo download url
    ArrayList<Map<String, String>> photoUrls;

    SimpleAdapter arrayAdapter;

    TextView logOut;

    public listFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View listView = inflater.inflate(R.layout.fragment_list, container, false);

        usersListView = listView.findViewById(R.id.usersListView);
        logOut = listView.findViewById(R.id.logOut);
        snaps = new ArrayList<>();
        usernames = new ArrayList<>();
        userId = new HashMap<>();
        usersDisplayName = new ArrayList<>();
        photoUrls = new ArrayList<>();

        final Query getDisplayName = mUserRef.orderByChild("uid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        getDisplayName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.i("current user: ", ds.child("displayName").getValue().toString());
                        displayName = ds.child("displayName").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final ValueEventListener userID = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userId.clear();
                usernames.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        userId.put(String.valueOf(ds.child("uid").getValue()), String.valueOf(ds.child("displayName").getValue()));
                        usernames.add(String.valueOf(ds.child("uid").getValue()));
                    }
                    Log.i("User IDs: ", userId.toString());
                }

                Query photos = mUserRef.orderByChild("displayName").equalTo(displayName);
                photos.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            snaps.clear();
                            for (int i = 0; i < usernames.size(); i++) {
                                Map<String, String> userInfo = new HashMap<>();
                                userInfo.put("username", userId.get(usernames.get(i)));
                                userInfo.put("numberofSnaps", String.valueOf(ds.child("receivedPhotos").child(usernames.get(i)).getChildrenCount()) + " snaps");
                                snaps.add(userInfo);
                                // grabbing the photo download urls by their unique id
                                for (DataSnapshot snapshot : ds.child("receivedPhotos").child(usernames.get(i)).getChildren()) {
//                                    Log.i("PHOTO KEY: ", snapshot.getKey());
                                    String key = snapshot.getKey();
//                                    Log.i("PHOTO URL: ", String.valueOf(ds.child("receivedPhotos").child(usernames.get(i)).child(key).getValue()));
                                    Map<String, String> photoInfo = new HashMap<>();
                                    photoInfo.put(usernames.get(i), String.valueOf(ds.child("receivedPhotos").child(usernames.get(i)).child(key).getValue()));
                                    photoUrls.add(photoInfo);
                                }
                            }
                            arrayAdapter.notifyDataSetChanged();
                        }
                        Log.i("PHOTO HASHMAP: ", photoUrls.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mUserRef.addValueEventListener(userID);

        arrayAdapter = new SimpleAdapter(getActivity(), snaps, android.R.layout.simple_list_item_2, new String[]{"username", "numberofSnaps"}, new int[]{android.R.id.text1, android.R.id.text2});
        usersListView.setAdapter(arrayAdapter);

        Query userQuery = mUserRef.orderByKey();
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    usersDisplayName.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        usersDisplayName.add(snapshot.child("displayName").getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // to log the user out of their account
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Logout", "attempting to log out");
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
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
                intent.putExtra("userClicked", usersDisplayName.get(position));
                startActivity(intent);
                return true;
            }
        });
    }
}

