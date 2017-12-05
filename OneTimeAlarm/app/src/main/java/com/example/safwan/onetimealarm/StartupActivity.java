package com.example.safwan.onetimealarm;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

public class StartupActivity extends AppCompatActivity {

    /** Final Variables**/
    final static int ALARM_ID_START = 2000;
    final static int CHECKBOX_ID_START = 1000;

    /** Static Variables**/
    protected static int totalAlarmRows = 0;
    private static boolean isDeleteSet = false;

    FloatingActionButton create_alarm_btn;
    TextView alarm_time_1;
    RelativeLayout startup_relative_layout;
    Button btn, btn1;
    TableLayout alarm_table;
    Switch cb_switch;
    int cbId = CHECKBOX_ID_START;

    private Menu currentMenu;

    /** Intent Request code usage **/
    // code = 1, usage = create new alarm
    // code = 2, usage = edit existing alarm

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        alarm_table = (TableLayout) findViewById(R.id.alarm_table);
        cb_switch = (Switch) findViewById(R.id.cb_switch);
        create_alarm_btn = (FloatingActionButton) findViewById(R.id.create_alarm_btn);


        // demo button
        btn = (Button) findViewById(R.id.btn);
        btn1 = (Button) findViewById(R.id.btn1);
        btn.setOnClickListener(callMe);
        btn1.setOnClickListener(fuckMe);


        // cb_switch onclickListener
        cb_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
//                    setDeleteMenu();
                } else {
                }
            }
        });


        create_alarm_btn .setOnClickListener(goToCreateAlarmActivity);
    }


    /**
     * Function: Creates a new Create Alarm Activity and goes there. Request Code = 1, and waits for result
     * Stimuli: Launches when create_alarm_button is clicked.
     */
    protected View.OnClickListener goToCreateAlarmActivity = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.out.println("Clicked alarm creator");
            Intent createAlarmIntent = new Intent(StartupActivity.this, CreateAlarmActivity.class);
            createAlarmIntent.putExtra("alarmAction", "create");
            startActivityForResult(createAlarmIntent, 1);
        }
    };





    private View.OnClickListener callMe = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            System.out.println("Call me ;)");
            createAlarmRow();
        }
    };

    private View.OnClickListener fuckMe = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_startup, menu);
        currentMenu = menu;
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {

        if(isDeleteSet) {
            ActivityCompat.invalidateOptionsMenu(StartupActivity.this);
            getMenuInflater().inflate(R.menu.menu_startup_delete, menu);

        } else {
            ActivityCompat.invalidateOptionsMenu(StartupActivity.this);
            getMenuInflater().inflate(R.menu.menu_startup, menu);
        }

        return true;
    }

    /**
     * Function: Sets delete option on action bar if input == true, else removes delete option.
     * Stimuli: Launches when an alarm row is long pressed.
     */
    public void setDeleteMenu(boolean val) {
        isDeleteSet = val;  // set static variable, used by onPrepareOptionsMenu
        onPrepareOptionsMenu(currentMenu);
    }

    /**
     * Function: Disables on/off switch for each alarm when delete button is present.
     * Stimuli: Should always be called before addDeleteBoxes()
     *          If called after, then TableRow will have 3 children instead of 2.
     */
    protected void  disableOnOffSwitch() {
        for(int i = 0; i < alarm_table.getChildCount(); i++) {
            TableRow tr = (TableRow) alarm_table.getChildAt(i);

            if (tr.getChildAt(1) instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) tr.getChildAt(1);
                if(ll.getChildAt(1) instanceof Switch) {
                    Switch s = (Switch) ll.getChildAt(ll.getChildCount() - 1);
                    System.out.println(s.getText());
//                    if(s.getVisibility() == View.VISIBLE)
                        s.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    /**
     * Function: Enables on/off switch for each alarm when delete button is destroyed.
     * Stimuli: Should always be called after removeDeleteBoxes().
     *          If called before, then TableRow will have 3 children instead of 2.
     */
    protected void  enableOnOffSwitches() {
        for (int i = 0; i < alarm_table.getChildCount(); i++) {
            TableRow tr = (TableRow) alarm_table.getChildAt(i);

            if (tr.getChildAt(1) instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) tr.getChildAt(1);
                if(ll.getChildAt(1) instanceof Switch) {
                    Switch s = (Switch) ll.getChildAt(ll.getChildCount() - 1);
//                    if(s.getVisibility() == View.INVISIBLE)
                        System.out.println(s.getText());
                        s.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Function: Adds "delete checkboxes" to all the alarm rows.
     * Stimuli: Launches when an alarm row is long pressed.
     */
    protected void addDeleteBoxes() {
        for(int i = 0; i < alarm_table.getChildCount(); i++) {
            TableRow tr = (TableRow) alarm_table.getChildAt(i);

            // create new checkbox
            CheckBox cb = new CheckBox(StartupActivity.this);
            cb.setId(cbId++);
            tr.addView(cb,0);
        }
    }



    /**
     * Function: Deletes "delete checkboxes" from all the alarm rows.
     * Stimuli: Launches when delete button is pressed.
     */
    protected void removeDeleteBoxes() {
        for(int i = 0; i < alarm_table.getChildCount(); i++) {
            TableRow tr = (TableRow) alarm_table.getChildAt(i);

            if(tr.getChildAt(0) instanceof CheckBox) {
                tr.removeViewAt(0);
                cbId--;
            }
        }
    }


    /**
     * Function: Adds a new alarm row with all its neccesary contents.
     * Stimuli: Launches when a new alarm is created. Most probably called from onActivityResult
     */
    protected void createAlarmRow() {

        // linear vertical layout
        LinearLayout linear_vertical_layout = new LinearLayout(StartupActivity.this);
//        linear_vertical_layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linear_vertical_layout.setOrientation(LinearLayout.VERTICAL);
//        linear_vertical_layout.setGravity(Gravity.LEFT);

            // TextView to display time
            TextView tv_time = new TextView(StartupActivity.this);
            tv_time.setLayoutParams(new ViewGroup.LayoutParams(
                140,
                40));
            tv_time.setText(Integer.toString(ALARM_ID_START+totalAlarmRows++));
            linear_vertical_layout.addView(tv_time,0);

            // TextView to display title
            TextView tv_title = new TextView(StartupActivity.this);
//            tv_title.setLayoutParams(new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));
            tv_title.setText("My title");
            linear_vertical_layout.addView(tv_title,1);

        // linear horizontal layout
        LinearLayout linear_horizontal_layout = new LinearLayout(StartupActivity.this);
//        linear_horizontal_layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linear_horizontal_layout.setOrientation(LinearLayout.HORIZONTAL);

            // TextView to display days
            TextView tv_day = new TextView(StartupActivity.this);
            tv_day.setLayoutParams(new ViewGroup.LayoutParams(
                    200,
                ViewGroup.LayoutParams.MATCH_PARENT));
            tv_day.setText("S M T");
            linear_horizontal_layout.addView(tv_day,0);
        
            // switch to display on/off
            Switch row_switch = new Switch(StartupActivity.this);
            linear_horizontal_layout.addView(row_switch,1);

        //add 2 liner layout to row
        TableRow tr = new TableRow(StartupActivity.this);
//        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT);
//        tr.setLayoutParams(lp);

        tr.addView(linear_vertical_layout);
        tr.addView(linear_horizontal_layout);

        // add row click listener
        tr.setOnClickListener(alarmRowClickListener);
        // add row long click listener
        tr.setOnLongClickListener(alarmRowLongClickListener);

        // add table row to alarm table
        alarm_table.addView(tr);
        System.out.println("Row added");
    }

    /**
     * Function: When an alarm row is long pressed, this method does the following:
     *              removes on/off alarm switches,
     *              adds delete boxes,
     *              checks the delete box of clicked row,
     *              change option menu to give delete option,
     *              hide FAB create_alarm_btn,
     * Stimuli: When an alarm row is long pressed.
     */
    View.OnClickListener alarmRowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.out.println("D: "+isDeleteSet);
            if(isDeleteSet) {
                setCheck((TableRow) view);
            }else {
                // go to new activity
                Intent editAlarmIntent = new Intent(StartupActivity.this, CreateAlarmActivity.class);
                editAlarmIntent.putExtra("alarmAction","edit");
                startActivityForResult(editAlarmIntent, 2);
            }
        }
    };


    /**
     * Function: When an alarm row is long pressed, this method does the following:
     *              removes on/off alarm switches,
     *              adds delete boxes,
     *              checks the delete box of clicked row,
     *              change option menu to give delete option,
     *              hide FAB create_alarm_btn,
     * Stimuli: When an alarm row is long pressed.
     */
    View.OnLongClickListener alarmRowLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            // removes on/off alarm switches
            disableOnOffSwitch();

            // hide FAB create_alarm_btn
            create_alarm_btn.setVisibility(View.INVISIBLE);

            // adds delete boxes
            addDeleteBoxes();

            // checks the delete box of clicked row
            setCheck((TableRow) view);

            // change option menu to give delete option
            setDeleteMenu(true);

            return true;
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_alarm:
                performMenuDeleteAction();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Function: Checks the given row's checkbox.
     * Input: tr:TableRow
     * Stimuli: When alarm row is long pressed.
     * Assumption: Assume checkbox is at index 0 of TableRow.
     */
    private void setCheck(TableRow tr) {
        CheckBox cb = (CheckBox) tr.getChildAt(0);
        if(cb.isChecked())
            cb.setChecked(false);
        else
            cb.setChecked(true);
    }

    /**
     * Function: When the Delete option is clicked on options menue, it does the following:
     *              remove delete options menu,
     *              removes all the selected alarms,
     *              removes all the delete boxes,
     *              adds on/off switches,
     *              show FAB create_alarm_btn,
     */
    private void performMenuDeleteAction() {
        // remove delete options menu
        setDeleteMenu(false);

        // removes all the selected alarms
        removeSelectedAlarms();

        // removes all the delete boxes
        removeDeleteBoxes();

        // adds on/off switches
        enableOnOffSwitches();

        // show FAB create_alarm_btn
        create_alarm_btn.setVisibility(View.VISIBLE);
    }


    /**
     * Function: Responsible for deleting all the alarm rows that has a checked delete box.
     * Stimuli: Called when delete options manu is clicked.
     */
    private void removeSelectedAlarms() {
        for(int i = alarm_table.getChildCount() - 1; i >= 0; i--) {
            TableRow tr = (TableRow) alarm_table.getChildAt(i);
            if(tr.getChildAt(0) instanceof CheckBox) {
                CheckBox cb = (CheckBox) tr.getChildAt(0);

                // if checked, then delete row
                if(cb.isChecked()) {
                    alarm_table.removeViewAt(i);
                }
            }

        }
    }

//    @Override
//    protected void onCreate(final Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_startup);
//
//        alarm_time_1 = (TextView) findViewById(R.id.alarm_time_1);
//        TableRow alarm_row_1 = (TableRow) findViewById(R.id.alarm_row_1);
//
//        create_alarm_btn = (FloatingActionButton) findViewById(R.id.create_alarm_btn);
//
//        TableLayout alarm_table = (TableLayout) findViewById(R.id.alarm_table );
//        alarm_table.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                TableLayout alarm_table = (TableLayout) findViewById(R.id.alarm_table);
////                alarm_table.removeViewAt(0);
//                CheckBox cb = (CheckBox) findViewById(R.id.checkBox4);
//                cb.setVisibility(View.INVISIBLE);
//                CheckBox cb5 = (CheckBox) findViewById(R.id.checkBox5);
//                cb5.setVisibility(View.INVISIBLE);
//                CheckBox cb6 = (CheckBox) findViewById(R.id.checkBox6);
//                cb6.setVisibility(View.INVISIBLE);
//                return false;
//            }
//        });
//
//
//
//        button6 = (Button) findViewById(R.id.button6);
//        button6.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onCreateDialog(savedInstanceState).show();
//                TableLayout alarm_table = (TableLayout) findViewById(R.id.alarm_table);
//                alarm_table.removeViewAt(0);
//            }
//        });
//
//
////        alarm_row_1.setOnLongClickListener(new View.OnLongClickListener() {
////            @Override
////            public boolean onLongClick(View view) {
////
////                System.out.println( "I have been long clicked");
////                onCreateDialog(savedInstanceState).show();
////                System.out.println( alarm_table.getChildCount());
////                System.out.println("ID:"+alarm_table.getChildAt(0).getId());
//////                TableRow n = (TableRow)findViewById(R.id.t2);
//////                alarm_table.removeViewAt(2);
//////                alarm_table.addView(n,0);
////                alarm_table.removeViewAt(0);
//////                alarm_table.removeViewAt(1);
////                return false;
////            }
////        });
//
//    }
////2131230750
//
//
//
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        // Use the Builder class for convenient dialog construction
//        AlertDialog.Builder builder = new AlertDialog.Builder(StartupActivity.this);
//        builder.setMessage("Fire")
//                .setPositiveButton("Firey", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        System.out.println( "ZE MISSILES!");
//                    }
//                })
//                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        System.out.println( "Cancellll");
//                    }
//                });
//        // Create the AlertDialog object and return it
//        return builder.create();
//    }
//
//
//
//
//    }
//
//


}
