package com.example.snapchat_clone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SendUserListActivity extends AppCompatActivity {

    // widgets
    ListView userList;
    ImageView sendButton;
    ImageView backButton;

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

    // progress dialog for uploading photos
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_user_list);

        // initializing
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        userList = findViewById(R.id.userList);
        userList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        userList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, userNames) {
            // updating the look of the text in the list view
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.roboto_medium);
                textView.setTextSize(24);
                textView.setTypeface(typeface);
                textView.setTextColor(Color.DKGRAY);
                return view;
            }
        };


        // firebase storage
        storageReference = FirebaseStorage.getInstance().getReference();

        // getting the current user's display name
        displayName = UserActivity.username;

        /*
            on click listener for the back button to go back to the camera fragment when tapped
         */
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("BUTTON CLICKED: ", "back button");
                finish();
            }
        });

        // to get all the display names of registered users
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = (String) ds.child("displayName").getValue();
                    Log.i("Display name: ", name);

                    // checks to make sure you are not adding your own display name to the array list
                    if (!name.equals(displayName)) {
                        userNames.add(name);
                    }
                }
                Log.i("Array Size: ", Integer.toString(userNames.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mUser.addListenerForSingleValueEvent(eventListener);

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

                View selectedItemView = view;

                if (selectedItemView.isActivated()) {
                    // adds the selected user to an array list
                    sendTo.add(userNames.get(position));
                    Log.i("User Selected: ", userNames.get(position));
                } else {
                    // removes the selected user from the array list
                    sendTo.remove(userNames.get(position));
                }

                Log.i("Send to: ", sendTo.toString());


                //UPDATED February 8, 2019 by Nicole
                // changed the array adapter to simplelistactivated 1
//                CheckedTextView checkedTextView = (CheckedTextView) view;
//
//                // adds the checked user to an array list
//                if (checkedTextView.isChecked()) {
//                    sendTo.add(userNames.get(position));
//                    Log.i("User Selected: ", userNames.get(position));
//                } else {
//                    // removes the selected user from the array list
//                    sendTo.remove(userNames.get(position));
//                    Log.i("Delete User: ", userNames.get(position));
//                }
//
//                Log.i("Send to: ", sendTo.toString());
            }
        });

        /*
        function that is run when the send button is clicked
            - grabs the byte array from image
            - adds it to firebase storage
            - adds the download url of image into the firebase database
         */
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Upload Photo: ", "attempting to upload photo");
//                Toast.makeText(SendUserListActivity.this, "Attempting to send photo...", Toast.LENGTH_SHORT).show();

                // progress dialog
                progressDialog = new ProgressDialog(SendUserListActivity.this);
                progressDialog.setMessage("Sending Snap");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();

                // get the userID of the current user
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // firebase storage reference
                final StorageReference myRef = storageReference
                        .child(FIREBASE_IMAGE_STORAGE + "/" + userID + "/" + "photo" + (count)); // specifying which user file the image is going to be stored in

                // grabbing the temporary file from the intent
                Intent intent = getIntent();
                String filePath = intent.getStringExtra("data");
                final String text = intent.getStringExtra("text");
                Log.i("STRING: ", text);
                File file = new File(filePath);

                // decoding the file into a byte array
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                byte [] data = stream.toByteArray();
                bitmap.recycle();

                // uploading the bytearray into the firebase storage
                final UploadTask uploadTask = myRef.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i("Photo upload:", "SUCCESS");

                        myRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.cancel();
                                Uri firebaseUrl = uri;
                                Log.i("FIREBASE URL: ", uri.toString());

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
                                mPhotos.child(photoKey).child("url").setValue(firebaseUrl.toString());
                                mPhotos.child(photoKey).child("text").setValue(text);
                            }

                            // return to the list view on finishing sending photo
                            Intent goBack = new Intent(getApplicationContext(), UserActivity.class);
                            startActivity(goBack);
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.cancel();
                        Toast.makeText(SendUserListActivity.this, "Photo Upload FAILED", Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                });
            }
        });
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
