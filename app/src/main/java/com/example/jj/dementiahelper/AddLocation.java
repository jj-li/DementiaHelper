package com.example.jj.dementiahelper;

import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;

public class AddLocation extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationManager locationManager;
    public static final String TAG = MapsActivity.class.getSimpleName();
    double currentLatitude;
    double currentLongitude;
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DementiaHelperDocuments";
    private static final int REQUEST_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //File dir = new File(path);
        //dir.mkdirs();

        ImageButton addButton = (ImageButton) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createsLatLong();

                //File file = new File (path + "/savedFile.txt");
                String[] data = {"Hard Coded Data", "TWO"};
                String res = loadFile("savedFile.txt");
                if (!res.equals("")) {
                    String[] prev = res.split("\n");
                    data = new String[prev.length + 1];
                    for (int i = 0; i < prev.length; i++) {
                        data[i] = prev[i];
                    }
                    //data[prev.length] = currentLatitude + "," + currentLongitude;
                    data[prev.length] = "Hard Coded Data";
                }
                saveFile("savedFile.txt", data);
                /*res = loadFile("savedFile.txt");
                if (res.equals("")) {
                    addr.setText("Nothing");
                }
                else
                    addr.setText(res);*/
            }

            public void saveFile(String file, String[] data) {
                FileOutputStream outputStream;
                try {
                    outputStream = openFileOutput(file, Context.MODE_PRIVATE);
                    for (int i = 0; i < data.length; i++) {
                        outputStream.write(data[i].getBytes());
                        if (i < data.length - 1) {
                            outputStream.write("\n".getBytes());
                        }
                    }
                    outputStream.close();
                    Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public String loadFile(String file) {
                String data = "";
                FileInputStream inputStream;
                try {
                    inputStream = openFileInput(file);
                    //File iF = new File(file);
                    //int length = (int) iF.length();
                    byte[] bytes = new byte[4096];
                    inputStream.read(bytes);
                    data = new String(bytes);
                    inputStream.close();
                    Toast.makeText(getApplicationContext(), "Loaded", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return data;
            }
        });

        Button myLocation = (Button) findViewById(R.id.myLocation);
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText addr = (EditText) findViewById(R.id.addressField);
                addr.setText(Double.toString(currentLatitude) + " " + Double.toString(currentLongitude));
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
//        mLocationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(10000)
//                .setFastestInterval(5000);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_location, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //checkAndRequestPermissions();
            return;
        }
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
        } else {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, 9000);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
    }

    public void createsLatLong() {
        Geocoder coder = new Geocoder(this);
        List<Address> address = null;
        StringBuilder strAddr = new StringBuilder();
        //add addr
        strAddr.append((((EditText) findViewById(R.id.addressField)).getText().toString().trim())).append(" ");
        //add city
        strAddr.append((((EditText) findViewById(R.id.cityField)).getText().toString().trim())).append(" ");
        //add state
        strAddr.append((((EditText) findViewById(R.id.stateField)).getText().toString().trim())).append(" ");
        //add zipcode
        strAddr.append((((EditText) findViewById(R.id.zipField)).getText().toString().trim())).append(" ");

        try {
            address = coder.getFromLocationName(strAddr.toString(), 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (address == null) {
            return;
        }
        Address location = address.get(0);

        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
//        String latLongString = Double.toString(latitude) + " ";
//        latLongString += Double.toString(longitude);
//        ((EditText) findViewById(R.id.zipField)).setText(latLongString);

        if (currentLatitude != latitude || currentLongitude != longitude) {
            currentLatitude = latitude;
            currentLongitude = longitude;
        }
    }

    private void checkAndRequestPermissions() {
        int coarsePermission = ContextCompat.checkSelfPermission(this,ACCESS_COARSE_LOCATION);
        int finePermission = ContextCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (coarsePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(ACCESS_COARSE_LOCATION);
        }
        if (finePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_PERMISSIONS);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Button myLocation = (Button) findViewById(R.id.myLocation);
                    myLocation.setClickable(false);
                }
                return;
            }
        }
    }
}
