package com.example.snapchat_clone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapsFragment#} factory method to
 * create an instance of this fragment.
 */
public class MapsFragment extends Fragment {

	GoogleMap mGoogleMap;
	MapView mapView;
	View mView;

	public MapsFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_maps, container, false);
		mapView = (MapView) mView.findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		mapView.onResume();

		try {
			MapsInitializer.initialize(getActivity().getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
		mapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {

				mGoogleMap = googleMap;
				googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				// For showing a move to my location button

				// For dropping a marker at a point on the Map
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
