package com.example.safwan.onetimealarm;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StartupActivity extends AppCompatActivity {

    /** Final Variables**/
    final static int ALARM_ID_START = 2000;
    final static int CHECKBOX_ID_START = 1000;

    /** Static Variables**/
    protected static int totalAlarmRows = 0;
    private static boolean isDeleteSet = false;
    protected static int dialogViewIndex;
    protected static ArrayList <Alarm> alarmObjList;// = new ArrayList<Alarm>();

    /** Plain Old Variables**/
    FloatingActionButton create_alarm_btn;
    TextView alarm_time_1;
    RelativeLayout startup_relative_layout;
    Button btn;
    TableLayout alarm_table;
    int cbId = CHECKBOX_ID_START;

    private Menu currentMenu;
    Alarm alarmObj;

    /** Intent Request code usage **/
    // code = 1, usage = create new alarm
    // code = 2, usage = edit existing alarm
    // code = 3, usage = copy an alarm with different time

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        System.out.println("on create");

        alarm_table = (TableLayout) findViewById(R.id.alarm_table);
        create_alarm_btn = (FloatingActionButton) findViewById(R.id.create_alarm_btn);

        loadData();
        // check for saved alarm objects
        if(savedInstanceState != null && savedInstanceState.containsKey("alarmObjList")) {
            System.out.println("saved alarms found!");
            alarmObjList = savedInstanceState.getParcelableArrayList("alarmObjList");
            initAlarmTable();
        }else if(savedInstanceState != null && savedInstanceState.containsKey("save")) {
            System.out.println("saved found!");
        }


        // demo button
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(callMe);

        create_alarm_btn .setOnClickListener(createNewAlarm);
    }


    /**
     * Function: Creates a new Create Alarm Activity and goes there. Request Code = 1, and waits for result
     * Stimuli: Launches when create_alarm_button is clicked.
     */
    protected View.OnClickListener createNewAlarm = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.out.println("Clicked alarm creator");
            Intent creatAlarmIntent = new Intent(StartupActivity.this, CreateAlarmActivity.class);
            creatAlarmIntent.putExtra("alarmAction", "create-alarm");
            gotoCreateAlarmActivity("createNewAlarm", "create-alarm", creatAlarmIntent);
        }
    };


    /**
     * Function: Handles activity for result intents.
     *           if(code == 1 && OK) Creates alarm valid.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            System.out.println("Back to startup from save click.");

            Bundle returnBundle = data.getExtras();
            Alarm parceledAlarm = returnBundle.getParcelable("alarm");
            insertAlarm(parceledAlarm);

        }else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            System.out.println("Back to startup from save click, on edit");

            Bundle returnBundle = data.getExtras();
            Alarm parceledAlarm = returnBundle.getParcelable("alarm");

            // edit existing alarm
            if(data.hasExtra("editIndex")) {
                int editIndex = data.getIntExtra("editIndex",0);
                updateAlarm(parceledAlarm, editIndex);
            }


        }else if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            System.out.println("Back to startup from save click, on copy");

            Bundle returnBundle = data.getExtras();
            Alarm parceledAlarm = returnBundle.getParcelable("alarm");
            insertAlarm(parceledAlarm);

        }else {
            System.out.println("Back to startup from cancel click.");
        }
    }


    private View.OnClickListener callMe = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            System.out.println("Call me ;)");
            insertAlarm(new Alarm());
//            try {
//                alarmObj = alarmObjList.get(0).clone();
//                createAlarmRow(alarmObj);
//                alarmObjList.add(alarmObj);
//                for(Alarm a : alarmObjList) {
//                    System.out.println(System.identityHashCode(System.identityHashCode(a)));
//                }
//            } catch (CloneNotSupportedException e) {
//                e.printStackTrace();
//            }

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
        menu.clear();

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
    protected TableRow createAlarmRow(Alarm alarmObj) {

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

            tv_time.setText(alarmObj.getTimeString());
            linear_vertical_layout.addView(tv_time,0);

            // TextView to display title
            TextView tv_title = new TextView(StartupActivity.this);
            tv_title.setText(alarmObj.getTitle());
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

            row_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    Switch s = (Switch) compoundButton;
                    LinearLayout tempL =  (LinearLayout) s.getParent();
                    TableRow tempRow = (TableRow) tempL.getParent();
                    int switchIndex = alarm_table.indexOfChild(tempRow);
                    if(switchIndex >= 0) {
                        System.out.println("ALarm: " + alarm_table.indexOfChild(tempRow));
                        alarmObjList.get(switchIndex).setAlarmSet(isChecked);
                    }
                }
            });
            linear_horizontal_layout.addView(row_switch,1);
            row_switch.setChecked(alarmObj.isAlarmSet());


        //add 2 liner layout to row
        TableRow tr = new TableRow(StartupActivity.this);

        tr.addView(linear_vertical_layout);
        tr.addView(linear_horizontal_layout);

        // add row click listener
        tr.setOnClickListener(alarmRowClickListener);
        // add row long click listener
        tr.setOnLongClickListener(alarmRowLongClickListener);

        // add table row to alarm table
        return tr;
    }

    /**
     * Function: If isDeleteSet == true, then the clicked row gets checked for delete,
     *           Else, goes to create alarm activity to be edited.
     * Stimuli: When an alarm row is clicked.
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
                editAlarmIntent.putExtra("alarmAction","edit-alarm");
                editAlarmIntent.putExtra("editIndex", alarm_table.indexOfChild(view));

                // get alarm and put it in bundle, and put bundle in intent
                Bundle bundleObj = new Bundle();
                bundleObj.putParcelable("editing-alarm", alarmObjList.get(alarm_table.indexOfChild(view)));
                editAlarmIntent.putExtras(bundleObj);

                gotoCreateAlarmActivity("alarmRowClickListener", "edit-alarm", editAlarmIntent);
            }
        }
    };


    /**
     * Function: When an delete option is pressed, this method does the following, if alarms are present:
     *              removes on/off alarm switches,
     *              adds delete boxes,
     *              checks the delete box of clicked row,
     *              change option menu to give delete option,
     *              hide FAB create_alarm_btn,
     * Stimuli: When an alarm row is long pressed.
     */
    private void setAlarmDeleteView() {

        /** CHECK FOR ALARM 1ST **/
        if(alarmObjList.size() == 0)
            return;

        // removes on/off alarm switches
        disableOnOffSwitch();

        // hide FAB create_alarm_btn
        create_alarm_btn.setVisibility(View.INVISIBLE);

        // adds delete boxes
        addDeleteBoxes();

        // change option menu to give delete option
        setDeleteMenu(true);
    }




    View.OnLongClickListener alarmRowLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
            dialogViewIndex = alarm_table.indexOfChild(view);
//            AlertDialog.Builder builder = new AlertDialog.Builder(StartupActivity.this, R.style.dialog_light);
            AlertDialog.Builder builder = new AlertDialog.Builder(StartupActivity.this);
            builder.setTitle("Options")
                    .setItems(R.array.planets_array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int itemIndex) {
                            if(itemIndex == 0) {
                                // copy alarm with different time
                                System.out.println("copy dialog");

                                // create alarm object bundle for copy alarm
                                Bundle bundleObj = new Bundle();
                                bundleObj.putParcelable("copying-alarm", alarmObjList.get(dialogViewIndex));

                                // create intent and put bundle in
                                Intent copyAlarmIntent = new Intent(StartupActivity.this, CreateAlarmActivity.class);
                                copyAlarmIntent.putExtra("alarmAction", "copy-alarm");
                                copyAlarmIntent.putExtras(bundleObj);

                                gotoCreateAlarmActivity("alarmRowLongClickListener", "copy-alarm", copyAlarmIntent);

                            }else if(itemIndex == 1) {
                                // delete the alarm
                                System.out.println("delete dialog at index: " + dialogViewIndex);
                                removeAlarm(dialogViewIndex);
                            }
                        }
                    });
            builder.show();
            return true;
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_alarm:
                System.out.println("delete ops");
                setAlarmDeleteView();
                return true;
            case R.id.settings:
                System.out.println("settings ops");
                return true;
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

        alarmObj = null;
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
                    removeAlarm(i);
                }
            }

        }
    }

    /**
     * Function: Responsible for deleting only the alarm, whose index was passed through.
     * Stimuli: Called when delete options manu is clicked,
     *          Called when delete options is clicked from long click dialog.
     */
    protected void removeAlarm(int alarmIndex) {
        alarm_table.removeViewAt(alarmIndex);
        alarmObjList.remove(alarmIndex);
//        for(Alarm a : alarmObjList) {
//            System.out.println(a.getHr());
//        }
    }

    /**
     * Function: Sets up the entire alarm table, alarm objects taken from alarmObjList.
     * Stimuli: Called when activity starts, orientation changes
     */
    protected void initAlarmTable() {
        for(Alarm a : alarmObjList) {
//            System.out.println();
            alarm_table.addView(createAlarmRow(a));
            System.out.println("Row added");
        }
    }





    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("saving instance!");
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        savedInstanceState.putString("save","yes");
    }


    @Override
    public void onPause(){
        System.out.println("on pause");
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("alarmObjList", alarmObjList);
        onSaveInstanceState(bundle);
        super.onPause();
    }

    @Override
    public void onResume() {
//        System.out.println("on resume");
        super.onResume();
    }

    @Override
    public void onRestart() {
//        System.out.println("on Restart");
        super.onRestart();
    }


    @Override
    public void onStart() {
        super.onStart();
//        System.out.println("on Start");
    }


    @Override
    public void onStop() {
        super.onStop();
        saveData();
//        System.out.println("on stop");
    }

    @Override
    public void onDestroy() {
//        System.out.println("on destroy");
        super.onDestroy();
    }

    private void saveData() {
        SharedPreferences sharedPref = getSharedPreferences("alarmSharedPreferences ", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(alarmObjList);
        editor.putString("alarmObjList", json);
        editor.commit();
    }
//
    private void loadData() {
        SharedPreferences sharedPref = getSharedPreferences("alarmSharedPreferences ", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("alarmObjList", null);
        Type type = new TypeToken<ArrayList<Alarm>>(){}.getType();
        alarmObjList = gson.fromJson(json, type);

        if(alarmObjList == null) {
            alarmObjList = new ArrayList<Alarm>();
        }else {
            initAlarmTable();
        }
    }


    /**
     * Function: This method will be used to insert a new alarm.
     *           This method encapsulates the 2 other methods which will help to keep track of
     *           alarm rows and alarm objects in the app.
     *           This method invokes the following 2 mthods,
     *              createAlarmRow(alarmObj)
     *              alarmObjList.add(alarmObj)
     */
    protected void insertAlarm(Alarm obj) {
        alarm_table.addView(createAlarmRow(obj));
        System.out.println("Row added");
        alarmObjList.add(obj);
    }

    protected void updateAlarm(Alarm obj, int index) {
        alarmObjList.set(index, obj);
        updateAlarmRow(createAlarmRow(obj), index);
    }

    protected void updateAlarmRow(TableRow tr, int index) {
        alarm_table.removeViewAt(index);
        alarm_table.addView(tr,index);
        System.out.println("Row edited!");
    }


    /**
     * Function: This is the method where all activity change from this current to CreateAlarmActivity will take place.
     * Stimuli: Will be called from different parts of this activity when needed.
     * Parameters: methodName: String   // will tell from which method an intent is requested from.
     *             ops: String          // will tell why it wants to switch activity.
     */
    private void gotoCreateAlarmActivity(String methodName, String ops, Intent i) {
        System.out.printf("Intent change requested by: %s \n",methodName);


        switch(ops) {
            case "create-alarm":
                startActivityForResult(i, 1);
                break;

            case "edit-alarm":
                startActivityForResult(i, 2);
                break;

            case "copy-alarm":
                startActivityForResult(i, 3);
                break;

            default:
                System.out.printf("Ops '%s' not supported in '%s' method \n",ops, "gotoCreateAlarmActivity");
        }
    }

}

