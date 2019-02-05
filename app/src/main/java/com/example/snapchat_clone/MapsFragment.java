package com.example.snapchat_clone;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

	//Google Maps API Objects
	GoogleMap mGoogleMap;
	MapView mapView;
	View mView;
	Geocoder geocoder;

	//Firebase Database Objects
	FirebaseDatabase database = FirebaseDatabase.getInstance();
	DatabaseReference mRootRef = database.getReference();
	DatabaseReference mUserRef = mRootRef.child("Users");
	Query latQuery;
	ValueEventListener value;

	//ArrayLists to hold all users names/info
	ArrayList<String> displayNames;
	ArrayList<Double> latitudes;
	ArrayList<Double> longitudes;
	ArrayList<String> cities;

	//Current user's information
	Double userLat, userLng;
	String userId, userDisplay;
	LatLng userLocation;

	// Misc
	boolean doubleBackToExitPressedOnce = false;
	boolean firstTime; // is true on first open, false elsewise; for initial camera zoom on current user

	public MapsFragment() {
		// Required empty public constructor
	}

	// Method that uses latitude and longitude to determine city & country of the last known location of the user
	public String getGeoInfo(double latitude, double longitude, Geocoder geocoder) {
		String info = "";
		try {
			List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 2);
			if (addressList != null && addressList.size() > 0) {
				// City
				if (addressList.get(0).getLocality() != null) {
					info = addressList.get(0).getLocality() + ", ";
				}
				// Country
				if (addressList.get(0).getCountryName() != null) {
					info += addressList.get(0).getCountryName();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}


	// METHOD FOR LOCATION SERVICES PERMISSIONS
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 2) {
			if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				UserActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 500, UserActivity.locationListener);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Defining
		cities = new ArrayList<>();
		displayNames = new ArrayList<>();
		latitudes = new ArrayList<>();
		longitudes = new ArrayList<>();
		firstTime = true;
		userDisplay = UserActivity.username;
		geocoder = new Geocoder(getActivity(), Locale.getDefault());

		// Value Event Listener to get current user's information
		// Has its own query
		value = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
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
	public void onStart() {
		super.onStart();

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
//
//					try {
//						Thread.sleep(3000);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}

					latitudes.clear();
					longitudes.clear();
					displayNames.clear();
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						latitudes.add(snapshot.child("latitude").getValue(Double.class));
						longitudes.add(snapshot.child("longitude").getValue(Double.class));
						displayNames.add((String) snapshot.child("displayName").getValue());
					}

					mapView.getMapAsync(new OnMapReadyCallback() {
						@Override
						/*
						 * Creates the map / houses initial map settings
						 */
						public void onMapReady(GoogleMap googleMap) {
							mGoogleMap = googleMap;
							mGoogleMap.setOnMarkerClickListener(MapsFragment.this);
							mGoogleMap.clear();
							// Map setting / Map Type
							googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

							// For dropping a marker at a point on the Map
							for (int i = 0; i < latitudes.size(); i++) {
								if (displayNames.get(i).equals(userDisplay)) {
									// Zooms in on user's location on first open
									if (firstTime) {
										userLocation = new LatLng(latitudes.get(i), longitudes.get(i));
										googleMap.addMarker(new MarkerOptions().position(userLocation).title(("you").toUpperCase()).snippet(getGeoInfo(latitudes.get(i), longitudes.get(i), geocoder) + "\n" + "").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
										CameraPosition cameraPosition = new CameraPosition.Builder().target(userLocation).zoom(12).build();
										googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
										firstTime = false;
									} else if (!firstTime) {
										userLocation = new LatLng(latitudes.get(i), longitudes.get(i));
										googleMap.addMarker(new MarkerOptions().position(userLocation).title((userDisplay).toUpperCase()).snippet(getGeoInfo(latitudes.get(i), longitudes.get(i), geocoder)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
									}
									// Other user's locations
								} else {
									LatLng position = new LatLng(latitudes.get(i), longitudes.get(i));
									googleMap.addMarker(new MarkerOptions().position(position).title((displayNames.get(i)).toUpperCase()).snippet(getGeoInfo(latitudes.get(i), longitudes.get(i), geocoder)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
								}
							}
						}
					});
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {}
		});
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		Log.i("boolean", Boolean.toString(doubleBackToExitPressedOnce));
		if (doubleBackToExitPressedOnce) {
			String userClicked = marker.getTitle().toLowerCase();
			Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
			intent.putExtra("userClicked", userClicked);
			startActivity(intent);

		} else {
			this.doubleBackToExitPressedOnce = true;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					doubleBackToExitPressedOnce = false;
				}
			}, 2000);

		}
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//Inflates the fragment

		mView = inflater.inflate(R.layout.fragment_maps, container, false);

		//Initialize mapView via ID
		//Create the map view
		mapView = (MapView) mView.findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		return mView;
	}
}
