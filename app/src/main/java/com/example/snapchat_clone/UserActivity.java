package com.example.snapchat_clone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.internal.firebase_auth.zzcz;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserActivity extends AppCompatActivity {

	FirebaseDatabase database = FirebaseDatabase.getInstance();
	FirebaseUser user;
	DatabaseReference mRootRef = database.getReference();
	DatabaseReference mUserRef = mRootRef.child("Users");
	static LocationManager locationManager;
	static LocationListener locationListener;
	static String uniqueID;
	static String username;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
	    // Initialize Firebase Auth
	    mAuth = AuthActivity.mAuth;

	    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new pageAdapter(getSupportFragmentManager()));
	    uniqueID = mAuth.getCurrentUser().getUid();
	    Log.i("unique id", uniqueID);
	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	    Query uniqueIdQuery = mUserRef.orderByChild("uid").equalTo(uniqueID);

	    ValueEventListener value = new ValueEventListener() {
		    @Override
		    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
			    if (dataSnapshot.exists()) {
				    username = "";
				    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					    username = (String) snapshot.child("displayName").getValue();
					    Log.i("Username in UserActivity", username + "");
				    }
			    }
		    }

		    @Override
		    public void onCancelled(@NonNull DatabaseError databaseError) { }
	    };
	    uniqueIdQuery.addListenerForSingleValueEvent(value);


	    locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();
				uniqueID = mAuth.getCurrentUser().getUid();
				Log.i("unique id", uniqueID);

				Query uniqueIdQuery = mUserRef.orderByChild("uid").equalTo(uniqueID);
				uniqueIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if (dataSnapshot.exists()) {
							username = "";
							for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
								username = (String) snapshot.child("displayName").getValue();
								Log.i("Username in UserActivity", username + "");
							}
							mUserRef.child(username).child("longitude").setValue(longitude);
							mUserRef.child(username).child("latitude").setValue(latitude);
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {}
				});
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };

	    // CAMERA AND LOCATION PERMISSIONS
	    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
		    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA}, 2);
	    } else {
		    UserActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1000, UserActivity.locationListener);
	    }
    }

	/*
			The pageAdapter for viewPager to implement the fragments
		 */
    private class pageAdapter extends FragmentPagerAdapter {

        public pageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0: return new listFragment();
                case 1: return new sendFragment();
                case 2: return new MapsFragment();
                default: return new listFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
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
