package com.example.umairali.wyapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private EditText mSearchEditText;
    private Button mButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Toolabr BackArrow
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAuth=FirebaseAuth.getInstance();
        mSearchEditText=(EditText)findViewById(R.id.searchEdit);
        mButton=(Button)findViewById(R.id.searchbtn);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Search();
            }
        });

    }

    private void Search() {
        String location=mSearchEditText.getText().toString();
        List<Address> addressList=null;
            Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
            try {
                addressList=geocoder.getFromLocationName(location,1);
                if (addressList != null && addressList.size() > 0) {
                    Address address=(Address)addressList.get(0);
                    LatLng latLng=new LatLng(address.getLatitude(),address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Where Are You"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                }

            } catch (IOException e) {
                e.printStackTrace();
        }
    }

    protected void onStart() {
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
                mMap.addMarker(new MarkerOptions().position(point).title("You Are Here"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));

            }
        });

    }
    private void sendToStart() {
        Intent startIntent = new Intent(MapsActivity.this, LoginActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startIntent);
        finish();

    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {


        }

    }
}
