package com.example.reto1appsmovilesgmapsjvelez;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Valor del permiso para la ubicacion del dispositivo.
    public static final int LOCATION_REQUEST_PERMISSION = 100;
    // Boolean para determinar si se ha concedido o no el permiso por parte del usuario de la ubicacion del dispositivo.
    private boolean permissionLocation = false;
    // Dialogo para crear un marcador.
    private MarkerDialog markerDialog;
    // El mapa de GoogleMaps
    private GoogleMap mMap;
    // Boton flotante para agregar marcadores en el mapa.
    private FloatingActionButton butFlotante;
    // TextView para mostrar la info del lugar mas cercano
    private TextView textViewInfo;
    // Boolean para poder fijar un marcador cuando se presione el boton flotante para agregar marcadores.
    private boolean fijarMarcador = false;
    // Guarda la posicion del ultimo marcador a guardar.
    private LatLng lastMarkerLat;
    // Para localizar el usuario
    private FusedLocationProviderClient fusedLocationProviderClient;
    // La ubicacion del usuario
    private Location userLocation;
    // El marcador del usuario
    private Marker userMarker;
    // Para verificar si el usuario del marcador ya ha sido agregado.
    private boolean userMarkerAdded = false;
    //Lista de markers
    private ArrayList<Marker> markers;
    //Para obtener informacion de una ubicación.
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLocationPermission();

        butFlotante = findViewById(R.id.butFlotante);
        textViewInfo = findViewById(R.id.txtViewInfo);

        butFlotante.setOnClickListener(
                (v) -> {

                    fijarMarcador = true;
                    Toast.makeText(this, "Indique un punto en el mapa para crear un marcador.", Toast.LENGTH_LONG).show();

                });

        geocoder = new Geocoder(this);
        markers = new ArrayList<>();

    }


    public void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMarkerClickListener(this);

        if (permissionLocation) {

            mMap.setMyLocationEnabled(false);
            putCameraOnUserLocation();

        }

        mMap.setOnMapClickListener((v) -> {

            if (fijarMarcador) {

                markerDialog = new MarkerDialog(this);
                markerDialog.show();

                lastMarkerLat = v;
                fijarMarcador = false;

            }

        });

    }

    public String getNearestLocation() {

        String nearest = "";
        double[] distances = new double[markers.size()];

        LatLng userRelativeLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

        for (int i = 0; i < distances.length; i++) {

            distances[i] = SphericalUtil.computeDistanceBetween(userRelativeLocation, markers.get(i).getPosition());

        }

        double min = distances[0];
        int indexMin = 0;

        for (int i = 1; i < distances.length; i++) {

            if (distances[i] < min) {

                min = distances[i];
                indexMin = i;

            }

        }


        if (distances[indexMin] < 20) {

            nearest += "\n Usted se encuentra en " + markers.get(indexMin).getTitle();

        } else {

            nearest += "\n El lugar más cercano es " + markers.get(indexMin).getTitle();

        }


        return nearest;

    }

    public void updateInfo() {

        if (markers.size() > 0) {
            textViewInfo.setText(getNearestLocation());
        } else {

            textViewInfo.setText("\n No hay marcadores agregados.");

        }
    }

    // Actualiza la ubicación cada segundo en el mapa.
    public void putCameraOnUserLocation() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(1000);
        request.setFastestInterval(500);

        fusedLocationProviderClient.requestLocationUpdates(request, new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                userLocation = locationResult.getLastLocation();
                LatLng userLastLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                if (!userMarkerAdded) {
                    userMarker = mMap.addMarker(new MarkerOptions().position(userLastLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.userlocation)).title("Mi ubicación"));
                    userMarkerAdded = true;
                } else {
                    userMarker.setPosition(userLastLocation);
                }

                updateInfo();


                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 18));

            }

        }, Looper.myLooper());

    }

    /**
     * Permite verificar si se ha concedido el permiso de la ubicacion del dispositivo. Si se ha concedido antes, entonces la aplicacion
     * continua normal. Sino, entonces se le pide al usuario el permiso para la ubicacion del dispositivo.
     */
    private void getLocationPermission() {

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            permissionLocation = true;

            initMap();

        } else {

            ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_PERMISSION);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case LOCATION_REQUEST_PERMISSION:
                if (permissions.length == 1 && permissions[0].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Se ha permitido el uso de la ubicación del dispositivo", Toast.LENGTH_LONG).show();
                    permissionLocation = true;
                    initMap();
                } else {
                    Toast.makeText(this, "No se ha permitido el uso de la ubicación del dispositivo", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }


    public void addMarker(String titleMarker) {

        Marker marker = mMap.addMarker(new MarkerOptions().position(lastMarkerLat).title(titleMarker).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        markers.add(marker);

    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        if (marker.getTitle().equalsIgnoreCase("mi ubicación")) {
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(userLocation.getLatitude(), userLocation.getLongitude(), 1);
                if (addresses != null) {
                    userMarker.setTitle("Mi ubicación");
                    userMarker.setSnippet(addresses.get(0).getAddressLine(0));
                    userMarker.showInfoWindow();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            LatLng userRelativeLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            double distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), userRelativeLocation);

            marker.setSnippet("Te encuentras a " + String.format("%.2f", distance) + " metros.");
            marker.showInfoWindow();

        }

        return true;
    }
}

