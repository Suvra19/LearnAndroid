package com.parse.starter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    List<String> requests = new ArrayList<>();
    ListView rideListView;
    ArrayAdapter arrayAdapter;

    List<Double> requestLatitudes = new ArrayList<>();
    List<Double> requestLongitudes = new ArrayList<>();
    List<String> usernames = new ArrayList<>();

    public void updateListView(Location location) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        final ParseGeoPoint geoPoint =  new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        query.whereNear("location", geoPoint);
        query.whereDoesNotExist("driverUsername");
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && !objects.isEmpty()) {
                    requests.clear();
                    requestLatitudes.clear();
                    requestLongitudes.clear();
                    for (ParseObject object : objects) {
                        ParseGeoPoint requestLocation = (ParseGeoPoint) object.get("location");
                        Double distanceInKms = geoPoint.distanceInKilometersTo(requestLocation);
                        requests.add(String.valueOf(Math.round(distanceInKms * 10) / 10) + " kms");
                        requestLatitudes.add(requestLocation.getLatitude());
                        requestLongitudes.add(requestLocation.getLongitude());
                        usernames.add(object.getString("username"));
                    }
                } else if (objects.isEmpty()) {
                    requests.add("No active requests nearby");
                }
                rideListView.setAdapter(arrayAdapter);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateListView(lastKnownLocation);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        setTitle("Nearby requests");
        rideListView = (ListView) findViewById(R.id.rideListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requests);
        requests.clear();
        requests.add("Getting nearby requests...");
        rideListView.setAdapter(arrayAdapter);
        rideListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (!requestLatitudes.isEmpty() && !requestLongitudes.isEmpty() && lastKnownLocation != null) {
                            Intent intent = new Intent(getApplicationContext(), DriverMapActivity.class);
                            intent.putExtra("requestLatitude", requestLatitudes.get(position));
                            intent.putExtra("requestLongitude", requestLongitudes.get(position));
                            intent.putExtra("driverLatitude", lastKnownLocation.getLatitude());
                            intent.putExtra("driverLongitude", lastKnownLocation.getLongitude());
                            intent.putExtra("username", usernames.get(position));
                            startActivity(intent);
                        }
                    } else {
                        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateListView(location);
                ParseUser.getCurrentUser().put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();
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
                    updateListView(lastKnownLocation);
                }
            } else {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }
}
