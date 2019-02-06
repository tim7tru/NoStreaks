package com.example.snapchat_clone;


import android.os.Bundle;
import android.provider.ContactsContract;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.content.Intent;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

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
import java.util.Objects;


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

    SimpleAdapter arrayAdapter;

    public listFragment() {
        // Required empty public constructor

    }

    // TODO: display the image on another activity that just has a imageView
    // full screen just like the camera
    // tapping on the image will view the next image -> if there are no more images left to be shown will be brought back to the listView
    // TODO: set a timer for how long the image is displayed for before they are returned to the listView
    // make sure to delete it from storage and from the database once the image is finished being viewed
    // display a timer in the corner on how much they time they have left to view the image


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View listView = inflater.inflate(R.layout.fragment_list, container, false);

        usersListView = listView.findViewById(R.id.usersListView);
        snaps = new ArrayList<>();
        usernames = new ArrayList<>();
        userId = new HashMap<>();
        usersDisplayName = new ArrayList<>();

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
                            Log.i("Snaps after delete: ", snaps.toString());
                            for (int i = 0; i < usernames.size(); i++) {
                                Map<String, String> userInfo = new HashMap<>();
                                userInfo.put("username", userId.get(usernames.get(i)));
                                userInfo.put("numberofSnaps", String.valueOf(ds.child("receivedPhotos").child(usernames.get(i)).getChildrenCount()) + " snaps");
                                snaps.add(userInfo);
                            }
                            Log.i("Snaps after going through the for loop:  ", snaps.toString());
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

