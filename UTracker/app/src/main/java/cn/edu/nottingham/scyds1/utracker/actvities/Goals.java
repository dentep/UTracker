package cn.edu.nottingham.scyds1.utracker.actvities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;

import cn.edu.nottingham.scyds1.utracker.R;
import cn.edu.nottingham.scyds1.utracker.database.DBHelper;
import cn.edu.nottingham.scyds1.utracker.database.WorkoutProviderContract;

/**
 *  Gamification (is that a word?) class that tells the user their achievements.
 *  The achievements requirements are completely random, not based on any source.
 *
 *  All achievements are the comparisons of selections from the db query
 *  If (with selection) the cursor of the user's workout returns more than 1 or more rows - achievement unlocked
 *
 * TODO: Save the achievements even if the user deletes the history (db) -> look into SharedPreferences
 */
public class Goals extends AppCompatActivity {
    private final String TAG = getClass().getName();
    DBHelper mDbHelper;
    SQLiteDatabase mDb;
    ImageView[] images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        //instantiate the db
        mDbHelper = new DBHelper(this);
        mDb = mDbHelper.getWritableDatabase();

        setUp();
        getAchievements();
    }

    //SET UP achievements and descriptions
    private void setUp(){
        String [] achievementNames = {
                getResources().getString(R.string.MARATHON_BRONZE),
                getResources().getString(R.string.MARATHON_SILVER),
                getResources().getString(R.string.MARATHON_GOLD),
                getResources().getString(R.string.RUNNER_BRONZE),
                getResources().getString(R.string.RUNNER_SILVER),
                getResources().getString(R.string.RUNNER_GOLD),
                getResources().getString(R.string.ENDURANCE_BRONZE),
                getResources().getString(R.string.ENDURANCE_SILVER),
                getResources().getString(R.string.ENDURANCE_GOLD),
                getResources().getString(R.string.TIMEMASTER_BRONZE),
                getResources().getString(R.string.TIMEMASTER_SILVER),
                getResources().getString(R.string.TIMEMASTER_GOLD),
        };

        //images dynamic allocation
        images = new ImageView[achievementNames.length];
        for(int i=0; i< achievementNames.length; i++) {
            String imageID = "img" + i;
            int resID = getResources().getIdentifier(imageID, "id", getPackageName());
            images[i] = (findViewById(resID));
            images[i].setColorFilter(getResources().getColor(R.color.unselected));
            images[i].setTag(i);
            images[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Goals.this, achievementNames[(int)v.getTag()], Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //set the mapping (image, color) and get achievements from queries of data
    private void getAchievements(){
        //hashmap with matching colors for images
        HashMap<ImageView, Integer> mapping = new HashMap<>();
        for(int i = 0; i < images.length;){
            mapping.put(images[i],getResources().getColor(R.color.bronze));
            mapping.put(images[i+1],getResources().getColor(R.color.silver));
            mapping.put(images[i+2],getResources().getColor(R.color.gold));
            i = i + 3;
        }

        //set the queries manually (you can switch selections here to see results)
        Cursor [] queries = {
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, "distance > 100", null, null, null, null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, "distance > 300", null, null, null, null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, "distance > 500", null, null, null, null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, null, null, WorkoutProviderContract.DISTANCE, "SUM(distance) > 10000", null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, null, null, WorkoutProviderContract.DISTANCE, "SUM(distance) > 20000", null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, null, null, WorkoutProviderContract.DISTANCE, "SUM(distance) > 50000", null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, "distance > 300 AND avgSpeed > 9", null, null, null, null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, "distance > 500 AND avgSpeed > 9", null, null, null, null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, "distance > 1000 AND avgSpeed > 9", null, null, null, null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, "duration > 60 AND avgSpeed > 0", null, null, null, null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, "duration > 300 AND avgSpeed > 0", null, null, null, null),
                mDb.query(WorkoutProviderContract.TABLE_NAME_HISTORY, new String[]{WorkoutProviderContract._ID}, "duration > 6000 AND avgSpeed > 0", null, null, null, null),
        };

        //get the achievements that are unlocked
        for (int i = 0; i < queries.length; i++){
            if (queries[i].getCount() > 0) {
                queries[i].moveToFirst();
                images[i].setColorFilter(mapping.get(images[i]));
            }
            queries[i].close();
        }
    }
}
