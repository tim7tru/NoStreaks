/**
 * list fragment that shows all the registered users
 * - can see how many snaps and messages are received from other users -> ghost icon will change to yellow if there are snaps or messages to view
 * - can view these messages and snaps by either clicking/long clicking on the users name in list view
 *
 */

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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class listFragment extends Fragment {

    // firebase authentication
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // firebase database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mRootRef = database.getReference();
    DatabaseReference mUserRef = mRootRef.child("Users");

    // the listView that displays all the info on the registered users
    ListView usersListView;

    // Array list of class UserListItem that has displayName, number of snaps waiting and number of messages waiting
    ArrayList<UserListItem> snaps;

    // LinkedHashmap with key: unique id, value: display name for all registered users
    // - using a linkedhashmap because it orders it by insertion
    LinkedHashMap<String, String> userId;

    // array list of all the unique ids of registered users
    ArrayList<String> uid;

    // display name and unique id of the current user
    String displayName;
    String currentUid;

    // array list of all the display names of registered users
    ArrayList<String> usersDisplayName;

    // ArrayList for photo download url
    ArrayList<String> photoUrls;

    // array list adapters
    SimpleAdapter arrayAdapter;
    UserAdapter userAdapter;

    // log out button
    TextView logOut;

    // snapcount text view
    TextView snapCount;

    public listFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View listView = inflater.inflate(R.layout.fragment_list, container, false);

        // initializing variables and widgets
        usersListView = listView.findViewById(R.id.usersListView);
        snapCount = listView.findViewById(R.id.snapCount);
        logOut = listView.findViewById(R.id.logOut);
        snaps = new ArrayList<>();
        uid = new ArrayList<>();
        userId = new LinkedHashMap<>();
        usersDisplayName = new ArrayList<>();
        photoUrls = new ArrayList<>();

        // to find the display name of the current user
        final Query getDisplayName = mUserRef.orderByChild("uid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        getDisplayName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.i("current user: ", ds.child("displayName").getValue().toString());
                        displayName = ds.child("displayName").getValue().toString();
                        currentUid = ds.child("uid").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // adding values to the userId (HashMap) and uid (ArrayList)
        final ValueEventListener userID = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userId.clear();
                uid.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String uniqueId = String.valueOf(ds.child("uid").getValue());
                        uid.add(uniqueId);
                        userId.put(String.valueOf(ds.child("uid").getValue()), String.valueOf(ds.child("displayName").getValue()));
                    }
                }

                // to query into the database to grab information about number of snaps and messages for each user relevant to the current user
                Query photos = mUserRef.orderByChild("displayName").equalTo(displayName);
                photos.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i("DataSnapshot", dataSnapshot.toString());
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Log.i("Runs", "runs");

                            // add snapcount for the current user and display it in the listview
                            snapCount.setVisibility(View.VISIBLE);
                            snapCount.setText("SnapCount: " + String.valueOf(dataSnapshot.child(displayName).child("count").getValue()));

                            snaps.clear();
                            for (int i = 0; i < uid.size(); i++) {
                            	int ghost;

                            	// check to not add current user into the list
                                if (!userId.get(uid.get(i)).equals(displayName)) {
                                    Log.i("Runs", "runs");
                                    String username = userId.get(uid.get(i));
                                    String snapCount = String.valueOf(ds.child("receivedPhotos").child(uid.get(i)).getChildrenCount());
                                    String messageCount = String.valueOf(ds.child("receivedMessages").child(uid.get(i)).getChildrenCount());
                                    if (Integer.parseInt(snapCount) == 0 && Integer.parseInt(messageCount) == 0) {
                                        ghost = R.drawable.ghost_no;
                                    } else {
                                        ghost = R.drawable.ghost_yes;
                                    }
                                    snaps.add(new UserListItem(ghost, username, snapCount, messageCount));
                                }

	                            // UPDATED February 8, 2019
                                // Now using UserListItem instead of using an array list of maps

//                                Map<String, String> userInfo = new HashMap<>();
//                                userInfo.put("username", userId.get(usernames.get(i)));
//                                userInfo.put("numberofSnaps", String.valueOf(ds.child("receivedPhotos").child(usernames.get(i)).getChildrenCount()) + " snaps " + String.valueOf(ds.child("receivedMessages").child(usernames.get(i)).getChildrenCount()) + " messages");
//                                snaps.add(userInfo);


                        }
                            userAdapter.notifyDataSetChanged();
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


        // setting up the array adapter for the list view
        userAdapter = new UserAdapter(getActivity(), snaps);
        usersListView.setAdapter(userAdapter);

        // query into the firebase database to get all the display names of registered users
        Query userQuery = mUserRef.orderByKey();
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    usersDisplayName.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String name = String.valueOf(snapshot.child("displayName").getValue());
                        if (!name.equals(displayName)) {
                            usersDisplayName.add(snapshot.child("displayName").getValue(String.class));
                        }
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

        /*
        on long click listener to open the ChatActivity
         */
        usersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("userClicked", usersDisplayName.get(position));
                startActivity(intent);
                return true;
            }
        });

        /*
        click listener to open the snaps from user
         */
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                // removing the current user's unique id from the arraylist so the proper unique id is grabbed on the click listener
                for (int i = uid.size()-1;i >= 0; i--) {
                    if (uid.get(i).equals(currentUid)) {
                        uid.remove(uid.get(i));
                    }
                }

                Log.i("UID: ", uid.get(position));


                // query into the database for image sent from the user that was clicked
                final Query photoInfo = mUserRef.child(displayName);
                photoInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        photoUrls.clear();

                        // have to +1 to the position because current logged in user is not displayed on the list
                        for (DataSnapshot ds : dataSnapshot.child("receivedPhotos").child(uid.get(position)).getChildren()) {
                            // get the unique key for each of the photos
                            String key = ds.getKey();
                            // grab all the photo urls based on their unique key and put it into the photoUrls (ArrayList)
                            // UPDATED : by Nicole -> No longer using a hashmap to store data
//                            photoUrls.put(key, String.valueOf(dataSnapshot.child("receivedPhotos").child(uid.get(position)).child(key).getValue()));
                            photoUrls.add(key);

                        }

                        Log.i("KEY: ", photoUrls.toString());

                        // check to see if the user has sent you any photos
                        if (photoUrls.isEmpty()) {
                            Toast.makeText(getActivity(), "There are no snaps!", Toast.LENGTH_SHORT).show();
                        } else {

                            Intent intent = new Intent(getContext(), ImageDisplayActivity.class);
                            intent.putExtra("clickedUser", uid.get(position));
                            intent.putExtra("keys", photoUrls);
                            startActivity(intent);

                            // UPDATED: By Nicole -> No longer using a hashmap to store data
                            // intent to the imageDisplayActivity.java with the photoUrls(ArrayList)
//                            Bundle bundle = new Bundle();
//                            bundle.putSerializable("photoUrls", photoUrls);
//                            Intent intent = new Intent(getContext(), ImageDisplayActivity.class);
//                            intent.putExtra("bundle", bundle);
//                            intent.putExtra("clickedUser", uid.get(position));
//                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}

