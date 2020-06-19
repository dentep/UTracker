package cn.edu.nottingham.scyds1.utracker.actvities;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.richpath.RichPath;
import com.richpath.RichPathView;

import cn.edu.nottingham.scyds1.utracker.R;


/**
 *  This class is a launcher class and works as a an app menu presentation class
 *  User has several options to start with the main option (start workout) in the middle
 *  More on the classes you can find in the report
 *
 */
public class MainActivity extends AppCompatActivity {
    //region globals
    private final String TAG = getClass().getName();
    private static final float DIM_AMOUNT = 0.9f;
    //endregion

    // UI elements
    RichPath btn_history, btn_weather, btn_goals, btn_start, btn_analytics;
    TextView title;

    //region onCreate()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up title
        title = findViewById(R.id.app_name);
        setUpTitle();

        //get paths
        RichPathView richPathView = findViewById(R.id.path_view);
        btn_history = richPathView.findRichPathByName(getString(R.string.btn_history));
        btn_start = richPathView.findRichPathByName(getString(R.string.btn_start));
        btn_weather = richPathView.findRichPathByName(getString(R.string.btn_weather));
        btn_analytics = richPathView.findRichPathByName(getString(R.string.btn_analytics));
        btn_goals = richPathView.findRichPathByName(getString(R.string.btn_goals));

        //set up the buttons listeners
        setUpUI();

    }
    //endregion

    //region setUpTitle() - giving title a 'fancy' look
    private void setUpTitle(){
        TextPaint paint = title.getPaint();
        float width = paint.measureText(getString(R.string.app_name));

        Shader textShader = new LinearGradient(0, 0, width, title.getTextSize(),
                new int[]{
                        getResources().getColor(R.color.one),
                        getResources().getColor(R.color.two),
                        getResources().getColor(R.color.three),
                        getResources().getColor(R.color.four),
                        getResources().getColor(R.color.five),
                }, null, Shader.TileMode.REPEAT);
        title.getPaint().setShader(textShader);
    }
    //endregion

    //region Menu buttons controlling Intents
    private void setUpUI(){
        btn_start.setOnPathClickListener(new RichPath.OnPathClickListener(){
            @Override
            public void onClick(RichPath richPath){
                Intent i = new Intent(getApplicationContext(), StartWorkout.class);
                startActivity(i);
            }
        });

        btn_history.setOnPathClickListener(new RichPath.OnPathClickListener(){
            @Override
            public void onClick(RichPath richPath){
                Intent i = new Intent(getApplicationContext(), History.class);
                startActivity(i);
            }
        });

        btn_analytics.setOnPathClickListener(new RichPath.OnPathClickListener(){
            @Override
            public void onClick(RichPath richPath){
                Intent i = new Intent(getApplicationContext(), Analytics.class);
                startActivity(i);
            }
        });

        btn_weather.setOnPathClickListener(new RichPath.OnPathClickListener(){
            @Override
            public void onClick(RichPath richPath){
               Intent i = new Intent(getApplicationContext(), Weather.class);
               startActivity(i);
            }
        });

        btn_goals.setOnPathClickListener(new RichPath.OnPathClickListener(){
            @Override
            public void onClick(RichPath richPath){
                Intent i = new Intent(MainActivity.this, Goals.class);
                startActivity(i);
            }
        });
    }
    //endregion
}
