package cn.edu.nottingham.scyds1.utracker.database;
import android.net.Uri;

/**
 * This is a support class for the database All URIs are matched with the ones here before CRUD operated.
 */
public class WorkoutProviderContract {
    public static final String AUTHORITY = "cn.edu.nottingham.scyds1.utracker.WorkoutProvider";
    public static final Uri WORKOUT_URI = Uri.parse("content://"+AUTHORITY+"/workout");
    public static final String _ID = "_id";
    public static final String DATE = "date";
    public static final String DURATION = "duration";
    public static final String AVGSPEED = "avgSpeed";
    public static final String ACTIVITYTYPE = "activityType";
    public static final String DISTANCE = "distance";
    public static final String NOTE = "note";
    public static final String GOAL = "goal";
    public static final String TABLE_NAME_HISTORY = "history";
}

