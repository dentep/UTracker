package cn.edu.nottingham.scyds1.utracker.actvities;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cn.edu.nottingham.scyds1.utracker.RecyclerAdapter;
import cn.edu.nottingham.scyds1.utracker.CardItem;
import cn.edu.nottingham.scyds1.utracker.R;
import cn.edu.nottingham.scyds1.utracker.database.DBHelper;
import cn.edu.nottingham.scyds1.utracker.database.WorkoutProviderContract;

/**
 *  This class is responsible for displaying the past workout ordered by the newest ones
 *  History class uses RecyclerView to display the contents and live adapter to update the
 *  contents in real time.
 *
 *  All data is queried from the db with cursors and providercontract support.
 *  Tested on Nexus 2 emulator (bug with real devices included in the report)
 */
public class History extends AppCompatActivity {
    //region global control elemenents (lists,layouts,adapters,db)
    private  final String TAG = getClass().getName();
    private ArrayList<CardItem> mList;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    DBHelper mDbHelper;
    SQLiteDatabase mDb;
    private static final float DIM_AMOUNT = 0.9f;
    //endregion

    //region onCreate()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        createList();
        buildRecyclerView();
    }
    //endregion

    //region removeItem() - removes the item from recycler list and db
    public void removeItem(int position) {
        //selection by start time in db
        String selection = "date= " + mList.get(position).getDate();

        //run db delete command
        getContentResolver().delete(WorkoutProviderContract.WORKOUT_URI, selection, null);

        mList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }
    //endregion

    //region createList() - populating the list with the entries from database
    public void createList() {
        mList = new ArrayList<>();

        //instantiate the db
        mDbHelper = new DBHelper(this);
        mDb = mDbHelper.getWritableDatabase();

        //get all our db elements
        Cursor cursor = mDb.query("history", new String[]{"_id", "date", "duration", "avgSpeed", "activityType", "distance", "note", "goal"},
                null, null, null, null, "_id" + " DESC");

        //go through the cursor one by asssigning values to every variable
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long date = cursor.getLong(1);
                    Log.d(TAG, String.valueOf(date));
                    long duration = cursor.getLong(2);
                    double avgSpeed = cursor.getDouble(3);
                    String activityType = cursor.getString(4);
                    double distance = cursor.getDouble(5);
                    String note = cursor.getString(6);
                    int goal = cursor.getInt(7);

                    //then we add it to our list of 'cards'
                    mList.add(new CardItem(date, duration, avgSpeed, activityType, distance, note, goal));
                } while (cursor.moveToNext());
            }
            //free the cursor
            cursor.close();
        }
    }
    //endregion

    //region buildRecyclerView() - prepare the rv, layouts and adapters
    public void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new RecyclerAdapter(mList, this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showCard(History.this, position);
            }

            @Override
            public void onDeleteClick(int position) {
                removeItem(position);
            }
        });
    }
    //endregion

    //region showCard() - dialog to show clicked card (button on it)
    private void showCard(Context context, int position){
        CardItem currentCard = mList.get(position);

        //Set up the dialog
        final Dialog dialog = new Dialog(context, R.style.CardDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.card_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        //size of the window
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.9);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.7);

        //setting the background and dim
        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.card_dialog_bg));
            dialog.getWindow().setLayout(dialogWidth, dialogHeight);
            dialog.getWindow().setDimAmount(DIM_AMOUNT);
        }else{
            Log.e(TAG, "Error getting the dialog window");
        }


        //finding ui elements
        TextView tvDate = dialog.findViewById(R.id.history_dialog_date);
        TextView tvDistance = dialog.findViewById(R.id.history_dialog_distance);
        TextView tvGoal = dialog.findViewById(R.id.history_dialog_goal);
        TextView tvAvgSpeed = dialog.findViewById(R.id.history_dialog_avgSpeed);
        TextView tvDuration = dialog.findViewById(R.id.history_dialog_duration);
        EditText etNote = dialog.findViewById(R.id.history_dialog_etNote);
        //setting the date in the dialog
        tvDate.setText(currentCard.getFormattedDate(currentCard.getDate(), context));

        //setting the distance in the dialog
        String message_placeholder = currentCard.getDistance() + " " + getString(R.string.unit_m);
        tvDistance.setText(message_placeholder);

        //setting the goal in the dialog
        tvGoal.setText(currentCard.getFormattedGoal(currentCard.getGoal(),context));

        //setting the avg speed in the dialog
        message_placeholder = currentCard.getAvgSpeed() + " " + getString(R.string.unit_ms);
        tvAvgSpeed.setText(message_placeholder);

        //setting the duration in the dialog
        message_placeholder = currentCard.getDuration() + " " + getString(R.string.unit_s);
        tvDuration.setText(message_placeholder);

        //setting the note in the dialog but do not allow typing
        etNote.setText(currentCard.getNote());
        etNote.setBackgroundColor(getResources().getColor(R.color.disabled_editText));
        etNote.setEnabled(false);

        //allow to change the note only the user clicks on the 'pencil' icon
        ImageButton btn_editNote = dialog.findViewById(R.id.btn_editText);
        btn_editNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //style differently and allow typing
                etNote.setBackgroundColor(getResources().getColor(R.color.white));
                etNote.setEnabled(true);
                etNote.setTextColor(getResources().getColor(R.color.backgroundColor));
                Toast.makeText(context, R.string.enabled_et, Toast.LENGTH_LONG).show();
            }
        });

        //button click to finish annotation
        Button btn_ok = dialog.findViewById(R.id.btn_ready);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newNote = etNote.getText().toString();

                //update adapter and also database (later use of History class needs to retreive correct note)
                if(!newNote.equals(currentCard.getNote())){
                    //update ui and adapter
                    Toast.makeText(context, R.string.note_changed, Toast.LENGTH_LONG).show();
                    mList.get(position).changeNote(newNote);
                    mAdapter.notifyItemChanged(position);

                    // update the db
                    ContentValues values = new ContentValues();
                    String selection = "date=" + mList.get(position).getDate();
                    values.put(WorkoutProviderContract.NOTE, newNote);

                    //run db update command
                    getContentResolver().update(WorkoutProviderContract.WORKOUT_URI, values, selection, null);
                }

                dialog.dismiss();
            }
        });

        //show dialog
        dialog.show();
    }
    //endregion
}
