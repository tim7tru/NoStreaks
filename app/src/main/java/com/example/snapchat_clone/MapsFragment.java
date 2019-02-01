package com.example.snapchat_clone;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapsFragment#} factory method to
 * create an instance of this fragment.
 */
public class MapsFragment extends Fragment {

	/*
	 * Map Objects
	 */
	GoogleMap mGoogleMap;
	MapView mapView;
	View mView;
	FirebaseDatabase database = FirebaseDatabase.getInstance();
	DatabaseReference mRootRef = database.getReference();
	DatabaseReference mUserRef = mRootRef.child("Users");
	Query latQuery, lngQuery;
	ArrayList<String> displayNames;
	ArrayList<Double> latitudes;
	ArrayList<Double> longitudes;


	public MapsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		displayNames = new ArrayList<>();
		latitudes = new ArrayList<>();
		longitudes = new ArrayList<>();

		latQuery = mUserRef.orderByKey();
		latQuery.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						latitudes.add((Double) snapshot.child("latitude").getValue(Double.class));
						longitudes.add((Double) snapshot.child("longitude").getValue(Double.class));
						displayNames.add((String) snapshot.child("displayName").getValue());
					}
					Log.i("latitudes", latitudes.toString());
					Log.i("longitudes", longitudes.toString());
					Log.i("display names", displayNames.toString());
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.fragment_maps, container, false); //Inflates the fragment

		/*
		 * Initialize mapView via ID
		 * Create the map view
		 */
		mapView = (MapView) mView.findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		mapView.onResume();

		/*
		 * Try Catch initializing the map with host activity context
		 */
		try {
			MapsInitializer.initialize(getActivity().getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}


		mapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			/*
			 * Creates the map / houses initial map settings
			 */
			public void onMapReady(GoogleMap googleMap) {
				mGoogleMap = googleMap;

				// Map setting / Map Type
				googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

				// For dropping a marker at a point on the Map
				// CURRENT : SYDNEY, AUSTRALIA
				LatLng sydney = new LatLng(-34, 151);
				googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

				// For zooming automatically to the location of the marker
				CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
				googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}
		});
		return mView;
	}


}
