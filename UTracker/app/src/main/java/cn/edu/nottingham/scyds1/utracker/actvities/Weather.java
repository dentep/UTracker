package cn.edu.nottingham.scyds1.utracker.actvities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.kwabenaberko.openweathermaplib.constants.Lang;
import com.kwabenaberko.openweathermaplib.constants.Units;
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper;
import com.kwabenaberko.openweathermaplib.implementation.callbacks.CurrentWeatherCallback;
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cn.edu.nottingham.scyds1.utracker.R;
import cn.edu.nottingham.scyds1.utracker.location.LocationService;
import info.isuru.sheriff.enums.SheriffPermission;
import info.isuru.sheriff.helper.Sheriff;
import info.isuru.sheriff.interfaces.PermissionListener;

import static java.util.Locale.UK;

/**
 *  This class is responsible for displaying current location weather using
 *  openweathermap.org APIs library
 *
 *  Tested on Nexus 2 emulator (real device does not get the weather details due to unknown reason)
 */
public class Weather extends AppCompatActivity implements PermissionListener{
    //region global variables
    private final String TAG = getClass().getName();
    int REQUEST_SINGLE_PERMISSION = 888;
    private BroadcastReceiver broadcastReceiver;
    Location loc;
    OpenWeatherMapHelper helper;
    Sheriff sheriffPermission;
    //endregion

    // UI elements
    TextView sunrise, sunset, humidity, pressure, wind, cloudiness, max_temp, min_temp, curr_temp, date_dt, city, weather_desc;

    //region onCreate() - setting up
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //ask location permissions
        requestPermission();

        //UI elements
        sunrise = findViewById(R.id.sunrise);
        sunset = findViewById(R.id.sunset);
        humidity = findViewById(R.id.humidity);
        pressure = findViewById(R.id.pressure);
        wind = findViewById(R.id.wind);
        cloudiness = findViewById(R.id.cloudiness);
        max_temp = findViewById(R.id.max_temp);
        min_temp = findViewById(R.id.min_temp);
        curr_temp = findViewById(R.id.current_temp);
        date_dt = findViewById(R.id.date_dt);
        city = findViewById(R.id.city);
        weather_desc = findViewById(R.id.weather_status);

        //helper object instantiated
        helper = new OpenWeatherMapHelper(getString(R.string.api_weather));
        helper.setUnits(Units.METRIC);
        helper.setLang(Lang.ENGLISH);
    }
    //endregion

    //region onResume() - managing broadcast receiver - update weather on our location
    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent.getExtras() != null) {
                        //get current location
                        loc = intent.getExtras().getParcelable("location");
                        if (loc != null){
                            //update the weather based on location
                            updateWeather(loc);
                        }
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }
    //endregion

    //region onDestroy() - unregister the receiver if the activity is destroyed and stop location service
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
        //stop location service
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        stopService(i);
    }
    //endregion

    //region onBackPressed() - stop location service (activity is not destroyed)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //stop location service
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        stopService(i);
    }
    //endregion

    //region updateWeather() - update the weather UI
    public void updateWeather(Location loc){
        helper.getCurrentWeatherByGeoCoordinates(loc.getLatitude(), loc.getLongitude(), new CurrentWeatherCallback() {
            @Override
            public void onSuccess(CurrentWeather currentWeather) {
                String message_placeholder;
                //set current data retrieval time
                date_dt.setText(getDateFormat(System.currentTimeMillis()));

                //set the city
                message_placeholder = currentWeather.getName() + ", " + currentWeather.getSys().getCountry();
                city.setText(message_placeholder);

                //set weather status
                weather_desc.setText(currentWeather.getWeather().get(0).getMain());

                //set temperature
                message_placeholder = ((int)currentWeather.getMain().getTemp()) + "°C";
                curr_temp.setText(message_placeholder);

                //set max temp
                message_placeholder = getString(R.string.max_temp) + " " + ((int)currentWeather.getMain().getTempMax()) + "°C";
                max_temp.setText(message_placeholder);

                //set min temp
                message_placeholder = getString(R.string.min_temp) +  " " + ((int)currentWeather.getMain().getTempMin()) + "°C";
                min_temp.setText(message_placeholder);

                //set sunrise
                sunrise.setText(getTimeFormat(currentWeather.getSys().getSunrise()));

                //set sunset
                sunset.setText(getTimeFormat(currentWeather.getSys().getSunset()));

                //set humidity
                message_placeholder = ((int)currentWeather.getMain().getHumidity())  + " %";
                humidity.setText(message_placeholder);

                //set pressure
                message_placeholder = ((int)currentWeather.getMain().getPressure()) + " " + getString(R.string.unit_hpa);
                pressure.setText(message_placeholder);

                //set clouds
                message_placeholder = currentWeather.getClouds().getAll() + " %";
                cloudiness.setText(message_placeholder);

                //set wind
                message_placeholder = currentWeather.getWind().getSpeed() + " " + getString(R.string.unit_ms);
                wind.setText(message_placeholder);
            }

            @Override
            public void onFailure(Throwable throwable) {
                if(throwable.getMessage() != null){
                    Log.v(TAG, throwable.getMessage());
                }else{
                    Log.e(TAG, getString(R.string.WEATHER_THROWABLE_ERROR));
                }
            }
        });
    }
    //endregion

    //region getTimeFormat() - format the epoch time to hours:minutes format
    private String getTimeFormat(long time){
        Date itemDate = new Date(time * 1000);

        return new SimpleDateFormat("hh:mm a", Locale.UK).format(itemDate).toUpperCase();
    }
    //endregion

    //region getDateFormat() - format the epoch time to mm dd, yyyy hh:mm
    private String getDateFormat(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", UK);
        Date formattedDate = new Date(time);

        return sdf.format(formattedDate);
    }
    //endregion

    //region Sheriff library easy permissions - asking for location
    public void requestPermission(){
        sheriffPermission = Sheriff.Builder()
                .with(this)
                .requestCode(REQUEST_SINGLE_PERMISSION)
                .setPermissionResultCallback(this)
                .askFor(SheriffPermission.LOCATION)
                .build();
        sheriffPermission.requestPermissions();
    }

    //if permissions are granted -> launch the location service
    public void onPermissionsGranted(int requestCode, ArrayList<String> acceptedPermissionList) {
        //start location service
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        startService(i);
    }

    //if permissions are not granted, tell the user.
    public void onPermissionsDenied(int requestCode, ArrayList<String> deniedPermissionList) {
        Toast.makeText(Weather.this, R.string.PERMISSION_NOT_GRANTED, Toast.LENGTH_LONG).show();
    }

    //permission class override by Sheriff library
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        sheriffPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //endregion
}