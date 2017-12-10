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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

public class CreateAlarmActivity extends AppCompatActivity {

    Button cancel_btn, save_btn;
    Menu currentMenu;
    // repeating days TextView item
    TextView sun, mon, tue, wed, thu, fri, sat;

    AlarmDemo alarmObj;

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
        } else if(i.getStringExtra("alarmAction").equals("copy")) {
            System.out.println("copy existing alarm with different time");
        }else {
            System.out.println("default call to new activity.");
        }

        this.setupBtn();
        this.setupRepeatingDaysOnClick();

    }

    private void setupBtn() {
        cancel_btn = (Button) findViewById(R.id.cancel_btn);
        save_btn = (Button) findViewById(R.id.save_btn);

        cancel_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                System.out.println("Clicked cancelled");

                Intent saveIntent = new Intent();
                saveIntent.putExtra("alarm","val");

                setResult(RESULT_CANCELED, saveIntent);
                finish();

            }
        });

        save_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                System.out.println("Clicked saved");
                Intent saveIntent = new Intent();

                Bundle bundleObj = saveAlarmInstance();
                saveIntent.putExtras(bundleObj);

                setResult(RESULT_OK, saveIntent);
                finish();
            }
        });
    }


    /**
     * Function: This method gets items from View and puts them in alarmObj,
     *           bundles them and passes it back, so it could be sent to startup activity.
     * Return: Bundle, containing an alarm object containing all alarm info.
     * Stimuli: called when save button is clicked.
     */
    private Bundle saveAlarmInstance() {
        Bundle bundleObj = new Bundle();
        alarmObj = new AlarmDemo();

        int[] arr = getAlarmTime();

        if(arr[0] > 12) {
            arr[0] -= 12;
            alarmObj.setAm_pm(0);
        } else {
            alarmObj.setAm_pm(1);
        }

        alarmObj.setHr(arr[0]);
        alarmObj.setMin(arr[1]);

        bundleObj.putParcelable("new-alarm", alarmObj);
        return bundleObj;
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


    /**
     * Function: Sets up onclick methods for repeating days, sun - sat.
     * Stimuli: Its an init method, called by onCreate method.
     */
    private void setupRepeatingDaysOnClick() {
        sun = findViewById(R.id.sun);
        sun.setOnClickListener(repeatingDaysOnClickListener);
        mon = findViewById(R.id.mon);
        mon.setOnClickListener(repeatingDaysOnClickListener);
        tue = findViewById(R.id.tue);
        tue.setOnClickListener(repeatingDaysOnClickListener);
        wed = findViewById(R.id.wed);
        wed.setOnClickListener(repeatingDaysOnClickListener);
        thu = findViewById(R.id.thu);
        thu.setOnClickListener(repeatingDaysOnClickListener);
        fri = findViewById(R.id.fri);
        fri.setOnClickListener(repeatingDaysOnClickListener);
        sat = findViewById(R.id.sat);
        sat.setOnClickListener(repeatingDaysOnClickListener);


    }

    /**
     * Function: Decides what happens when any of the repeating days are clicked.
     */
     private View.OnClickListener repeatingDaysOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView tv = (TextView)view;
            System.out.println(getResources().getResourceEntryName(tv.getId()));
            if(tv.getCurrentTextColor() == getResources().getColor(R.color.colorPrimary))
                tv.setTextColor(getResources().getColor(R.color.darker_grey));
            else
                tv.setTextColor(getResources().getColor(R.color.colorPrimary));

        }
    };


}