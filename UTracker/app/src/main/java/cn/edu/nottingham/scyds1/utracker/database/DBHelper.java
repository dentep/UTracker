package cn.edu.nottingham.scyds1.utracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.edu.nottingham.scyds1.utracker.R;

public class DBHelper extends SQLiteOpenHelper {
    //just a little helper (always pay attention to the names in this class!)
    //region DBHelper() - helper
    public DBHelper(Context context) {
        super(context, context.getString(R.string.dbName), null, 1);
    }
    //endregion

    //region onCreate() - database start
    @Override
    public void onCreate(SQLiteDatabase db) {
        String tableHistory = "history";
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + tableHistory
                + " (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,date long," +
                "duration long, avgSpeed double, activityType String, distance double, note String, goal int);");
    }
    //endregion

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}