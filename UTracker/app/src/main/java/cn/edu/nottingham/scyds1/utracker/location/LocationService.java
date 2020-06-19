package cn.edu.nottingham.scyds1.utracker.location;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 *  Service class to track the location every 3 seconds
 *  bugs and findings are described in the report
 */
public class LocationService extends Service {
    //region globals
    private final String TAG = getClass().getName();
    private LocationListener locationListener;          //listener to spot when the location is changed
    private LocationManager locationManager;            //location manager helper
    private Location previousLocation;                  //used to compare distances between locations
    private double distance = 0;                        //initial distance
    private double speed = 0.0;                         //initial speed
    private int updateTime = 3000;                      //3000 milliseconds - 3 seconds
    //endregion


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //missing permission because we check it in all activities where the permissions is needed
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location newLocation) {
                Intent i = new Intent("location_update");

                //if first time (start point)
                if(previousLocation == null){
                    previousLocation = newLocation;
                }else{
                    //if not the first time and the person is not standing
                    if(previousLocation.distanceTo(newLocation) != 0){
                        distance = distance + previousLocation.distanceTo(newLocation);

                        //current speed
                        speed = (previousLocation.distanceTo(newLocation) / (updateTime / 1000.0));

                        //update location
                        previousLocation = newLocation;
                    }
                }
                //send back to activity using broadcast
                i.putExtra("location", newLocation);
                i.putExtra("distance", distance);
                i.putExtra("speed", speed);
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if(locationManager != null) {
            //already given permissions
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 0, locationListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection permissions
            locationManager.removeUpdates(locationListener);
        }
    }
}
