package cn.edu.nottingham.scyds1.utracker.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import static android.content.ContentValues.TAG;
import static cn.edu.nottingham.scyds1.utracker.database.WorkoutProviderContract._ID;

/**
 *  Provider class which is responsible for all CRUD operations in the application
 */
public class WorkoutProvider extends ContentProvider {
    //region globals
    private DBHelper mDBHelper;
    Uri insertURI;
    //endregion

    //region static globals (keys) and matcher
    private static final UriMatcher mURIMatcher;
    public static final int WORKOUT_ID = 1;
    public static final int WORKOUT = 2;
    //endregion

    //region urimatcher classification (pay attention to the names)
    static {
        mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mURIMatcher.addURI(cn.edu.nottingham.scyds1.utracker.database.WorkoutProviderContract.AUTHORITY, "workout", WORKOUT_ID);
        mURIMatcher.addURI(cn.edu.nottingham.scyds1.utracker.database.WorkoutProviderContract.AUTHORITY, "workout/#", WORKOUT);
    }
    //endregion

    @Override
    public boolean onCreate() {
        this.mDBHelper = new DBHelper(this.getContext());
        return true;
    }

    //Function from MDP lab to get type of the URI
    @Override
    public String getType(Uri uri) {
        if (uri.getLastPathSegment()==null) {
            return "vnd.android.cursor.dir/WorkoutProviderContract.data.text";
        } else {
            return "vnd.android.cursor.item/WorkoutProviderContract.data.text";
        }
    }

    //region insert() - one of the four functions to control the table. inserts rows.
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String tableName;

        switch(mURIMatcher.match(uri)) {
            case WORKOUT_ID:
                tableName = WorkoutProviderContract.TABLE_NAME_HISTORY;
                break;
            default: tableName = "";
        }

        if(!tableName.isEmpty()) {
            long id = db.insert(tableName, null, values);
            db.close();
            insertURI = ContentUris.withAppendedId(uri, id);

            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(insertURI, null);
            }
        }
        return insertURI;
    }
    //endregion

    //region query() - get the rows from the table based on the selection
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(WorkoutProviderContract.TABLE_NAME_HISTORY);

        Log.d(TAG, selection);
        switch (mURIMatcher.match(uri)) {
            case WORKOUT_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments());
                break;
            case WORKOUT:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (sortOrder == null || sortOrder.equals("")){
            sortOrder = _ID;
        }

        Cursor c = qb.query(mDBHelper.getReadableDatabase(), projection, selection, selectionArgs,
                null, null, sortOrder);

        if (getContext() != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return c;
    }
    //endregion

    //region update() - updates the rows based on the selection
    @Override
    public int update(@NonNull  Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = mDBHelper.getWritableDatabase();
        String tableName = WorkoutProviderContract.TABLE_NAME_HISTORY;
        int rowsUpdated = 0;

        switch (mURIMatcher.match(uri)) {
            case WORKOUT_ID:
                rowsUpdated = sqlDB.update(tableName, values, selection, selectionArgs);
                break;
            case WORKOUT:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if(getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
    //endregion

    //region delete() - delete row(s) based on the selection
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        String tableName = WorkoutProviderContract.TABLE_NAME_HISTORY;

        switch (mURIMatcher.match(uri)){
            case WORKOUT_ID:
                count = mDBHelper.getReadableDatabase().delete(tableName, selection, selectionArgs);
                break;
            case WORKOUT:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if(getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        Log.d(TAG, String.valueOf(count));
        return count;
    }
    //endregion
}