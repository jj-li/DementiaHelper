package com.example.jj.dementiahelper;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

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
                if (!createsLatLong()) {
                    Toast.makeText(getApplicationContext(), "Invalid Address. Please Try Again.", Toast.LENGTH_LONG).show();
                    return;
                }

                //File file = new File (path + "/savedFile.txt");
                String res = loadFile("savedFile.txt");
                String[] data;
                EditText name = (EditText) findViewById(R.id.nameField);
                String currentLocation = name.getText() + "," + currentLatitude + "," + currentLongitude;
                Log.d("Loaded: ", res);
                if (!res.equals("")) {
                    String[] prev = res.split("\n");
                    data = new String[prev.length+1];
                    for (int i = 0; i < prev.length; i++) {
                        data[i] = prev[i];
                        String[] temp = prev[i].split(",");
                        Log.d("Name ", temp[0] + " vs " + name.getText());
                        String currName = name.getText().toString();
                        if (currName.equals(temp[0])) {
                            Toast.makeText(getApplicationContext(), "Location name already exists.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (currentLatitude == Double.parseDouble(temp[1]) && currentLongitude == Double.parseDouble(temp[2])) {
                            Toast.makeText(getApplicationContext(), "Location address already exists.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    data[prev.length] = currentLocation;
                }
                else {
                    data = new String[1];
                    data[0] = currentLocation;
                }
                saveFile("savedFile.txt", data);
                finish();
                startActivity(new Intent(getApplicationContext(),Locations.class));
            }

            public void saveFile(String file, String[] data) {

                Context context = getApplicationContext();
                try {
                    FileOutputStream fos = context.openFileOutput(file, Context.MODE_PRIVATE);
                    for (String line : data) {
                        // add terminal character so that it doesn't get written as one line
                        fos.write((line + "\n").getBytes());
                    }
                    fos.close();
                    Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public String loadFile(String file) {
                Context context = getApplicationContext();
                String data = "";
                try {
                    FileInputStream fin = context.openFileInput(file);
                    if (fin != null) {
                        InputStreamReader inputStreamReader = new InputStreamReader(fin);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String line;
                        while (( line = bufferedReader.readLine() ) != null) {
                            data += line + "\n";
                        }
                        fin.close();
                    }
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return data;
            }
        });

        Button myLocation = (Button) findViewById(R.id.myLocation);
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddressName();
            }
        });

        ImageButton backButton = (ImageButton) findViewById(R.id.cancelButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(),Locations.class));
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

    private void createAddressName() {
        Geocoder geocoder;
        List<android.location.Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            ((EditText) findViewById(R.id.addressField)).setText(address);
            ((EditText) findViewById(R.id.cityField)).setText(city);
            ((EditText) findViewById(R.id.stateField)).setText(state);
            ((EditText) findViewById(R.id.zipField)).setText(postalCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean createsLatLong() {
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
            return false;
        }
        if (address.size() == 0) {
            return false;
        }
            Address location = address.get(0);

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            if (currentLatitude != latitude || currentLongitude != longitude) {
                currentLatitude = latitude;
                currentLongitude = longitude;
            }
            return true;
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
