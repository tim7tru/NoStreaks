package com.example.snapchat_clone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SendUserListActivity extends AppCompatActivity {

    // widgets
    ListView userList;
    ImageView sendButton;

    // list
    ArrayList<String> userNames = new ArrayList<>();
    ArrayList<String> sendTo = new ArrayList<>();

    // variables
    long count = 0;
    String displayName = "";

    // array adapter
    ArrayAdapter<String> arrayAdapter;

    // firebase database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mRoot = database.getReference();
    DatabaseReference mUser = mRoot.child("Users");

    // firebase storage
    StorageReference storageReference;

    // firebase storage file path
    String FIREBASE_IMAGE_STORAGE = "photos/users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_user_list);

        // initializing
        sendButton = findViewById(R.id.sendButton);
        userList = findViewById(R.id.userList);
        userList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, userNames);

        storageReference = FirebaseStorage.getInstance().getReference();

        // to get all the display names of registered users
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String displayName = (String) ds.child("displayName").getValue();
                    Log.i("Display name: ", displayName);
                    userNames.add(displayName);
                }
                Log.i("Array Size: ", Integer.toString(userNames.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mUser.addListenerForSingleValueEvent(eventListener);

        displayName = UserActivity.username;

        // to find the image count for the current user
        final DatabaseReference mCount = mUser.child(displayName);
        final ValueEventListener eventListener1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    count = (long) dataSnapshot.child("count").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mCount.addValueEventListener(eventListener1);

        // to select which users are selected to send the image too
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = (CheckedTextView) view;

                // adds the checked user to an array list
                if (checkedTextView.isChecked()) {
                    sendTo.add(userNames.get(position));
                    Log.i("User Selected: ", userNames.get(position));
                } else {
                    // removes the selected user from the array list
                    sendTo.remove(userNames.get(position));
                    Log.i("Delete User: ", userNames.get(position));
                }

                Log.i("Send to: ", sendTo.toString());
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Upload Photo: ", "attempting to upload photo");
                Toast.makeText(SendUserListActivity.this, "Attempting to send photo...", Toast.LENGTH_SHORT).show();

                // get the userID of the current user
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                final StorageReference myRef = storageReference
                        .child(FIREBASE_IMAGE_STORAGE + "/" + userID + "/" + "photo" + (count)); // specifying which user file the image is going to be stored in

                // grabbing the bytearray from the intent
                Intent intent = getIntent();
                String filePath = intent.getStringExtra("data");
                File file = new File(filePath);

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                byte [] data = stream.toByteArray();
                bitmap.recycle();

                // uploading the bytearray into the database
                final UploadTask uploadTask = myRef.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i("Photo upload:", "SUCCESS");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Photo upload: ", "FAILED");
                    }
                });

                // getting the image URL
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return myRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri firebaseUrl = task.getResult();
                            Log.i("Firebase URL: ",firebaseUrl.toString());

                            // add the image URL into the firebase database
                            Log.i("add photo to database: ", "starting to add photo to database");

                            // updating the count in the database for the current user
                            count = count + 1;
                            Map<String, Object> updateCount = new HashMap<>();
                            updateCount.put("count", count);
                            mCount.updateChildren(updateCount);

                            // putting the image URL into the database for the users the image was sent to
                            for (int i = 0; i < sendTo.size(); i++) {
                                DatabaseReference mPhotos = mUser.child(sendTo.get(i)).child("receivedPhotos").child(FirebaseAuth.getInstance().getCurrentUser().getUid()); // referencing the right database
                                String photoKey = mPhotos.push().getKey();
                                mPhotos.child(photoKey).setValue(firebaseUrl.toString());
                            }
                            Toast.makeText(SendUserListActivity.this, "Sent photos!", Toast.LENGTH_SHORT).show();

                            // return to the list view on finishing sending photo
                            Intent goBack = new Intent(getApplicationContext(), UserActivity.class);
                            startActivity(goBack);
                        }
                    }

                });
            }
        });

        // count the number of images already located in the users database


        userList.setAdapter(arrayAdapter);
    }

    /*
Enables fullscreen mode in the app
- hides the notification bar and navigation bar
- will show again if you swipe down from the top
*/
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}
