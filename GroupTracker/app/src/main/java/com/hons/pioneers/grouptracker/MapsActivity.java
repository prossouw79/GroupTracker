package com.hons.pioneers.grouptracker;

import android.location.Location;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        mHandler = new Handler();
        startRepeatingTask();
    }

    Runnable RefreshMarkers = new Runnable() {
        @Override
        public void run() {
            setUpMap(); //this function can change value of mInterval.
            mHandler.postDelayed(RefreshMarkers, mInterval);
        }
    };

    void startRepeatingTask() {
        RefreshMarkers.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(RefreshMarkers);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
            /*mMap.clear();
            List<MapObject> mapObjects = getObjects("myID");
            for(MapObject ob : mapObjects) {
                if (ob instanceof Person) {
                    Person obj = (Person) ob;
                    mMap.addMarker(new MarkerOptions().position(new LatLng(obj.getLat(), obj.getLong())).title(obj.getTitle()).snippet(obj.getSnip()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
                else {
                    MapMarker obj = (MapMarker) ob;
                    mMap.addMarker(new MarkerOptions().position(new LatLng(obj.getLat(), obj.getLong())).title(obj.getTitle()).snippet(obj.getSnip()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
    }

    private List<MapObject> getObjects(String id) {
        Random r = new Random();

        List<MapObject> list = new ArrayList<MapObject>();
        list.clear();

        for (int i = 0; i < 5; i++)
            list.add(new Person(r.nextFloat() * (100 - 20) + 20, r.nextFloat() * (100 - 20) + 20, Integer.toString(r.nextInt())));
        for (int i = 0; i < 5; i++)
            list.add(new MapMarker(r.nextFloat() * (100 - 20) + 20, r.nextFloat() * (100 - 20) + 20, Integer.toString(r.nextInt())));

        return list;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
    }
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private class MapObject
    {
        float lon;
        float lat;
        String title;
        String snip = "snippet here";
    }

    private class Person extends MapObject
    {
        public Person(float lonv, float latv, String titlev)
        {
            super.lon = lonv;
            super.lat = latv;
            super.title = titlev;
        }

        public float getLat() {
            return super.lat;
        }

        public float getLong(){
            return super.lon;
        }

        public String getTitle(){
            return super.title;
        }

        public String getSnip(){
            return super.snip;
        }
    }

    private class MapMarker extends MapObject
    {
        public MapMarker(float lonv, float latv, String titlev)
        {
            super.lon = lonv;
            super.lat = latv;
            super.title = titlev;
        }

        public float getLat() {
            return super.lat;
        }

        public float getLong(){
            return super.lon;
        }

        public String getTitle(){
            return super.title;
        }

        public String getSnip(){
            return super.snip;
        }
    }
}


