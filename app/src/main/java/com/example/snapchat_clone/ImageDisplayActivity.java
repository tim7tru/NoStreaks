package com.example.snapchat_clone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ImageDisplayActivity extends AppCompatActivity {

    // imageview to display the snaps
    ImageView snapView;

    // arraylist for the photourls
    HashMap<String,String> photoUrls;

    // integer to iterate through the photoUrls(ArrayList)
    int i;

    // variable for current user displayname
    String displayName;

    // variable to get the uid of the clicked user
    String clickedUser;

    // ArrayList for unique id for images
    ArrayList<String> uniqueId;

    // ArrayList for imageUrls
    ArrayList<String> imageUrls;

    // firebase database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mRoot = database.getReference();
    DatabaseReference mUser = mRoot.child("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        // initializing widgets
        snapView = findViewById(R.id.snapView);

        // initializing arrayList
        photoUrls = new HashMap<>();
        uniqueId = new ArrayList<>();
        imageUrls = new ArrayList<>();

        // initializing variables
        i = 0;
        displayName = UserActivity.username;

        // to grab the photourl arraylist from listfragment.java
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            photoUrls = (HashMap<String, String>) bundle.getSerializable("photoUrls");
        }
        clickedUser = intent.getStringExtra("clickedUser");
        Log.i("IMAGE ACTIVITY:", String.valueOf(photoUrls));
        Log.i("CLICKED USER: ", clickedUser);

        // to load the images in the background
        for (Map.Entry<String, String> entry : photoUrls.entrySet()) {
            Picasso.get().load(entry.getValue()).fetch();
            // to load the hashmap into 2 separate arraylists
            uniqueId.add(entry.getKey());
            imageUrls.add(entry.getValue());
        }

        Log.i("UNIQUE ID: ", uniqueId.toString());
        Log.i("IMAGE URLS: ", imageUrls.toString());



        // load the first snap into snapView(ImageView)
        Picasso.get().load(imageUrls.get(i)).fit().centerCrop().into(snapView);

//        // when the user clicks on the image if will move into the next image in the photoUrls(ArrayList)
        snapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                i++;
                if (i < imageUrls.size()) {
                    Log.i("LOADING IMAGE: ", imageUrls.get(i));
                    Picasso.get().load(imageUrls.get(i)).fit().centerCrop().into(snapView);
                    Query deletePhoto = mUser.child(displayName);
                    deletePhoto.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            deleteSnap(i-1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else if (i >= imageUrls.size()) {
                    deleteSnap(i-1);
                    Intent back = new Intent(getApplicationContext(), UserActivity.class);
                    startActivity(back);
                }
            }
        });

    }

    public void deleteSnap(final Integer position) {
        Query deletePhoto = mUser.child(displayName);
        deletePhoto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DatabaseReference ref = dataSnapshot.child("receivedPhotos").child(clickedUser).getRef();
                ref.child(uniqueId.get(position)).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
