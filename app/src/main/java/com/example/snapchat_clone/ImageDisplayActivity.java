package com.example.snapchat_clone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

    // variables for timer
    int timer;
    boolean isTimerRunning;
    CountDownTimer mCountDownTimer;

    // firebase database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mRoot = database.getReference();
    DatabaseReference mUser = mRoot.child("Users");

    // gesture detector
    GestureDetector gestureDetector;
    public static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    // array list for text from images
    ArrayList<String> text;

    // text view that will show the text from images
    TextView textView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        // initializing widgets
        snapView = findViewById(R.id.snapView);
        timerText = findViewById(R.id.timerText);
        textView = findViewById(R.id.textView);

        // initializing arrayList
        uniqueId = new ArrayList<>();
        imageUrls = new ArrayList<>();
        text = new ArrayList<>();

        // initializing variables
        i = 0;
        timer = 10;
        displayName = UserActivity.username;

        // gesture detector
        gestureDetector = new GestureDetector(this , new MyGestureListener());

        // grab the clicked user and arraylist of unique ids for each image
        Intent intent = getIntent();
        clickedUser = intent.getStringExtra("clickedUser");
        uniqueId = intent.getStringArrayListExtra("keys");

        Log.i("KEYS: ", uniqueId.toString());

        // load the text, and image urls into separate arraylists
        Query user = mUser.child(displayName);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int h = 0; h < uniqueId.size(); h++) {
                    // adding each individual image and text into two different array lists
                    text.add(String.valueOf(dataSnapshot.child("receivedPhotos").child(clickedUser).child(uniqueId.get(h)).child("text").getValue()));
                    imageUrls.add(String.valueOf(dataSnapshot.child("receivedPhotos").child(clickedUser).child(uniqueId.get(h)).child("url").getValue()));

                }
                Log.i("TEXT ARRAY: ", text.toString());
                Log.i("IMAGE URLS: ", imageUrls.toString());

                // preload the images so that they will appear faster
                for (int j = 0; j < imageUrls.size(); j++) {
                    Picasso.get().load(imageUrls.get(j)).fetch();
                }

                // load the first snap into snapView(ImageView)
                if (text.get(i).equals("")) {
                    // if there is no text associated with the image
                    Picasso.get().load(imageUrls.get(i)).fit().centerCrop().into(snapView);
                    // starting the countdown timer
                    isTimerRunning = true;
                    mCountDownTimer.start();
                } else {
                    // if there is text along with the image
                    Picasso.get().load(imageUrls.get(i)).fit().centerCrop().into(snapView);
                    // starting the countdown timer
                    isTimerRunning = true;
                    mCountDownTimer.start();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // countdown timer for snaps
        mCountDownTimer = new CountDownTimer(12000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                i++;
                playSnap(i);
            }
        };

        // setting up the gesture listener for the imageview
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
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        // when the user swipes down on the screen -> returns them back to the UserActivity
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                deleteSnap(i);
                Log.i("SWIPE: ", "Top to Bottom");
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(intent);
            }
            return true;
        }

        // when the user taps on the imageView it shows the next image from the arraylist, if there is none it will return to the UserActivity
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("SINGLE TAP: ", "CONFIRMED");
            i++;
            playSnap(i);
            return true;
        }
    }

    /*
    Function that sets the image view with the next step and calls the function to delete the previous snap
     */
    public void playSnap (Integer num) {
        // if there are still images left in imagesUrls
        if (num < imageUrls.size()) {
            // if there is no text along with the image
            if (text.get(num).equals("")) {
                textView.setVisibility(View.INVISIBLE);
                Picasso.get().load(imageUrls.get(num)).fit().centerCrop().into(snapView);
                deleteSnap(num - 1);
                // if there is text alone with the image
            } else {
                textView.setText(text.get(num));
                textView.setVisibility(View.VISIBLE);
                Picasso.get().load(imageUrls.get(num)).fit().centerCrop().into(snapView);
                deleteSnap(num-1);
            }
            // resetting the timer
            if (!isTimerRunning) {
                isTimerRunning = true;
                mCountDownTimer.start();
            } else {
                isTimerRunning = true;
                mCountDownTimer.cancel();
                mCountDownTimer.start();
            }
            // if there are no more images to show
        } else if (num >= imageUrls.size()) {
            // cancelling the countdown timer
            if (isTimerRunning) {
                isTimerRunning = false;
                mCountDownTimer.cancel();
            }
            // returning back to the list view
            deleteSnap(num-1);
            Intent back = new Intent (getApplicationContext(), UserActivity.class);
            startActivity(back);
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
