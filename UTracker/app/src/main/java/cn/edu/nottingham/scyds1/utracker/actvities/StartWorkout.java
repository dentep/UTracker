package cn.edu.nottingham.scyds1.utracker.actvities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import cn.edu.nottingham.scyds1.utracker.R;
import cn.edu.nottingham.scyds1.utracker.database.WorkoutProviderContract;
import cn.edu.nottingham.scyds1.utracker.database.DBHelper;
import cn.edu.nottingham.scyds1.utracker.location.LocationService;
import info.isuru.sheriff.enums.SheriffPermission;
import info.isuru.sheriff.helper.Sheriff;
import info.isuru.sheriff.interfaces.PermissionListener;

/**
 *  This is one of the largest and most enchanced classes in the program.
 *  It controls the database insertion and update operations, maps and multiple UI elements
 *
 *  Tested on Nexus 2 emulator (bug with real device explained in the report
 */
public class StartWorkout extends AppCompatActivity implements OnMapReadyCallback,PermissionListener {
    //region Final Global Variables
    private final String TAG = getClass().getName();
    private static final int ERROR_CODE_AVAILABILITY = 99404;                       //location services unavailable code
    private static final float DEFAULT_ZOOM = 15f;                                  //default zoom in maps (15 is street view)
    private static final int FLAG_CANCELLED = 0;                                    //flag for workout
    private static final int FLAG_FINISHED = 1;                                     //flag for workout
    private static final DecimalFormat df2 = new DecimalFormat("#.##");     //number formatting (eg. 2.22)
    private static final DecimalFormat df1 = new DecimalFormat("#.#");      //number formatting (eg. 2.1)
    private static final float DIM_AMOUNT = 0.9f;                                  //dim of the dialog window
    private static final String ACTIVITY_TYPE_RUN   = "run";                        //code value for activity type (run)
    private static final String ACTIVITY_TYPE_WALK  = "walk";                       //code value for activity type (walk)
    private static final String ACTIVITY_TYPE_MIXED = "mixed";                      //code value for activity type (mixed)
    int REQUEST_SINGLE_PERMISSION = 888;                                            //location permissions request code
    //endregion

    //region UI Elements
    private TextView tv_distance, tv_avgSpeed, tv_goal;
    private Button btn_process;
    //endregion

    //region Additional Globals (db, broadcastreceiver, map)
    private BroadcastReceiver broadcastReceiver;                     //service updates receiver
    private GoogleMap map;                                           //map
    SQLiteDatabase mDb;                                              //database helper
    Sheriff sheriffPermission;
    //endregion

    //region Dialog Elements
    LinearLayout btn_run,btn_walk,btn_mix;                           //buttons (linear layout) for workout types
    TextView workoutNote,workoutGoal, workoutUnits, workoutGoalTitle;//dialog text views
    Button btn_add_goal, btn_remove_goal, btn_ready;                 //dialog buttons
    EditText noteInput;                                              //dialog input for note
    SwitchMaterial switchMaterial;                                   //dialog switch for goal/nogoals
    //endregion

    //region Database values (changeable), the ones that will be used for values.put()
    //some of the variables are local in the insertion method, such as duration
    List<Double> SPEED = new ArrayList<>();             //stores speed to calculate average speed
    long START_TIME;                                    //start time workout
    double DISTANCE;                                    //total distance
    String ACTIVITY_TYPE, NOTE;                         //activity type and note
    int WORKOUT_GOAL = 100;                             //initial goal - 100meter
    //endregion
    
    /**
     * Function to control the map on the screen
     * @param googleMap the map
     */
    //region onMapReady()
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
    }
    //endregion

    /**
     * move camera on the map to the current position
     * @param latLng latitude and longitude
     */
    //region moveCamera()
    private void moveCamera(LatLng latLng){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
    }
    //endregion

    /**
     * managing broadcast receiver - update the gps location
     */
    //region onResume()
    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent.getExtras() != null) {
                        //get the location and update globals
                        Location loc = intent.getExtras().getParcelable("location");
                        DISTANCE = intent.getExtras().getDouble("distance");
                        SPEED.add(intent.getExtras().getDouble("speed"));

                        //display information for speed
                        String message_placeholder;
                        message_placeholder = df2.format(SPEED.get(SPEED.size() - 1)) + " " + getString(R.string.unit_ms);
                        tv_avgSpeed.setText(message_placeholder);

                        //display information for distance
                        message_placeholder = df1.format(DISTANCE) + " " + getString(R.string.unit_m);
                        tv_distance.setText(message_placeholder);

                        //move the camera on the map
                        if(loc != null) {
                            moveCamera(new LatLng(loc.getLatitude(), loc.getLongitude()));
                        }
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }
    //endregion

    /**
     * unregister the receiver if the activity is destroyed andstop the service
     */
    //region onDestroy()
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        stopService(i);
    }
    //endregion

    /**
     * Activity starts here (finding ui elements, checking permissions, etc)
     * @param savedInstanceState saved object to restore
     */
    //region onCreate()
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);


        //db helper
        mDb = new DBHelper(this).getWritableDatabase();

        //find UI elements
        tv_distance = findViewById(R.id.tv_distance);
        tv_avgSpeed = findViewById(R.id.tv_avgSpeed);
        tv_goal = findViewById(R.id.tv_goal);
        btn_process = findViewById(R.id.btn_process);

        //request permissions first
        requestPermission();
    }
    //endregion

    /**
     * Initialize the google map in fragment
     */
    //region initMap()
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if(mapFragment != null) {
            mapFragment.getMapAsync(StartWorkout.this);
        }
    }
    //endregion

    /**
     *  Control the flow of the program by button states
     */
    //region setUpUI()
    private void setUpUI(){
        btn_process.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (btn_process.getText().length() == 0 || btn_process.getText().equals(getString(R.string.btn_process_start))) {
                    //insert first part of contents
                    startAndInsert();
                } else {
                    //update the row (finish data collection)
                    updateAndFinish(FLAG_FINISHED);
                }
            }
        });
    }
    //endregion

    /**
     * A method which controls the inital set up before the workout -> activity type, goal, note.
     * @param context current activity
     */
    //region showMyDialog()
    private void showMyDialog(Context context) {
        //Set up the dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.workout_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        //UI elements
        btn_run = dialog.findViewById(R.id.workout_dialog_btn_run);
        btn_walk = dialog.findViewById(R.id.workout_dialog_btn_walk);
        btn_mix = dialog.findViewById(R.id.workout_dialog_btn_mix);
        workoutNote = dialog.findViewById(R.id.workout_note);
        workoutGoal = dialog.findViewById(R.id.workout_dialog_goal);
        btn_add_goal = dialog.findViewById(R.id.add_more_goal);
        btn_remove_goal = dialog.findViewById(R.id.remove_more_goal);
        noteInput = dialog.findViewById(R.id.editTextNote);
        switchMaterial = dialog.findViewById(R.id.no_goal_swtich);
        workoutUnits = dialog.findViewById(R.id.unit_goal);
        workoutGoalTitle = dialog.findViewById(R.id.goal_tv);
        btn_ready = dialog.findViewById(R.id.btn_ready);

        workoutGoal.setText(String.valueOf(WORKOUT_GOAL));
        noteInput.setBackground(getResources().getDrawable(R.drawable.edittext_bg));

        //btn run selected
        btn_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ACTIVITY_TYPE = ACTIVITY_TYPE_RUN;
                btn_run.setBackgroundColor(getResources().getColor(R.color.item_focused_color));
                btn_mix.setBackgroundColor(getResources().getColor(R.color.item_selector_color));
                btn_walk.setBackgroundColor(getResources().getColor(R.color.item_selector_color));
                workoutNote.setText(R.string.note_run);
            }
        });

        //btn walk selected
        btn_walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ACTIVITY_TYPE = ACTIVITY_TYPE_WALK;
                btn_walk.setBackgroundColor(getResources().getColor(R.color.item_focused_color));
                btn_mix.setBackgroundColor(getResources().getColor(R.color.item_selector_color));
                btn_run.setBackgroundColor(getResources().getColor(R.color.item_selector_color));
                workoutNote.setText(R.string.note_walk);
            }
        });

        //btn mix selected
        btn_mix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ACTIVITY_TYPE = ACTIVITY_TYPE_MIXED;
                btn_mix.setBackgroundColor(getResources().getColor(R.color.item_focused_color));
                btn_run.setBackgroundColor(getResources().getColor(R.color.item_selector_color));
                btn_walk.setBackgroundColor(getResources().getColor(R.color.item_selector_color));
                workoutNote.setText(R.string.note_mixed);
            }
        });

        //btn add more goal selected
        btn_add_goal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //adding to goals
                WORKOUT_GOAL = WORKOUT_GOAL + 100;
                workoutGoal.setText(String.valueOf(WORKOUT_GOAL));
            }
        });

        //btn remove goal selected
        btn_remove_goal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //adding to goals
                if(WORKOUT_GOAL == 100){
                    Toast.makeText(StartWorkout.this,getString(R.string.minimum_goal), Toast.LENGTH_LONG).show();
                }else {
                    WORKOUT_GOAL = WORKOUT_GOAL - 100;
                    workoutGoal.setText(String.valueOf(WORKOUT_GOAL));
                }
            }
        });

        //switch control for selecting goal/no goal
        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    WORKOUT_GOAL = 0;
                    workoutGoal.setVisibility(View.INVISIBLE);
                    btn_add_goal.setVisibility(View.INVISIBLE);
                    btn_remove_goal.setVisibility(View.INVISIBLE);
                    workoutUnits.setVisibility(View.INVISIBLE);
                    workoutGoalTitle.setVisibility(View.INVISIBLE);
                }else{
                    WORKOUT_GOAL = 100;
                    workoutGoal.setText(String.valueOf(WORKOUT_GOAL));
                    workoutGoal.setVisibility(View.VISIBLE);
                    btn_add_goal.setVisibility(View.VISIBLE);
                    btn_remove_goal.setVisibility(View.VISIBLE);
                    workoutUnits.setVisibility(View.VISIBLE);
                    workoutGoalTitle.setVisibility(View.VISIBLE);
                }
            }
        });

        //btn ready selected
        btn_ready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ACTIVITY_TYPE != null) {

                    //set up the goal in main window and finish dialog
                    if(WORKOUT_GOAL == 0){
                        tv_goal.setText(R.string.no_goal);
                    }else {
                        String message_placeholder = WORKOUT_GOAL + " " + getString(R.string.unit_m);
                        tv_goal.setText(message_placeholder);
                    }

                    //set the note
                    NOTE = noteInput.getText().toString();

                    //finish
                    dialog.dismiss();
                }else{
                    Toast.makeText(StartWorkout.this, getString(R.string.not_ready), Toast.LENGTH_LONG).show();
                }
            }
        });


        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.8);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.8);
        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.workout_dialog_bg));
            dialog.getWindow().setLayout(dialogWidth, dialogHeight);
            dialog.getWindow().setDimAmount(DIM_AMOUNT);
        }else{
            Log.e(TAG, "Error getting the dialog window");
        }

        dialog.show();
    }
    //endregion


    /**
     *  A method to insert first set of elements
     */
    //region startAndInsert()
    private void startAndInsert(){
        //change button state
        btn_process.setText(R.string.btn_process_stop);

        //start location service
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        startService(i);

        // Add new entry to workout table
        ContentValues values = new ContentValues();

        //get current time
        START_TIME = System.currentTimeMillis();

        //prepare values to insert in database
        values.put(WorkoutProviderContract.DATE, START_TIME);                       //1 - start time, date
        values.put(WorkoutProviderContract.ACTIVITYTYPE, ACTIVITY_TYPE);            //2 - type
        values.put(WorkoutProviderContract.NOTE, NOTE);                             //3 - note
        values.put(WorkoutProviderContract.GOAL, WORKOUT_GOAL);                     //4 - goal

        //run db insert command
        getContentResolver().insert(WorkoutProviderContract.WORKOUT_URI, values);
    }
    //endregion


    /**
     * A method to finish data collection process and update the db rows
     * @param flag is used to distinguish between cancellation from onBackPressed or 'STOP' button click
     */
    //region updateAndFinish()
    private void updateAndFinish(int flag){
        // new values after workout has finished
        ContentValues values = new ContentValues();

        //selection of the row in db is easier by starttime (faster look than for ID)
        String selection = "date= " + START_TIME;

        //get current time to calculate duration (1000 is 1 second)
        long duration = (System.currentTimeMillis() - START_TIME) / 1000;

        //values that will be updated
        values.put(WorkoutProviderContract.DURATION, df2.format(duration));                  //5 - duration (in seconds)
        values.put(WorkoutProviderContract.DISTANCE, df1.format(DISTANCE));                  //6 - distance
        values.put(WorkoutProviderContract.AVGSPEED, df2.format(calcAverageSpeed(SPEED)));   //7 - avg. speed

        //if the user cancelled workout by pressing back button, add a note
        if(flag == FLAG_CANCELLED){
            values.put(WorkoutProviderContract.NOTE, R.string.cancelled);                    //set a note for cancelled worrkout
        }

        //run db update command
        getContentResolver().update(WorkoutProviderContract.WORKOUT_URI, values, selection, null);

        //end service and change button state
        btn_process.setText(R.string.btn_process_start);
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        stopService(i);
    }
    //endregion

    /**
     *  user pressed back button (needs special care if the process was launched)
     */
    //region onBackPressed()
    @Override
    public void onBackPressed() {
        //end service and change button state if the process started
        if(btn_process.getText() == getString(R.string.btn_process_stop)) {
            //set up for alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
            builder.setCancelable(false);
            builder.setTitle(R.string.dialog_title);
            builder.setMessage(R.string.dialog_subtitle);

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //if user selects "YES", finish the activity with the correct db update
                    updateAndFinish(FLAG_CANCELLED);
                    finish();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //if user select "NO", dismiss dialog and continue
                    dialog.cancel();
                }
            });
            //show alert dialog
            AlertDialog alert = builder.create();
            alert.show();
        }else{
            //workout was never started
            finish();
        }
    }
    //endregion


    /**
     * A method to calculate the average speed
     * @param speed list of all speed that were measured during the the run
     * @return avgSpeed the average speed of the runner
     */
    //region calcAverageSpeed()
    private double calcAverageSpeed(List<Double> speed){
        double avgSpeed;
        double sum = 0;

        for(int i = 0; i < speed.size(); i++){
            sum = sum + speed.get(i);
        }

        avgSpeed = sum / speed.size();

        return avgSpeed;
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
        //if services ara available -> lets go!
        if(servicesCheck()){
            initMap();
            setUpUI();
            //show workout preparation dialog
            showMyDialog(StartWorkout.this);
        }

    }

    //if permissions are not granted, tell the user.
    public void onPermissionsDenied(int requestCode, ArrayList<String> deniedPermissionList) {
        Toast.makeText(StartWorkout.this, R.string.PERMISSION_NOT_GRANTED, Toast.LENGTH_LONG).show();
    }

    //permission class override by Sheriff library
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        sheriffPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //endregion


    /**
     * Check whether the device can call map requests
     * @return boolean true/false depending whether it can or no
     */
    //region servicesCheck()
    public boolean servicesCheck(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(StartWorkout.this);
        if(available == ConnectionResult.SUCCESS){
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(StartWorkout.this, available, ERROR_CODE_AVAILABILITY);
            dialog.show();
        }else{
            Toast.makeText(this, R.string.error_map_requests,Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    //endregion

}
