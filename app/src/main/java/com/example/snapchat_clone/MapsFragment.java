package com.example.snapchat_clone;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class MapsFragment extends Fragment {

	//Google Maps API Objects
	GoogleMap mGoogleMap;
	MapView mapView;
	View mView;

	//Firebase Database Objects
	FirebaseDatabase database = FirebaseDatabase.getInstance();
	DatabaseReference mRootRef = database.getReference();
	DatabaseReference mUserRef = mRootRef.child("Users");
	Query latQuery, lngQuery;
	ValueEventListener value;

	//ArrayLists to hold all users names/info
	ArrayList<String> displayNames;
	ArrayList<Double> latitudes;
	ArrayList<Double> longitudes;

	//Current user's information
	Double userLat, userLng;
	String userId, userDisplay;
	LatLng userLocation;

	// Misc
	boolean firstTime; // is true on first open, false elsewise; for initial camera zoom on current user

	public MapsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Defining
		displayNames = new ArrayList<>();
		latitudes = new ArrayList<>();
		longitudes = new ArrayList<>();
		firstTime = true;

		// Value Event Listener to get current user's information
		// Has its own query
		value = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					userDisplay = "";
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						userDisplay = (String) snapshot.child("displayName").getValue();
						userLat = snapshot.child("latitude").getValue(Double.class);
						userLng = snapshot.child("longitude").getValue(Double.class);
					}
					Log.i("user latitude", Double.toString(userLat));
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//Inflates the fragment
		mView = inflater.inflate(R.layout.fragment_maps, container, false);

		//Initialize mapView via ID
		//Create the map view
		mapView = (MapView) mView.findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		mapView.onResume();

		//Try Catch initializing the map with host activity context
		try {
			MapsInitializer.initialize(getActivity().getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		latQuery = mUserRef.orderByKey();
		latQuery.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					userId = UserActivity.uniqueID;
					Query uniqueIdQuery = mUserRef.orderByChild("uid").equalTo(userId);
					uniqueIdQuery.addListenerForSingleValueEvent(value);

					try {
						wait(100);
					} catch (Exception e) {
						e.printStackTrace();

					}
					latitudes.clear();
					longitudes.clear();
					displayNames.clear();
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						latitudes.add(snapshot.child("latitude").getValue(Double.class));
						longitudes.add(snapshot.child("longitude").getValue(Double.class));
						displayNames.add((String) snapshot.child("displayName").getValue());
					}
					Log.i("latitudes", latitudes.toString());
					Log.i("longitudes", longitudes.toString());
					Log.i("display names", displayNames.toString());

					mapView.getMapAsync(new OnMapReadyCallback() {
						@Override
						/*
						 * Creates the map / houses initial map settings
						 */
						public void onMapReady(GoogleMap googleMap) {
							mGoogleMap = googleMap;
							mGoogleMap.clear();
							// Map setting / Map Type
							googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

							// For dropping a marker at a point on the Map
							for (int i = 0; i < latitudes.size(); i++) {
								if (displayNames.get(i).equals(userDisplay)) {
									if (firstTime) {
										userLocation = new LatLng(latitudes.get(i), longitudes.get(i));
										googleMap.addMarker(new MarkerOptions().position(userLocation).title(userDisplay).snippet("You are here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
										CameraPosition cameraPosition = new CameraPosition.Builder().target(userLocation).zoom(12).build();
										googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
										firstTime = false;
									} else if (!firstTime) {
										userLocation = new LatLng(latitudes.get(i), longitudes.get(i));
										googleMap.addMarker(new MarkerOptions().position(userLocation).title(userDisplay).snippet("You are here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
									}
								} else {
									LatLng position = new LatLng(latitudes.get(i), longitudes.get(i));
									googleMap.addMarker(new MarkerOptions().position(position).title(displayNames.get(i)).snippet("Blank minutes ago").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
								}
							}
						}
					});
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {}
		});
		return mView;
	}
}
