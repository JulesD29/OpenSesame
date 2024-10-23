package com.example.opensesame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GeoZoneCall";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CALL_PERMISSION = 2;
    private FusedLocationProviderClient fusedLocationClient;

    // Numéro de téléphone à appeler
    private static final String PHONE_NUMBER = "+123456789";

    // Drapeau pour activer/désactiver l'appel automatique
    private boolean autoCallEnabled = true;

    private CheckBox autoCallCheckbox; // Case à cocher pour activer/désactiver l'appel automatique

    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView phoneNumberText = findViewById(R.id.phoneNumberText);
        Button callButton = findViewById(R.id.callButton);
        autoCallCheckbox = findViewById(R.id.autoCallCheckBox);

        phoneNumberText.setText("Numéro de téléphone : " + PHONE_NUMBER); // Assure-toi que PHONE_NUMBER est bien initialisé
        callButton.setOnClickListener(v -> makePhoneCall());
        autoCallCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> autoCallEnabled = isChecked);


        // Votre code d'initialisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialiser le LocationCallback AVANT d'appeler startLocationUpdates()
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());

                    if (isWithinGeoZone(location)) {
                        Log.d(TAG, "Utilisateur dans la zone, déclencher l'appel.");
                        makePhoneCall();
                    }
                }
            }
        };

        // Vérifiez les permissions et démarrez les mises à jour de localisation
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            startLocationUpdates();  // Appelez startLocationUpdates une fois que tout est initialisé
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // Intervalle en millisecondes (10 secondes)
        locationRequest.setFastestInterval(5000); // Intervalle minimal (5 secondes)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        // Vérifiez que locationCallback n'est pas nul avant de passer à requestLocationUpdates
        if (locationCallback != null) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            Log.e(TAG, "LocationCallback is null!");
        }
    }


    private boolean isWithinGeoZone(Location location) {
        double targetLatitude = 47.2085577;
        double targetLongitude = -1.5830814;
        float[] distance = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), targetLatitude, targetLongitude, distance);
        return distance[0] < 100; // Moins de 100 mètres
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void makePhoneCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + PHONE_NUMBER));
            startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else if (requestCode == REQUEST_CALL_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            makePhoneCall();
        } else {
            Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
        }
    }
}
