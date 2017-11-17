package com.example.umairali.wyapp;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import android.location.LocationListener;
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class MapsActivity extends android.support.v4.app.Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private EditText mSearchEditText;
    private Button mButton;
    private SupportMapFragment map;
    private View v;
    private LocationManager locationManager;
    private static final String LOG_TAG = "PlaceSelectionListener";
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    public static Fragment newInstance() {
        return new MapsActivity();
    }
    private GeoDataApi mGeoDataClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v = inflater.inflate(R.layout.activity_maps, container, false);
        Bundle bdl = getArguments();


        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        SupportMapFragment fragment = new SupportMapFragment();
        transaction.add(R.id.map, fragment);
        transaction.commit();

        fragment.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();

        // Method #1
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng location=place.getLatLng();
                String locationName=place.getName().toString();
                Search(location,locationName);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
        autocompleteFragment.setHint("Search a Location");

        return v;
    }

    private void Search(LatLng location, String locationName) {


                mMap.addMarker(new MarkerOptions().position(location).title(locationName));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

    }

    public void onStart() {
        super.onStart();
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            sendToStart();

        } else {


        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                // TODO Auto-generated method stub
                mMap.clear();
                double lat=point.latitude;
                double log=point.longitude;
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(lat,log, 1);
                    String cityName = addresses.get(0).getAddressLine(0);
                    String stateName = addresses.get(0).getAddressLine(1);
                    String countryName = addresses.get(0).getAddressLine(2);
                    mMap.addMarker(new MarkerOptions().position(point).title(cityName+" "+stateName+" "+countryName));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    private void sendToStart() {
        Intent startIntent = new Intent(getContext(), LoginActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startIntent);

    }

    @Override
    public void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {


        }

    }
}
