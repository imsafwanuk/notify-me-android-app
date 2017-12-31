package com.karim.safwan.notifyme;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@TargetApi(Build.VERSION_CODES.N)
public class CreateAlarmActivity extends AppCompatActivity {

/** Final Variables**/

/** Static Variables**/
    static boolean isCreateAlarmActivityRunning = false;

/** Plain Old Variables **/
    Button cancel_btn, save_btn;
    Menu currentMenu;
    // repeating days TextView item
    TextView sun, mon, tue, wed, thu, fri, sat;
    TextView tv_date;
    int[] selectedTextViewDayList = new int[7];
    TimePicker timePicker;
    Alarm alarmObj;
    EditText et_location, et_title, et_description;
    Switch repeat_switch, switch_dayLight;
    Intent globalReceiveIntent;
    LinearLayout days_linear_layout;
    ImageView dst_info_img, calendar_img;
    TableRow date_row, day_row;
    int date_picker_year, date_picker_month, date_picker_date;
    boolean doubleBackToExitPressedOnce = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);

        // inflate layout
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater li = LayoutInflater.from(CreateAlarmActivity.this);
        View customView = li.inflate(R.layout.activity_alarm_action_bar, null);
        mActionBar.setCustomView(customView);
        mActionBar.setDisplayShowCustomEnabled(true);

        Calendar classCalendar = Calendar.getInstance();
        classCalendar.setTimeInMillis(System.currentTimeMillis());
        // set current min + 1
        classCalendar.setTimeInMillis(classCalendar.getTimeInMillis()+60000);

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setMinute(classCalendar.get(Calendar.MINUTE));
        if(classCalendar.get(Calendar.HOUR_OF_DAY) <= 11) {
            classCalendar.set(Calendar.HOUR_OF_DAY, classCalendar.get(Calendar.HOUR_OF_DAY) + 12);
        }
        timePicker.setHour(classCalendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setOnTimeChangedListener(timeSetListener);

        repeat_switch = findViewById(R.id.repeat_switch);
        repeat_switch.setOnClickListener(repeatChangeListener);
        et_title = findViewById(R.id.et_title);
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        float density  = getResources().getDisplayMetrics().density;
        int dpWidth  = (int)(outMetrics.widthPixels / density);
        final int maxTitleLength = dpWidth / 12;
        System.out.println("device with "+dpWidth);
        et_title.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxTitleLength)});
        et_title.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                if (s.length() >= maxTitleLength)
                {
                    new AlertDialog.Builder(CreateAlarmActivity.this).setTitle("Character limit exceeded").setMessage("Input cannot exceed more than " + maxTitleLength + " characters.").setPositiveButton(android.R.string.ok, null).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        et_location = findViewById(R.id.et_location);
        final int maxLocationLength = dpWidth / 10;
        et_location.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLocationLength)});
        et_location.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                if (s.length() >= maxLocationLength)
                {
                    new AlertDialog.Builder(CreateAlarmActivity.this).setTitle("Character limit exceeded").setMessage("Input cannot exceed more than " + maxLocationLength + " characters.").setPositiveButton(android.R.string.ok, null).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        et_description = findViewById(R.id.et_description);
        switch_dayLight = findViewById(R.id.switch_dayLight);
        days_linear_layout = findViewById(R.id.days_linear_layout);
        date_row = findViewById(R.id.date_row);
        date_row.setOnClickListener(showCalendar);
        day_row = findViewById(R.id.day_row);
        alarmObj = null;




        date_picker_year = classCalendar.get(Calendar.YEAR);
        date_picker_month = classCalendar.get(Calendar.MONTH);
        date_picker_date = classCalendar.get(Calendar.DAY_OF_MONTH);
        tv_date = findViewById(R.id.tv_date);
        tv_date.setText(new SimpleDateFormat("E, dd/MM/yyyy").format(classCalendar.getTime()));

        this.setupBtn();
        this.setupRepeatingDaysOnClick();

        /**Get intent and deal with it accordingly**/
        globalReceiveIntent = getIntent();

        if (globalReceiveIntent.getStringExtra("alarmAction").equals("create-alarm")) {
            System.out.println("create new alarm");
//            alarmObj = alarmObj = Alarm.getAlarmInstance();
//            alarmObj.setAlarmTimeInMIllis(System.currentTimeMillis());
//            setUpSameAlarmView(alarmObj);
        } else if (globalReceiveIntent.getStringExtra("alarmAction").equals("edit-alarm")) {
            int editIndex = globalReceiveIntent.getIntExtra("editIndex", -1);
            System.out.println("edit existing alarm of index: " + editIndex);

            Bundle returnBundle = globalReceiveIntent.getExtras();
            Alarm a = returnBundle.getParcelable("editing-alarm");
            setUpSameAlarmView(a);

        } else if (globalReceiveIntent.getStringExtra("alarmAction").equals("copy-alarm")) {
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

        } else {
            System.out.println("default call to new activity.");
        }

    }


    /**
     * Function: Assigns click listener to cancel and save button.
     * And finishes activity task and returns to parent activity from where the intent came.
     * <p>
     * Assumption:StartActivityForResult is used to launch the intent that this class gets.
     * Stimuli: Called by onCreate method.
     */
    private void setupBtn() {
        // set up dst help icon
        dst_info_img = findViewById(R.id.dst_info_img);
        dst_info_img.setOnClickListener(dstHelpDialog);


        cancel_btn = (Button) findViewById(R.id.cancel_btn);
        save_btn = (Button) findViewById(R.id.save_btn);

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Clicked cancelled");

                Intent saveIntent = new Intent();
                saveIntent.putExtra("alarm", "val");

                setResult(RESULT_CANCELED, saveIntent);
                finish();

            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Clicked saved");
                Intent saveIntent = new Intent();

                // create alarm instance and bundle it
                Bundle bundleObj = saveAlarmInstance();
                saveIntent.putExtras(bundleObj);

                // if edit alarm
                if (globalReceiveIntent.hasExtra("editIndex")) {
                    int editIndex = globalReceiveIntent.getIntExtra("editIndex", -1);
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
     * bundles them and passes it back, so it could be sent to startup activity.
     * Return: Bundle, containing an alarm object containing all alarm info.
     * Stimuli: called when save button is clicked.
     */
    private Bundle saveAlarmInstance() {
        Bundle bundleObj = new Bundle();
        if (alarmObj == null)
            alarmObj = Alarm.getAlarmInstance();

        alarmObj.setAlarmTimeInMIllis(getAlarmTimeInMillis());
        alarmObj.setTitle(et_title.getText().toString());
        alarmObj.setDescription(et_description.getText().toString());
        alarmObj.setLocation(et_location.getText().toString());
        alarmObj.repeatingAlarmDaysList = selectedTextViewDayList;
        if(repeat_switch.isChecked()) {
            int count = 0;
            for(int i : selectedTextViewDayList) {
                if(i == 1) {
                    count++;
                    break;
                }
            }
            if(count == 0)
                repeat_switch.setChecked(false);
        }

        alarmObj.setRepeat(repeat_switch.isChecked());
        alarmObj.setAlarmSet(true);

        bundleObj.putParcelable("alarm", alarmObj);
        return bundleObj;
    }

    private long getAlarmTimeInMillis() {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        ca.set(Calendar.MINUTE, timePicker.getMinute());
        ca.set(date_picker_year, date_picker_month, date_picker_date);

        return ca.getTimeInMillis();
    }


    /**
     * Function: Sets up onclick methods for repeating days, sun - sat.
     * Stimuli: Its an init method, called by onCreate method.
     */
    private void setupRepeatingDaysOnClick() {
        for (int i = 0; i < days_linear_layout.getChildCount(); i++) {
            TextView day = (TextView) days_linear_layout.getChildAt(i);
            day.setOnClickListener(repeatingDaysOnClickListener);
        }
        sun = findViewById(R.id.sun);
//        sun.setOnClickListener(repeatingDaysOnClickListener);
        mon = findViewById(R.id.mon);
//        mon.setOnClickListener(repeatingDaysOnClickListener);
        tue = findViewById(R.id.tue);
//        tue.setOnClickListener(repeatingDaysOnClickListener);
        wed = findViewById(R.id.wed);
//        wed.setOnClickListener(repeatingDaysOnClickListener);
        thu = findViewById(R.id.thu);
//        thu.setOnClickListener(repeatingDaysOnClickListener);
        fri = findViewById(R.id.fri);
//        fri.setOnClickListener(repeatingDaysOnClickListener);
        sat = findViewById(R.id.sat);
//        sat.setOnClickListener(repeatingDaysOnClickListener);


    }

    /**
     * Function: Decides what happens when any of the repeating days are clicked.
     */
    private View.OnClickListener repeatingDaysOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView tv = (TextView) view;

            // remove day
            if (tv.getCurrentTextColor() == getResources().getColor(R.color.myColorPrimaryDark)) {
                selectedTextViewDayList[convertStringToCalendarDay(getResources().getResourceEntryName(tv.getId())) - 1] = 0;
                tv.setTextColor(getResources().getColor(R.color.darker_grey));
                System.out.println("Removing: " + getResources().getResourceEntryName(tv.getId()));
            } else {
                // add day
                selectedTextViewDayList[convertStringToCalendarDay(getResources().getResourceEntryName(tv.getId())) - 1] = 1;
                tv.setTextColor(getResources().getColor(R.color.myColorPrimaryDark));
                System.out.println("Adding: " + getResources().getResourceEntryName(tv.getId()));
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

        // repeating days
        if (obj.isRepeat()) {
            repeat_switch.setChecked(true);
            repeat_switch.callOnClick();
            System.out.println("Rep days present");
            int[] arr = obj.getRepeatingAlarmDays();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == 1) {
                    days_linear_layout.getChildAt(i).callOnClick();
                    System.out.println("Rep days are: " + ((TextView) days_linear_layout.getChildAt(i)).getText());

                }

            }
        } else {
            repeat_switch.setChecked(false);
            repeat_switch.callOnClick();
            Calendar ca = Calendar.getInstance();
            ca.setTimeInMillis(obj.getTimeMillis());
            date_picker_year = ca.get(Calendar.YEAR);
            date_picker_month = ca.get(Calendar.MONTH);
            date_picker_date = ca.get(Calendar.DATE);
            tv_date.setText(new SimpleDateFormat("E, dd/MM/yyyy").format(ca.getTime()));
        }


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

    /**
     * Function: Takes a String of days name and converts them into int, 1 = sun, 7 = sat.
     *
     * @param str should be lowercase 3 letter days, sun, mon, tue ...
     *            Return: Any one int from 1..7;
     */
    private int convertStringToCalendarDay(String str) {
        switch (str) {
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


    View.OnClickListener dstHelpDialog = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            View helpView = View.inflate(CreateAlarmActivity.this, R.layout.dst_help_layout, null);
            View title = View.inflate(CreateAlarmActivity.this, R.layout.dialog_header_dst_help, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateAlarmActivity.this);
            builder.setCustomTitle(title)
                    .setView(helpView)
                    .setCancelable(true);
            builder.show();
        }
    };


    View.OnClickListener repeatChangeListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Switch s = (Switch) view;
            // repeat on, hide date row and show day row
            if (s.isChecked()) {
                s.setChecked(true);
                date_row.setVisibility(View.GONE);
                day_row.setVisibility(View.VISIBLE);
            } else {
                day_row.setVisibility(View.GONE);
                date_row.setVisibility(View.VISIBLE);
                s.setChecked(false);
            }
        }
    };


    private TimePicker.OnTimeChangedListener timeSetListener = new TimePicker.OnTimeChangedListener() {

        @Override
        public void onTimeChanged(TimePicker timePicker, int hour, int min) {
                Calendar ca = Calendar.getInstance();
                ca.set(date_picker_year, date_picker_month, date_picker_date, hour, min, 0);
                String ss = "1 Alarm time: " + ca.getTimeInMillis() + " My time: " + System.currentTimeMillis()+ " Diff: " +Long.toString(Math.abs( ca.getTimeInMillis() - System.currentTimeMillis()));
                System.out.println(ss);
                if(ca.getTimeInMillis() - System.currentTimeMillis() <= 0) {
//                    ca.setTimeInMillis(ca.getTimeInMillis() + 86400000L);
                    ca.add(Calendar.DATE, 1);
                    System.out.println(new SimpleDateFormat("E, dd/MM/yyyy").format(ca.getTime()));

                    date_picker_date = ca.get(Calendar.DATE);
                    date_picker_year = ca.get(Calendar.YEAR);
                    date_picker_month = ca.get(Calendar.MONTH);
                    date_picker_date = ca.get(Calendar.DAY_OF_MONTH);

                    tv_date.setText(new SimpleDateFormat("E, dd/MM/yyyy").format(ca.getTime()));
                }
            }
    };

    /**
     * Function: Stores the hr and min, in 24hr format in an arr and returns that.
     * Return: arr[2] where, arr[0] = hr and arr[1] = min.
     */
    private int[] getAlarmTime() {
        int[] arr = new int[2];
        int hr = timePicker.getHour();
        arr[0] = hr;
        int min = timePicker.getMinute();
        arr[1] = min;
        return arr;
    }

    private DatePickerDialog.OnDateSetListener  mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day);
            tv_date.setText(new SimpleDateFormat("E, dd/MM/yyyy").format(newDate.getTime()));

            // save date in class scope
            date_picker_date = day;
            date_picker_month = month;
            date_picker_year = year;
        }
    };

    View.OnClickListener showCalendar = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            DatePickerDialog dialog = new DatePickerDialog(
                    CreateAlarmActivity.this,
                    mDateSetListener,
                    date_picker_year, date_picker_month, date_picker_date);
            dialog.getWindow().setBackgroundDrawableResource(R.color.white);
            dialog.show();
            }
        };



    @Override
    public void onBackPressed(){
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(CreateAlarmActivity.this, "Quick double click BACK to exit", Toast.LENGTH_SHORT).show();
        if(isCreateAlarmActivityRunning)
            showExitDialog();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
                System.out.println("double is now " + doubleBackToExitPressedOnce);
            }
        }, 400);
    }

    private void showExitDialog() {
        View title = View.inflate(CreateAlarmActivity.this, R.layout.dialog_header_create_alarm_exit, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateAlarmActivity.this);
        builder.setCustomTitle(title)
                .setCancelable(true)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        save_btn.callOnClick();
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        cancel_btn.callOnClick();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {

                        if (keyCode == KeyEvent.KEYCODE_BACK ) {
                            if (doubleBackToExitPressedOnce) {
                                dialog.cancel();
                                onBackPressed();
                                return true;
                            }else {
                                dialog.cancel();
                                doubleBackToExitPressedOnce = true;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        doubleBackToExitPressedOnce=false;
                                        System.out.println("double is now " + doubleBackToExitPressedOnce);
                                    }
                                }, 400);
                            }
                        }
                        return true;
                    }
                });

        //        .setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialogInterface) {
//
//            }
//        })
        if(isCreateAlarmActivityRunning)
            builder.show();


    };

    @Override
    public void onStart() {
        super.onStart();
        isCreateAlarmActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();

        isCreateAlarmActivityRunning = false;
    }

}