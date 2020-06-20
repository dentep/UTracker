package cn.edu.nottingham.scyds1.utracker.actvities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.Calendar;

import cn.edu.nottingham.scyds1.utracker.R;
import cn.edu.nottingham.scyds1.utracker.database.DBHelper;
import cn.edu.nottingham.scyds1.utracker.database.WorkoutProvider;
import cn.edu.nottingham.scyds1.utracker.database.WorkoutProviderContract;

/**
 *  This class is responsible for displaying analytics -> the data from database
 *  divided into several categories -> all time, this month, and this week
 *
 *  All data is queried from the db with cursors and providercontract support.
 *  Tested on Nexus 2 emulator
 */
public class Analytics extends AppCompatActivity {
    //region globals
    private final String TAG = getClass().getName();
    private static final DecimalFormat df2 = new DecimalFormat("#.##");     //number formatting (eg. 2.22)
    private static final DecimalFormat df1 = new DecimalFormat("#.#");      //number formatting (eg. 2.1)
    DBHelper mDbHelper;
    SQLiteDatabase mDb;
    //endregion

    //values to store times of the current week and month
    long startWeek, endWeek, startMonth, endMonth;

    //UI elements
    TextView allTimeDistance, allTimeDuration,allTimeAvgSpeed, allTimeLongestDuration, allTimeLongestDistance,
             thisWeekDistance, thisWeekDuration, thisWeekAvgSpeed, thisWeekLongestDuration, thisWeekLongestDistance,
             thisMonthDistance, thisMonthDuration, thisMonthAvgSpeed, thisMonthLongestDuration, thisMonthLongestDistance;

    //region onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        //instantiate the db
        mDbHelper = new DBHelper(this);
        mDb = mDbHelper.getWritableDatabase();

        //All time ui elements
        allTimeDistance = findViewById(R.id.allTimeDistance);
        allTimeDuration = findViewById(R.id.allTimeDuration);
        allTimeAvgSpeed = findViewById(R.id.allTimeAvgSpeed);
        allTimeLongestDistance = findViewById(R.id.allTimeLongestDistance);
        allTimeLongestDuration = findViewById(R.id.allTimeLongestDuration);

        //this month ui elements
        thisMonthDistance = findViewById(R.id.thisMonthDistance);
        thisMonthDuration = findViewById(R.id.thisMonthDuration);
        thisMonthAvgSpeed = findViewById(R.id.thisMonthAvgSpeed);
        thisMonthLongestDistance = findViewById(R.id.thisMonthLongestDistance);
        thisMonthLongestDuration = findViewById(R.id.thisMonthLongestDuration);

        //this week ui elements
        thisWeekDistance = findViewById(R.id.thisWeekDistance);
        thisWeekDuration = findViewById(R.id.thisWeekDuration);
        thisWeekAvgSpeed = findViewById(R.id.thisWeekAvgSpeed);
        thisWeekLongestDistance = findViewById(R.id.thisWeekLongestDistance);
        thisWeekLongestDuration = findViewById(R.id.thisWeekLongestDuration);

        //retrieve times of the current weeks/months
        getTimes();

        //UI control for each components
        dataAllTime();
        dataThisMonth();
        dataThisWeek();


    }
    //endregion

    //region dataAllTime() - collect data from all time and display
    private void dataAllTime(){
        String message_placeholder;

        message_placeholder = getTotalDistance(null) + " " + getString(R.string.analytics_unit_meter);
        allTimeDistance.setText(message_placeholder);

        message_placeholder = getTotalDuration(null) + " " + getString(R.string.analytics_unit_second);
        allTimeDuration.setText(message_placeholder);

        message_placeholder = getTotalAvgSpeed(null) + " " + getString(R.string.analytics_unit_metersecond);
        allTimeAvgSpeed.setText(message_placeholder);

        message_placeholder = getLongestRunDistance(null) + " " + getString(R.string.analytics_unit_meter);
        allTimeLongestDistance.setText(message_placeholder);

        message_placeholder = getLongestRunDuration(null) + " " + getString(R.string.analytics_unit_second);
        allTimeLongestDuration.setText(message_placeholder);
    }
    //endregion

    //region dataThisMonth - collect data from this month and display
    private void dataThisMonth(){
        //change start variable here if you dont want to wait until next week/month
        String selection = "date BETWEEN " + startMonth + " and " + endMonth;

        String message_placeholder;
        message_placeholder = getTotalDistance(selection) + " " + getString(R.string.analytics_unit_meter);
        thisMonthDistance.setText(message_placeholder);

        message_placeholder = getTotalDuration(selection) + " " + getString(R.string.analytics_unit_second);
        thisMonthDuration.setText(message_placeholder);

        message_placeholder = getTotalAvgSpeed(selection) + " " + getString(R.string.analytics_unit_metersecond);
        thisMonthAvgSpeed.setText(message_placeholder);

        message_placeholder = getLongestRunDistance(selection) + " " + getString(R.string.analytics_unit_meter);
        thisMonthLongestDistance.setText(message_placeholder);

        message_placeholder = getLongestRunDuration(selection) + " " + getString(R.string.analytics_unit_second);
        thisMonthLongestDuration.setText(message_placeholder);
    }
    //endregion

    //region dataThisWeek() - collect data from this week and display
    private void dataThisWeek(){
        //change start value to see the differences (if you have workouts done already)
        String selection = "date BETWEEN " + startWeek + " and " + endWeek;

        String message_placeholder;
        message_placeholder = getTotalDistance(selection) + " " + getString(R.string.analytics_unit_meter);
        thisWeekDistance.setText(message_placeholder);

        message_placeholder = getTotalDuration(selection) + " " + getString(R.string.analytics_unit_second);
        thisWeekDuration.setText(message_placeholder);

        message_placeholder = getTotalAvgSpeed(selection) + " " + getString(R.string.analytics_unit_metersecond);
        thisWeekAvgSpeed.setText(message_placeholder);

        message_placeholder = getLongestRunDistance(selection) + " " + getString(R.string.analytics_unit_meter);
        thisWeekLongestDistance.setText(message_placeholder);

        message_placeholder = getLongestRunDuration(selection) + " " + getString(R.string.analytics_unit_second);
        thisWeekLongestDuration.setText(message_placeholder);
    }
    //endregion

    //region queries
    //calculate sum distance Value
    public int getTotalDistance(String selection){
        Cursor cursor = mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{"SUM("+WorkoutProviderContract.DISTANCE+")"}, selection, null, null, null, null);

        return returnResult(cursor);
    }

    //calculate sum duration Value
    public int getTotalDuration(String selection){
        Cursor cursor = mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{"SUM("+WorkoutProviderContract.DURATION+")"}, selection, null, null, null, null);

        return returnResult(cursor);
    }

    //calculate sum avgSpeed Value
    public int getTotalAvgSpeed(String selection){
        Cursor cursor = mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{"AVG("+WorkoutProviderContract.AVGSPEED+")"}, selection, null, null, null, null);

        return returnResult(cursor);
    }

    //calculate max duration Value
    public int getLongestRunDuration(String selection){
        Cursor cursor = mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{"MAX("+WorkoutProviderContract.DURATION+")"}, selection, null, null, null, null);

        return returnResult(cursor);
    }

    //calculate max distance Value
    public int getLongestRunDistance(String selection){
        Cursor cursor = mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{"MAX("+WorkoutProviderContract.DISTANCE+")"}, selection, null, null, null, null);

        return returnResult(cursor);
    }
    //endregion

    //function to alleviate repetitiong in cursor retrieval
    public int returnResult(Cursor cursor){
        int result = 0;

        if(cursor.getCount()>0){
            cursor.moveToFirst();
            result = cursor.getInt(0);
        }
        cursor.close();

        return result;
    }

    //get first and last day of the current week and month
    public void getTimes() {
        // set up calendar
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        // get start of this week in milliseconds
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        startWeek = cal.getTimeInMillis();

        // end of the week in millis
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        endWeek = cal.getTimeInMillis();

        //start of the month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        startMonth = cal.getTimeInMillis();

        //end of the month
        cal.add(Calendar.MONTH, 1);
        endMonth = cal.getTimeInMillis();
    }
}
