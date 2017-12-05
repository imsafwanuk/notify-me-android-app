package com.example.safwan.onetimealarm;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

public class CreateAlarmActivity extends AppCompatActivity {

    Button cancel_btn, save_btn;
    Menu currentMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater li = LayoutInflater.from(CreateAlarmActivity.this);
        View customView = li.inflate(R.layout.activity_alarm_action_bar, null);
        mActionBar.setCustomView(customView);
        mActionBar.setDisplayShowCustomEnabled(true);


        // onclick of cancel and save



        /**Get intent and deal with it accordingly**/
        Intent i = getIntent();
        if(i.getStringExtra("alarmAction").equals("create")) {
            System.out.println("create new alarm");
        } else if(i.getStringExtra("alarmAction").equals("edit")) {
            System.out.println("edit existing alarm");
        }else {
            System.out.println("default call to new activity.");
        }

        this.setupBtn();

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_startup_delete, menu);
//        currentMenu = menu;
//        return true;
//    }


    private void setupBtn() {
        cancel_btn = (Button) findViewById(R.id.cancel_btn);
        save_btn = (Button) findViewById(R.id.save_btn);

        cancel_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                System.out.println("Clicked cancelled");
                Intent cancelIntent = new Intent(CreateAlarmActivity.this, StartupActivity.class);
                cancelIntent.putExtra("decision","cancel");
                startActivity(cancelIntent);
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                System.out.println("Clicked saved");
                Intent saveIntent = new Intent();
                int[] arr = getAlarmTime();
                saveIntent.putExtra("hr",arr[0]);
                saveIntent.putExtra("min",arr[1]);
                setResult(RESULT_OK, saveIntent);
                finish();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private int[] getAlarmTime() {
        int[] arr = new int[2];
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        int hr = timePicker.getHour();
        arr[0] = hr;
        int min = timePicker.getMinute();
        arr[1] = min;
        return arr;
    }
}
