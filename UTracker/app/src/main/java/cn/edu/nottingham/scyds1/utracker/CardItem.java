package cn.edu.nottingham.scyds1.utracker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 *  Support class to manage the database elements. Mostly getters.
 *  One setter for the note (annotation from the user)
 */
public class CardItem {
    //region globals
    private long mDate;
    private long mDuration;
    private double mAvgSpeed;
    private String mActivityType;
    private double mDistance;
    private String mNote;
    private int mGoal;
    private static final String ACTIVITY_TYPE_RUN = "run";
    private static final String ACTIVITY_TYPE_WALK = "walk";
    //endregion

    //region constructor
    public CardItem(long date, long duration, double avgSpeed, String activityType,
                    double distance, String note, int goal) {
        mDate = date;
        mDuration = duration;
        mAvgSpeed = avgSpeed;
        mActivityType = activityType;
        mDistance = distance;
        mNote = note;
        mGoal = goal;
    }
    //endregion

    //region getters
    //getters for each row elements
    public void changeNote(String note) {
        mNote = note;
    }
    public long getDate() {
        return mDate;
    }
    public long getDuration() {
        return mDuration;
    }
    public double getAvgSpeed(){
        return mAvgSpeed;
    }
    public String getActivityType(){
        return mActivityType;
    }
    public double getDistance(){
        return mDistance;
    }
    public String getNote(){
        return mNote;
    }
    public int getGoal(){
        return mGoal;
    }
    //endregion


    //get formatted message for distance display
    public String getFormattedDistance(double distance, Context context){
        String text = context.getString(R.string.distance) + " " + distance + " " + context.getString(R.string.unit_m);
        return text;
    }

    //get formatted date in a form of mm dd, yyyy hh:mm
    public String getFormattedDate(long dateLong, Context context){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.UK);
        Date date = new Date(dateLong);

        return sdf.format(date);
    }

    //get formatted message for average speed display
    public String getFormattedSpeed(double speed, Context context){
        String text = context.getString(R.string.speed) + " " + speed + " " + context.getString(R.string.unit_ms);
        return text;
    }

    //get formatted message for duration display
    //TODO: future work might include formatted time (3664 seconds -> 1 hour, 1 minute and 4 seconds)
    public String getFormattedDuration(double duration, Context context){
        String text = context.getString(R.string.duration) + " " + duration + " " + context.getString(R.string.unit_s);
        return text;
    }

    //change the card thumbnail to the activity type image that was presented in the workout
    public Drawable getFormattedImage(String activityType, Context context){
        Drawable res;

        if(activityType.equals(ACTIVITY_TYPE_RUN)){
            res = context.getResources().getDrawable(R.drawable.run);
            res.setTint(context.getResources().getColor(R.color.orange));
        }else if(activityType.equals(ACTIVITY_TYPE_WALK)){
            res = context.getResources().getDrawable(R.drawable.walk);
            res.setTint(context.getResources().getColor(R.color.yellow));
        }else{
            res = context.getResources().getDrawable(R.drawable.mixed);
            res.setTint(context.getResources().getColor(R.color.blue));
        }

        return res;
    }

    public String getFormattedGoal(int goal, Context context){
        String text = goal + " " + context.getString(R.string.unit_m);
        return text;
    }
}
