package com.parse.starter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Button callUberButton;
    Boolean isActiveRequest = false;
    Handler handler = new Handler();
    TextView infoTextView;
    Boolean isDriverActive = false;

    public void logoutUser(View view) {
        ParseUser.logOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void callUber(View view) {
        if (isActiveRequest) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && !objects.isEmpty()) {
                        for (ParseObject object : objects) {
                            object.deleteInBackground();
                        }
                        isActiveRequest = false;
                        callUberButton.setText("Call Uber");
                    }
                }
            });

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocation != null) {
                        ParseObject request = new ParseObject("Request");
                        request.put("username", ParseUser.getCurrentUser().getUsername());
                        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        request.put("location", parseGeoPoint);
                        request.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    callUberButton.setText("Cancel Uber");
                                    isActiveRequest = true;
                                    checkForUpdates();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(this, "Could not find rider's location", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public void updateMap(Location location) {
        if (!isDriverActive) {
            LatLng riderLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(riderLocation).title("Rider location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(riderLocation, 10));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateMap(lastKnownLocation);
                }
            }
        }
    }

    public void checkForUpdates() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("driverUsername");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && !objects.isEmpty()) {
                    isDriverActive = true;
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("username", objects.get(0).getString("driverUsername"));
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            if (e == null && !objects.isEmpty()) {
                                ParseGeoPoint driverLocation = objects.get(0).getParseGeoPoint("location");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (lastKnownLocation != null) {
                                            ParseGeoPoint userLocation = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                            Double distanceInKms = driverLocation.distanceInKilometersTo(userLocation);
                                            if (distanceInKms < 0.01) {
                                                infoTextView.setText("Your Driver is here");
                                                ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
                                                query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                                                query.findInBackground(new FindCallback<ParseObject>() {
                                                    @Override
                                                    public void done(List<ParseObject> objects, ParseException e) {
                                                        if (e == null) {
                                                            for (ParseObject object : objects) {
                                                                object.deleteInBackground();
                                                            }
                                                        }
                                                    }
                                                });
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        infoTextView.setText("");
                                                        callUberButton.setVisibility(View.VISIBLE);
                                                        callUberButton.setText("Call Uber");
                                                        isActiveRequest = false;
                                                        isDriverActive = false;
                                                    }
                                                }, 5000);

                                            } else {
                                                Double distanceOneDP = (double) Math.round(distanceInKms * 10) / 10;
                                                infoTextView.setText("Your driver is" + distanceOneDP.toString() + "on the way!");
                                                List<Marker> markers = new ArrayList<>();
                                                mMap.clear();

                                                LatLng driverLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
                                                LatLng requestLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());

                                                markers.add(mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver location")));
                                                markers.add(mMap.addMarker(new MarkerOptions().position(requestLatLng).title("Your location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

                                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                for (Marker marker : markers) {
                                                    builder.include(marker.getPosition());
                                                }

                                                LatLngBounds bounds = builder.build();
                                                int padding = 60;
                                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                                mMap.animateCamera(cu);
                                            }
                                            Double distanceOneDP = (double) Math.round(distanceInKms * 10) / 10;
                                            infoTextView.setText("Your driver is" + distanceOneDP.toString() + "on the way!");
                                            List<Marker> markers = new ArrayList<>();

                                            LatLng driverLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
                                            LatLng requestLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());

                                            markers.add(mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver location")));
                                            markers.add(mMap.addMarker(new MarkerOptions().position(requestLatLng).title("Your location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                            for (Marker marker : markers) {
                                                builder.include(marker.getPosition());
                                            }

                                            LatLngBounds bounds = builder.build();
                                            int padding = 60;
                                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                            mMap.animateCamera(cu);
                                            callUberButton.setVisibility(View.INVISIBLE);
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    checkForUpdates();
                                                }
                                            }, 2000);
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        //getActionBar().hide();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        infoTextView = (TextView) findViewById(R.id.infoTextView);
        callUberButton = (Button) findViewById(R.id.callUberButton);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && !objects.isEmpty()) {
                    isActiveRequest = true;
                    callUberButton.setText("Cancel Uber");
                    checkForUpdates();
                }
            }
        });
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*// Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
               updateMap(location);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    updateMap(lastKnownLocation);
                }
            } else {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }
}
