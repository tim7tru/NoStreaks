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

    // ArrayList for photo download url
    HashMap<String, String> photoUrls;

    SimpleAdapter arrayAdapter;
    UserAdapter userAdapter;

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
        photoUrls = new HashMap<>();

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
                        }
                            arrayAdapter.notifyDataSetChanged();
                        }

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

        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                // query into the database for image sent from the user that was clicked
                final Query photoInfo = mUserRef.child(displayName);
                photoInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        photoUrls.clear();
                        for (DataSnapshot ds : dataSnapshot.child("receivedPhotos").child(usernames.get(position)).getChildren()) {
                            // get the unique key for each of the photos
                            String key = ds.getKey();
                            // grab all the photo urls based on their unique key and put it into the photoUrls (ArrayList)

                            photoUrls.put(key, String.valueOf(dataSnapshot.child("receivedPhotos").child(usernames.get(position)).child(key).getValue()));
                        }

                        Log.i("PHOTO URLS: ", photoUrls.toString());

                        // intent to the imageDisplayActivity.java with the photoUrls(ArrayList)
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("photoUrls", photoUrls);
                        Intent intent = new Intent(getContext(), ImageDisplayActivity.class);
                        intent.putExtra("bundle", bundle);
                        intent.putExtra("clickedUser", usernames.get(position));
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}

