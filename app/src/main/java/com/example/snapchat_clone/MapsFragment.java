package com.example.snapchat_clone;

import android.Manifest;
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
	LocationListener locationListener;
	LocationManager locationManager;
	String rawInfo;
	Float latitude, longitude;

	public MapsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1000, locationListener);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				rawInfo = location.toString();
				Pattern p = Pattern.compile("gps (.*?) hAcc=");
				Matcher m = p.matcher(rawInfo);

				while (m.find()) {
					rawInfo = m.group(1);
				}

				String[] splitInfo = rawInfo.split(",");
				latitude = Float.parseFloat(splitInfo[0]);
				longitude = Float.parseFloat(splitInfo[1]);
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}
		};


		if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
		} else {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1000, locationListener);
		}
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
