package com.example.safwan.onetimealarm;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

public class CreateAlarmActivity extends AppCompatActivity {

    Button cancel_btn, save_btn;
    Menu currentMenu;
    // repeating days TextView item
    TextView sun, mon, tue, wed, thu, fri, sat;
    int[] selectedTextViewDayList = new int[7];
    TimePicker timePicker;
    Alarm alarmObj;
    EditText et_location, et_title, et_description;
    Switch switch_dayLight;
    Intent globalReceiveIntent;

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

        // add text view 7 days of week
//        dayTextViewList.add(sun); dayTextViewList.add(mon); dayTextViewList.add(tue);
//            dayTextViewList.add(wed); dayTextViewList.add(thu);
//            dayTextViewList.add(fri); dayTextViewList.add(sat);


        timePicker = (TimePicker) findViewById(R.id.timePicker);
        et_title = (EditText) findViewById(R.id.et_title);
        et_location = (EditText) findViewById(R.id.et_location);
        et_description = (EditText) findViewById(R.id.et_description);
        switch_dayLight = (Switch) findViewById(R.id.switch_dayLight);
        alarmObj = null;


        /**Get intent and deal with it accordingly**/
        globalReceiveIntent = getIntent();

        if(globalReceiveIntent.getStringExtra("alarmAction").equals("create-alarm")) {
            System.out.println("create new alarm");

        } else if(globalReceiveIntent.getStringExtra("alarmAction").equals("edit-alarm")) {
            int editIndex =  globalReceiveIntent.getIntExtra("editIndex",-1);
            System.out.println("edit existing alarm of index: " + editIndex);

            Bundle returnBundle = globalReceiveIntent.getExtras();
            Alarm a = returnBundle.getParcelable("editing-alarm");
            setUpSameAlarmView(a);

        } else if(globalReceiveIntent.getStringExtra("alarmAction").equals("copy-alarm")) {
            System.out.println("copy existing alarm with different time");

            Bundle returnBundle = globalReceiveIntent.getExtras();
            Alarm a = returnBundle.getParcelable("copying-alarm");

            // clone alarm (can just use the alarm from return bundle, but im implementing prototype pattern)
            try {
                setUpSameAlarmView(a.clone());
                System.out.println("cloned existing alarm");
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

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

                // create alarm instance and bundle it
                Bundle bundleObj = saveAlarmInstance();
                saveIntent.putExtras(bundleObj);

                // if edit alarm
                if(globalReceiveIntent.hasExtra("editIndex")) {
                    int editIndex = globalReceiveIntent.getIntExtra("editIndex",-1);
                    saveIntent.putExtra("editIndex", editIndex);
                }

                saveIntent.putExtra("check", "has");
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
        if(alarmObj == null)
            alarmObj = Alarm.getAlarmInstance();

        int[] arr = getAlarmTime();
        alarmObj.setAlarmTime(arr[0], arr[1]);
        alarmObj.setTitle(et_title.getText().toString());
        alarmObj.setDescription(et_description.getText().toString());
        alarmObj.setLocation(et_location.getText().toString());
        alarmObj.setChangeWithDayLightSavings(switch_dayLight.isChecked());
        alarmObj.repeatingAlarmDaysList = selectedTextViewDayList;

        bundleObj.putParcelable("alarm", alarmObj);
        return bundleObj;
    }

    /**
     * Function: Stores the hr and min, in 24hr format in an arr and returns that.
     * Return: arr[2] where, arr[0] = hr and arr[1] = min.
     */
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
            if(tv.getCurrentTextColor() == getResources().getColor(R.color.colorPrimary)) {
                selectedTextViewDayList[convertStringToCalendarDay(getResources().getResourceEntryName(tv.getId())) - 1] = 0;
                tv.setTextColor(getResources().getColor(R.color.darker_grey));
            }
            else {
                selectedTextViewDayList[convertStringToCalendarDay(getResources().getResourceEntryName(tv.getId())) - 1] = 1;
                tv.setTextColor(getResources().getColor(R.color.colorPrimary));
            }


        }
    };


    /**
     * Function: Ensures all the relevant fields in view have same contents as alarm passed to it.
     * Parameter: obj: Alarm
     * Stimuli: launches when copy-alarm or edit-alarm intent is given to this activity
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void setUpSameAlarmView(Alarm obj) {
        // timer
        timePicker.setHour(obj.getHrOfDay());
        timePicker.setMinute(obj.getMin());

        // title
        et_title.setText(obj.getTitle());

        // location
        et_location.setText(obj.getLocation());

        // description
        et_description.setText(obj.getDescription());

        // change with day light
        switch_dayLight.setChecked(obj.isChangeWithDayLightSavings());

        // remove this alarm
        alarmObj = obj;
    }

    private int convertStringToCalendarDay(String str) {
        switch(str) {
            case "sun":
                return Calendar.SUNDAY;
            case "mon":
                return Calendar.MONDAY;
            case "tue":
                return Calendar.TUESDAY;
            case "wed":
                return Calendar.WEDNESDAY;
            case "thu":
                return Calendar.THURSDAY;
            case "fri":
                return Calendar.FRIDAY;
            case "sat":
                return Calendar.SATURDAY;
            default:
                return -1;
        }
    }
}