package com.example.snapchat_clone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ImageDisplayActivity extends AppCompatActivity {

    // imageview to display the snaps
    ImageView snapView;

    // arraylist for the photourls
    HashMap<String,String> photos;

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

    // timer text
    TextView timerText;

    // variable for timer
    int timer;

    // firebase database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mRoot = database.getReference();
    DatabaseReference mUser = mRoot.child("Users");

    // gesture detector
    GestureDetector gestureDetector;
    public static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    // screen dimensions
    int height;
    int width;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        // initializing widgets
        snapView = findViewById(R.id.snapView);
        timerText = findViewById(R.id.timerText);

        // initializing arrayList
        photos = new HashMap<>();
        uniqueId = new ArrayList<>();
        imageUrls = new ArrayList<>();

        // initializing variables
        i = 0;
        timer = 10;
        displayName = UserActivity.username;

        // gesture detector
        gestureDetector = new GestureDetector(new GestureListener());

        // height and width of screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = size.y;
        width = size.x;

        // to grab the photourl arraylist and clickeduser from listfragment.java
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            photos = (HashMap<String, String>) bundle.getSerializable("photoUrls");
        }
        clickedUser = intent.getStringExtra("clickedUser");

        // to load the images in the background -> to view the images faster as they take a long time to load
        for (Map.Entry<String, String> entry : photos.entrySet()) {
            Picasso.get().load(entry.getValue()).fetch();
            // to load the hashmap into 2 separate arraylists
            uniqueId.add(entry.getKey());
            imageUrls.add(entry.getValue());
        }

        // load the first snap into snapView(ImageView)
        Picasso.get().load(imageUrls.get(i)).fit().centerCrop().into(snapView);

        // swipe up gesture on imageview
        snapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

    }

    /*
    GestureListener that listens for swipe down and tap on imageView
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        // when the user swipes down on the screen -> returns them back to the UserActivity
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                deleteSnap(i);
                Log.i("SWIPE: ", "Top to Bottom");
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(intent);
            }
            return false;
        }

        // when the user taps on the imageView it shows the next image from the arraylist, if there is none it will return to the UserActivity
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("SINGLE TAP: ", "CONFIRMED");
            i++;
            if (i < imageUrls.size()) {
                Log.i("LOADING IMAGE: ", imageUrls.get(i));
                Picasso.get().load(imageUrls.get(i)).fit().centerCrop().into(snapView);
                // to delete the image from the database after viewing it
                deleteSnap(i-1);

                // if there are no more photos left to view to go back to the listFragment
            } else if (i >= imageUrls.size()) {
                // to delete te image from the database after viewing it
                deleteSnap(i-1);
                Intent back = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(back);
            }
            return false;
        }
    }

    // to delete the image from the database
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
