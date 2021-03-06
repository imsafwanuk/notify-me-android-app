package com.karim.safwan.notifyme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by safwan on 14/12/2017.
 */

public class MainAlarmFragment extends Fragment {

/** Final Variables**/
    final static NotifyService notifyServiceManager = new NotifyService();  // for all alarm and notification requests

/** Static Variables**/
    private static boolean isDeleteSet = false; //if actionbar shows only delete on top right, then this is true. else false
    protected static int dialogViewIndex;   // a var that passes index of TableRow that was long pressed to launch a dialog box.
    protected static Alarm[] alarmObjList = new Alarm [Alarm.INSTANCE_LIMIT];    // holds n fixed number of alarms
    private static View thisView;
    private static Activity mainAlarmActivity;

/** Plain Old Variables**/
    HashMap<String, ArrayList> locationMap;     // a location name maps to an array of alarms, with same location.
    FloatingActionButton create_alarm_btn;      // FAB with a + sign
    Button chgbtn;                              // temp btn that deletes all table rows and alarm objects from list.
    TableLayout alarm_table;                    // Table layout ref in main alarm fragment.
    Alarm alarmObj;                             // not sure if i need this :p


    /** Intent Request code usage **/

    // code = 1, usage = create new alarm
    // code = 2, usage = edit existing alarm
    // code = 3, usage = copy an alarm with different time

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thisView = (View) inflater.inflate(R.layout.fragment_main_alarm, container, false);
        alarm_table = (TableLayout) thisView.findViewById(R.id.alarm_table);

        loadData();

        // check for saved alarm objects
        if(savedInstanceState != null && savedInstanceState.containsKey("alarmObjList")) {
            System.out.println("saved alarms found!F");
            Parcelable[] parcels = savedInstanceState.getParcelableArray("alarmObjList");
                for ( int i=0; i < parcels.length; i++ ){
                    alarmObjList[i] = (Alarm) parcels[i];
                }

            initAlarmTable();
            System.out.println("View is: "+ mainAlarmActivity);
        }else if(savedInstanceState != null && savedInstanceState.containsKey("save")) {
            System.out.println("No obj list found!");
        }else {
            System.out.println("No save found!");
        }

        // init add button
        create_alarm_btn = (FloatingActionButton) thisView.findViewById(R.id.create_alarm_btn);
        create_alarm_btn.setOnClickListener(createNewAlarm);

        // demo button
/*
        chgbtn = (Button) thisView.findViewById(R.id.chngbtn);
        chgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Alarm[] n = new Alarm[Alarm.INSTANCE_LIMIT];
                alarmObjList = Arrays.copyOf(n, n.length);
                alarm_table.removeAllViews();
            }
        });

        create_alarm_btn .setOnClickListener(createNewAlarm);
*/
        return thisView;
    }



    /**
     * Function: Sets up the entire alarm table. Each alarm objects are taken from alarmObjList.
     * Stimuli: Called when activity starts, orientation changes
     */
    protected void initAlarmTable() {
        for(Alarm a : alarmObjList) {
            if(a != null) {
                a.removeIdFromQ();
                TableRow tr = createAlarmRow(a);
                alarm_table.addView(tr);
                alarm_table.addView(createLowerBorderRow());
                System.out.println("Row added back");
            }
        }
        addPaddedRow();
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("saving instance!");
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("save","yes");
    }


    /**
     * Function: Save alarm list when app paused.
     */
    @Override
    public void onPause(){
        System.out.println("on pause");
        Bundle bundle = new Bundle();
        bundle.putParcelableArray("alarmObjList", alarmObjList);
        onSaveInstanceState(bundle);
        super.onPause();
    }


    /**
     * Function: Save alarm list in device storage when app stopped.
     */
    @Override
    public void onStop() {
        super.onStop();
        saveData();
    }



    @Override
    public void onStart() {
        super.onStart();
        // turn off any past alarms
        turnOffPastAlarms();
    }

    /**
     * Function: Saves alarm objects in alarm object list in Json data.
     */
    private void saveData() {
        SharedPreferences sharedPref = mainAlarmActivity.getSharedPreferences("alarmSharedPreferences ", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(alarmObjList);
        editor.putString("alarmObjList", json);
        editor.commit();
    }

    /**
     * Function: Load alarm objects from alarm object list using Json data.
     *           If data not there then set up empty array of n fixed sze.
     */
    private void loadData() {
        SharedPreferences sharedPref = mainAlarmActivity.getSharedPreferences("alarmSharedPreferences ", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("alarmObjList", null);
        Type type = new TypeToken<Alarm[]>(){}.getType();
        alarmObjList = gson.fromJson(json, type);

        if(alarmObjList == null) {
            alarmObjList = new Alarm[Alarm.INSTANCE_LIMIT];
        }else {
            initAlarmTable();
        }
    }

    /**
     * Function: Creates a new Create Alarm Activity and goes there. Request Code = 1, and waits for result
     * Stimuli: Launches when create_alarm_button is clicked.
     */
    protected View.OnClickListener createNewAlarm = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.out.println("Clicked alarm creator");
            Intent creatAlarmIntent = new Intent(mainAlarmActivity, CreateAlarmActivity.class);
            creatAlarmIntent.putExtra("alarmAction", "create-alarm");
            gotoCreateAlarmActivity("createNewAlarm", "create-alarm", creatAlarmIntent);
        }
    };


    /**
     * Function: This is the method where all activity change from this current to CreateAlarmainAlarmActivity will take place.
     * Stimuli: Will be called from different parts of this activity when needed.
     * Parameters: methodName: String   // will tell from which method an intent is requested from.
     *             ops: String          // will tell why it wants to switch activity.
     */
    private void gotoCreateAlarmActivity(String methodName, String ops, Intent i) {
        System.out.printf("Intent change requested by: %s \n",methodName);


        switch(ops) {
            case "create-alarm":
                if( Alarm.getInstanceCount() >= Alarm.INSTANCE_LIMIT ) {
                    System.out.println("Max alarms reached");
                    break;
                }

                startActivityForResult(i, 1);
                break;

            case "copy-alarm":
                if( Alarm.getInstanceCount() >= Alarm.INSTANCE_LIMIT ) {
                    System.out.println("Max alarms reached");
                    break;
                }
                startActivityForResult(i, 3);
                break;

            case "edit-alarm":
                startActivityForResult(i, 2);
                break;

            default:
                System.out.printf("Ops '%s' not supported in '%s' method \n",ops, "gotoCreateAlarmaActivity");
        }
    }


    /**
     * Function: Handles activity for result intents.
     *           if(code == 1 && OK) Creates alarm valid.
     *           if(code == 2 && OK) Edits existing alarm valid.
     *           if(code == 3 && OK) Creates alarm valid that was copied.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

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
                updateAlarm(parceledAlarm, editIndex, false);
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


    /**
     * Function: Inserts a new alarm row, adds the alarm to alarm list and calls notify service to initiate notification.
     * Stumili: When a new alarm is created. (Not existing alarm, can be a copied alarm as its considered a new alarm).
     */
    protected void insertAlarm(Alarm obj) {
        TableRow tr = createAlarmRow(obj);
        alarm_table.addView(tr);
        alarm_table.addView(createLowerBorderRow());
        alarmObjList[obj.getAlarmId()] = obj;

        // launch alarm manager to create alarm at this new row's set time by getting switch of row and checking it.
        LinearLayout ll = (LinearLayout) tr.getChildAt(tr.getChildCount()-1);
        Switch switchS = (Switch) ll.getChildAt(tr.getChildCount()-1);
        switchS.callOnClick();
        addPaddedRow();
        System.out.println("Row inserted");
    }


    /**
     * Function: Changes an alarm row with respect to @param timezoneChange.
     *          if timezoneChange is true, then just edit the text of alarm row of given @param index with time from @param obj.
     *          else delete @param index and add a new row with @param obj details at this @param index.
     *          P.s for some reason, when timezoneChage is true and a new alarm is created for the old one, startActForResult has no activity attached.
     *
     * Stimuli: When alarm time is edited or updated.
     *          Called by checkDstAlarms and by onActivityResult when req id = 2.
     */
    protected void updateAlarm(Alarm obj, int index, boolean timezoneChange) {
        alarmObjList[obj.getAlarmId()] =  obj;
        if(timezoneChange) {
            TableRow tr = (TableRow) alarm_table.getChildAt(index);
            TextView tv = (TextView) ((LinearLayout) tr.getChildAt(0)).getChildAt(0);
            tv.setText(obj.getTimeString());

            // launch alarm manager to create alarm at this new row's set time by getting switch of row and checking it.
            LinearLayout ll = (LinearLayout) tr.getChildAt(tr.getChildCount()-1);
            Switch switchS = (Switch) ll.getChildAt(tr.getChildCount()-1);
            switchS.callOnClick();

            System.out.println("Row edited!");
        } else
            updateAlarmRow(createAlarmRow(obj), index);
    }

    /**
     * Function: Removes Table row at @param index and adds a row @param tr. Then starts notify service call.
     * Stimuli: Called by updateAlarm method.
     */
    protected void updateAlarmRow(TableRow tr, int index) {
        alarm_table.removeViewAt(index);
        alarm_table.addView(tr,index);

        // launch alarm manager to create alarm at this new row's set time by getting switch of row and checking it.
        LinearLayout ll = (LinearLayout) tr.getChildAt(tr.getChildCount()-1);
        Switch switchS = (Switch) ll.getChildAt(tr.getChildCount()-1);
        switchS.callOnClick();

        System.out.println("Row edited!");
    }


    /**
     * Function: When switch is turned on, send alarm and alarm list and turn on alarm in notify service.
     *           When switch is turned off, send alarm and turn off alarm in notify service.
     */
    View.OnClickListener switchOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch s = (Switch) view;
            boolean isChecked = s.isChecked();
            LinearLayout tempL = (LinearLayout) s.getParent();
            TableRow tempRow = (TableRow) tempL.getParent();
            Alarm alarmObj = alarmObjList[(int)tempRow.getTag()];

            alarmObj.setAlarmSet(isChecked);
            if (isChecked) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("alarmObj", alarmObj);
                bundle.putParcelableArray("alarm-array", alarmObjList);
                long intervalTimeInMillis = notifyServiceManager.setAlarm(mainAlarmActivity, alarmObj.getAlarmId(), bundle, NotifyService.APP_TIME_UPDATE);
                if(intervalTimeInMillis < 0)
                    s.setChecked(false);
                toastAlarmInterval(intervalTimeInMillis);
            } else {
                notifyServiceManager.deleteAllAlarmFor(mainAlarmActivity, alarmObj.getAlarmId());
                toastMessage("Notify cancelled", Toast.LENGTH_SHORT);
            }
        }
    };

    private void toastAlarmInterval(long interval) {
        String toastStr;
        if(interval == -1) {
            toastStr = "Please select a valid date.";
        }else {
            toastStr  = "Will notify at " +new java.text.SimpleDateFormat("h:mm a E,\n\t\t\t\t\t\tdd-MM-yyyy").format(interval);
        }

        toastMessage(toastStr, Toast.LENGTH_LONG);
    }

    public void toastMessage(String str, int duration) {
        Context context = mainAlarmActivity;
        CharSequence text = str;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * Function: If isDeleteSet == true, then the clicked row gets checked for delete,
     *           Else, goes to create alarm activity to be edited.
     * Stimuli: When an alarm row is clicked.
     */

    View.OnClickListener alarmRowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(isDeleteSet) {
                setCheck((TableRow) view);
            }else {
                Intent editAlarmIntent = new Intent(mainAlarmActivity, CreateAlarmActivity.class);
                editAlarmIntent.putExtra("alarmAction","edit-alarm");
                editAlarmIntent.putExtra("editIndex", alarm_table.indexOfChild(view));

                // get alarm and put it in bundle, and put bundle in intent
                Bundle bundleObj = new Bundle();
                bundleObj.putParcelable("editing-alarm", alarmObjList[(int)((TableRow) view).getTag()]);
                editAlarmIntent.putExtras(bundleObj);

                // go to new activity
                gotoCreateAlarmActivity("alarmRowClickListener", "edit-alarm", editAlarmIntent);
            }
        }
    };


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
     * Function: Show dialog box which allows copy of alarm with diff time and delete selected alarm.
     */
    View.OnLongClickListener alarmRowLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            dialogViewIndex = alarm_table.indexOfChild(view);
            View title = View.inflate(mainAlarmActivity, R.layout.dialog_header_options_copy_alarm,null);
            AlertDialog.Builder builder = new AlertDialog.Builder(mainAlarmActivity);
            builder.setCustomTitle(title)
                    .setItems(R.array.planets_array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int itemIndex) {
                            if(itemIndex == 0) {
                                // copy alarm with different time
                                System.out.println("copy dialog");

                                // create alarm object bundle for copy alarm
                                Bundle bundleObj = new Bundle();
                                int alarmId = (int) alarm_table.getChildAt(dialogViewIndex).getTag();
                                bundleObj.putParcelable("copying-alarm", alarmObjList[alarmId]);

                                // create intent and put bundle in
                                Intent copyAlarmIntent = new Intent(mainAlarmActivity, CreateAlarmActivity.class);
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

    /**
     * Function: Responsible for deleting only the alarm, whose index was passed through.
     * Stimuli: Called when delete options manu is clicked,
     *          Called when delete options is clicked from long click dialog.
     */
    protected void removeAlarm(int alarmRowIndex) {
        System.out.println("deleting index: " + alarmRowIndex);
        TableRow tr = (TableRow) alarm_table.getChildAt(alarmRowIndex);
        notifyServiceManager.deleteAllAlarmFor(mainAlarmActivity,(int) tr.getTag());
        alarm_table.removeViewAt(alarmRowIndex+1);
        alarm_table.removeViewAt(alarmRowIndex);
        Alarm a = alarmObjList[(int) tr.getTag()];
        alarmObjList[(int) tr.getTag()] = null;
        Alarm.deconstruct(a);

    }

    /**
     * Function: Responsible for deleting all the alarm rows that has a checked delete box.
     * Stimuli: Called when delete options manu is clicked.
     */
    private void removeSelectedAlarms() {
        for(int i = alarm_table.getChildCount(); i >= 0; i--) {
            if (alarm_table.getChildAt(i)!= null && Strategy.isInteger( alarm_table.getChildAt(i).getTag().toString() ) ) {
                TableRow tr = (TableRow) alarm_table.getChildAt(i);
                if (tr.getChildAt(0) instanceof CheckBox) {
                    CheckBox cb = (CheckBox) tr.getChildAt(0);

                    // if checked, then delete row
                    if (cb.isChecked()) {
                        removeAlarm(i);
                    }
                }
            }
        }
    }



    /**
     * Function: When the Delete option is clicked on options menu, it does the following:
     *              remove delete options menu,
     *              removes all the selected alarms,
     *              removes all the delete boxes,
     *              adds on/off switches,
     *              show FAB create_alarm_btn,
     * Stimuli: Called by startup_activity, after delete option is selected from actionbar
     */
    public void performMenuDeleteAction() {
        // remove delete options menu
        isDeleteSet = false;

        alarm_table = mainAlarmActivity.findViewById(R.id.alarm_table);
        create_alarm_btn = (FloatingActionButton)  mainAlarmActivity.findViewById(R.id.create_alarm_btn);


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
     * Function: A helper method that takes UI back to normal view without deleting any alarms.
     * Stimuli: When users presses delete all options and goes somewhere else without deleting any alarm.
     */
    public void restoreMainStartupView() {
        // remove delete options menu
        isDeleteSet = false;

        alarm_table = mainAlarmActivity.findViewById(R.id.alarm_table);
        create_alarm_btn = (FloatingActionButton)  mainAlarmActivity.findViewById(R.id.create_alarm_btn);


        // removes all the delete boxes
        removeDeleteBoxes();

        // adds on/off switches
        enableOnOffSwitches();

        // show FAB create_alarm_btn
        create_alarm_btn.setVisibility(View.VISIBLE);

        alarmObj = null;
    }


    /**
     * Function: Deletes "delete checkboxes" from all the alarm rows.
     * Stimuli: Launches when delete button is pressed.
     */
    protected void removeDeleteBoxes() {
        for(int i = 0; i < alarm_table.getChildCount(); i++) {
            if (Strategy.isInteger( alarm_table.getChildAt(i).getTag().toString() ) ) {
                TableRow tr = (TableRow) alarm_table.getChildAt(i);

                if (tr.getChildAt(0) instanceof CheckBox) {
                    tr.removeViewAt(0);
                }
            }
        }
    }


    /**
     * Function: Disables on/off switch for each alarm when delete button is present.
     * Stimuli: Should always be called before addDeleteBoxes()
     *          If called after, then TableRow will have 3 children instead of 2.
     */
    protected void  disableOnOffSwitch() {
        for(int i = 0; i < alarm_table.getChildCount(); i++) {
            if (Strategy.isInteger( alarm_table.getChildAt(i).getTag().toString() ) ) {
                TableRow tr = (TableRow) alarm_table.getChildAt(i);

                if (tr.getChildAt(1) instanceof LinearLayout) {
                    LinearLayout ll = (LinearLayout) tr.getChildAt(1);
                    if (ll.getChildAt(1) instanceof Switch) {
                        Switch s = (Switch) ll.getChildAt(ll.getChildCount() - 1);
                        System.out.println(s.getText());
                        s.setVisibility(View.INVISIBLE);
                    }
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
            if (Strategy.isInteger( alarm_table.getChildAt(i).getTag().toString() ) ) {
                TableRow tr = (TableRow) alarm_table.getChildAt(i);

                if (tr.getChildAt(1) instanceof LinearLayout) {
                    LinearLayout ll = (LinearLayout) tr.getChildAt(1);
                    if (ll.getChildAt(1) instanceof Switch) {
                        Switch s = (Switch) ll.getChildAt(ll.getChildCount() - 1);
                        System.out.println(s.getText());
                        s.setVisibility(View.VISIBLE);
                    }
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
            if (Strategy.isInteger(alarm_table.getChildAt(i).getTag().toString())) {
                TableRow tr = (TableRow) alarm_table.getChildAt(i);

                // create new checkbox
                CheckBox cb = new CheckBox(mainAlarmActivity);
                tr.addView(cb, 0);
            }
        }
    }


    /**
     * Function: When an delete option is pressed, this method does the following, if alarms are present:
     *              removes on/off alarm switches,
     *              adds delete boxes,
     *              hide FAB create_alarm_btn,
     *              checks the delete box of clicked row,
     * Stimuli: Called by startup_activity, when delete alarms option is selected from actionbar.
     */
    public void setAlarmDeleteView() {
        isDeleteSet = true;
        alarm_table = (TableLayout) mainAlarmActivity.findViewById(R.id.alarm_table);
        create_alarm_btn = (FloatingActionButton)  mainAlarmActivity.findViewById(R.id.create_alarm_btn);
        System.out.println("Null OblList " +  alarm_table.getChildCount());

        if(alarmObjList.length == 0 || alarm_table == null)
            return;

        // removes on/off alarm switches
        disableOnOffSwitch();

        // hide FAB create_alarm_btn
        create_alarm_btn.setVisibility(View.INVISIBLE);

        // adds delete boxes
        addDeleteBoxes();

    }


    /**
     * Function: Creates a hash map which has <string> location for keys and arraylist of <Alarm> of same location.
     *           The hashmap gets stored in a static hashmap var.
     *           Needed by Location fragment to display location.
     * Stimuli: Called everytime users goes to Location fragment from this fragment.
     */
    private void createLocationGroup() {
        locationMap = new HashMap<String, ArrayList>();
        for(Alarm a : alarmObjList) {
            if( a != null ) {
                String location = "Empty Locations";
                if(a.getLocation().length() > 0)
                    location = a.getLocation().substring(0, 1).toUpperCase() + a.getLocation().substring(1);

                System.out.println("Loca: " + location);
                if(!location.isEmpty() && !locationMap.containsKey(location)) {
                    locationMap.put(location,new ArrayList<Alarm>());
                }

                ArrayList<Alarm> arr = locationMap.get(location);
                if(arr != null && !arr.contains(a))
                    arr.add(a);
            }
        }
    }

    // helper method that shows contents in hashmap
    private void getAllLocation() {
        System.out.println("In get loca");

        for(String s : locationMap.keySet()) {
            System.out.println(s);
            ArrayList<Alarm> arr = locationMap.get(s);
            for(Alarm a : arr) {
                System.out.println(a.getTimeString());
            }
        }
    }


    /**
     * Function: Calls createLocationGroup to provides a hashmap of locations to requester, normally an activity.
     * Stimuli: Called everytime users goes to Location fragment from this fragment.
     */
    public HashMap<String, ArrayList> getLocationHashMap() {
        createLocationGroup();
        return locationMap;
    }


    /**
     * Function: Checks all the listed alarms. If DST setting on, then:
     *           If alarm time is on DST and current time is standard, then convert alarm time to standard.
     *           If alarm time is not on DST and current time is on DST, then convert alarm time to DST
     * Stimuli: Called when an intent with 'Intent.ACTION_TIMEZONE_CHANGED' is received from NotifyService class.
     */
    public void checkDstAlarms() {
        alarm_table = mainAlarmActivity.findViewById(R.id.alarm_table);

        for(Alarm a : alarmObjList) {
            if( a != null )
                a.resolveDstAlarmTime();
        }

        // need to update rows as well.
        for( int i =0; i < alarm_table.getChildCount(); i++ ) {
            if (Strategy.isInteger( alarm_table.getChildAt(i).getTag().toString() ) ) {
                Alarm a = alarmObjList[(int) alarm_table.getChildAt(i).getTag()];
                if (a.isChangeWithDayLightSavings()) {
                    updateAlarm(a, i, true);
                }
            }
        }
    }





    /**
     * Function: Creates a new alarm row with all its necessary contents.
     * Stimuli: Is called many times whenever a row needs to be created.
     * Return: Table Row object with necessary contents.
     * Layout: VL: TV/TV,  HL: TV/Switch
     */
    // this function should be responsible for only creating basic table row with all its child elements.
    protected TableRow createAlarmRow(Alarm alarmObj) {

        System.out.println("Main act is: " + mainAlarmActivity);
        TableRow tr = new TableRow(mainAlarmActivity);
        tr.setTag(alarmObj.getAlarmId());
        tr.setOnClickListener(alarmRowClickListener);
        tr.setOnLongClickListener(alarmRowLongClickListener);

/* first layout in the row */

        // linear vertical layout
        LinearLayout linear_vertical_layout = new LinearLayout(mainAlarmActivity);
        linear_vertical_layout.setOrientation(LinearLayout.VERTICAL);

        // TextView to display time
        TextView tv_time = new TextView(mainAlarmActivity);
        linear_vertical_layout.addView(tv_time,0);

        // TextView to display title
        TextView tv_title = new TextView(mainAlarmActivity);
        linear_vertical_layout.addView(tv_title,1);

        tr.addView(linear_vertical_layout,0);

/* second layout in the row */

        // linear horizontal layout
        LinearLayout linear_horizontal_layout = new LinearLayout(mainAlarmActivity);
        linear_horizontal_layout.setOrientation(LinearLayout.HORIZONTAL);

        // TextView to display days
        TextView tv_day = new TextView(mainAlarmActivity);
        linear_horizontal_layout.addView(tv_day,0);

        // switch to display on/off
        Switch row_switch = new Switch(mainAlarmActivity);
        row_switch.setOnClickListener(switchOnClickListener);
        linear_horizontal_layout.addView(row_switch,1);


        tr.addView(linear_horizontal_layout,1);


        // add text, time in row
        tr = addAlarmRowContent(tr, alarmObj);
        tr = addAlarmRowDesign(tr);

        // add table row to alarm table
        return tr;
    }

    // only add text and shit
    private TableRow addAlarmRowContent(TableRow tr, Alarm alarmObj) {

/* first layout in the row */

        int childAt = 0;

        // add time
        TextView tv_time = (TextView) ((LinearLayout)tr.getChildAt(childAt)).getChildAt(0);
        tv_time.setText(alarmObj.getTimeString());

        // add title
        TextView tv_title = (TextView) ((LinearLayout)tr.getChildAt(childAt)).getChildAt(tr.getChildCount()-1);
        tv_title.setText(alarmObj.getTitle());

/* second layout in the row */

        childAt = 1;

        // add rep days
        TextView tv_days = (TextView) ((LinearLayout)tr.getChildAt(childAt)).getChildAt(0);
        String tvDayStr;
        if(alarmObj.isRepeat()){
            tvDayStr = Strategy.repDays(alarmObj.getRepeatingAlarmDays());
            tv_days.setText(Html.fromHtml(tvDayStr));
        } else{
            Calendar ca = Calendar.getInstance();
            ca.setTimeInMillis(alarmObj.getTimeMillis());
            tvDayStr = new SimpleDateFormat("E, dd/MM/yyyy").format(ca.getTime());
            tv_days.setText(tvDayStr);
        }


        // set alarm on (switch is in last layout of row and element of that layout)
        Switch s = (Switch)((LinearLayout)tr.getChildAt(childAt)).getChildAt(tr.getChildCount()-1);
        s.setChecked(alarmObj.isAlarmSet());


        return tr;
    }

    // this will only add layout params and design
    @TargetApi(23)
    private TableRow addAlarmRowDesign(TableRow tr) {

        // table row params
        TableRow.LayoutParams trParam= new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT,6.0f);
        tr.setLayoutParams(trParam);
        // set row bg color
        tr.setBackgroundColor(getResources().getColor(R.color.mainRowBackground));
        tr.setPadding(5,2,0,2);

/* first layout in the row */

        int childAt = 0;

        // vertical layout params
        LinearLayout linear_vertical_layout = (LinearLayout) tr.getChildAt(childAt);
        TableRow.LayoutParams lvParam = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.alarm_row_height),2.0f);
        linear_vertical_layout.setLayoutParams(lvParam);

        // time text view params
        TextView tv_time = (TextView) ((LinearLayout)tr.getChildAt(childAt)).getChildAt(0);
        LinearLayout.LayoutParams tvTimeParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,2.0f);
        tv_time.setLayoutParams(tvTimeParam);
        tv_time.setTextAppearance(R.style.TextAppearance_AppCompat);

        // title text view params
        TextView tv_title = (TextView) ((LinearLayout)tr.getChildAt(childAt)).getChildAt(tr.getChildCount()-1);
        LinearLayout.LayoutParams tvTitleParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,2.0f);
        tv_title.setLayoutParams(tvTitleParam );

/* second layout in the row */

        childAt = 1;

        // horizontal layout params
        LinearLayout linear_horizontal_layout = (LinearLayout) tr.getChildAt(childAt);
        TableRow.LayoutParams hlParam = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.alarm_row_height), 4.0f);
        linear_horizontal_layout.setLayoutParams(hlParam);

        // rep days text view params
        TextView tv_days = (TextView) ((LinearLayout)tr.getChildAt(childAt)).getChildAt(0);
        LinearLayout.LayoutParams tvDayParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT,3.0f);
        tv_days.setLayoutParams(tvDayParam );
        tv_days.setGravity(Gravity.TOP | Gravity.RIGHT);

        // switch params


        return tr;
    }

    /**
     * Function: Creates a separate table row that has a horizontal layout of a small height, which acts as lower border for each row.
     *           One of these rows should be added whenever a new alarm row is created.
     *           All these Rows will have tag = "border-row"
     * Return: A table row with tag = "border-row"
     */
    protected TableRow createLowerBorderRow() {
        LinearLayout hr_layout = new LinearLayout(mainAlarmActivity);
        hr_layout .setOrientation(LinearLayout.HORIZONTAL);
        TableRow.LayoutParams lrParam = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,  (int) getResources().getDimension(R.dimen.table_row_lower_border),6.0f);
        hr_layout.setLayoutParams(lrParam);
        hr_layout.setBackgroundColor(getResources().getColor(R.color.darker_grey));

        TableRow tr = new TableRow(mainAlarmActivity);
        tr.setTag("border-row");
        tr.addView(hr_layout);
        return tr;
    }

    /**
     * Function: Sole purpose is to give extra space at end of table
     * Stimuli: Calleed every time a new row is added.
     */
    private void addPaddedRow() {
        TableRow tr = null;

        for(int i = 0; i < alarm_table.getChildCount(); i++) {
            // existing padded row found
            if(alarm_table.getChildAt(i).getTag().toString().equals("padded-row")) {
                tr = (TableRow) alarm_table.getChildAt(i);
                alarm_table.removeViewAt(i);
            }
        }

        // padded row not found, so create and add
        if(tr == null) {
            tr = new TableRow(mainAlarmActivity);
            TableRow.LayoutParams trParam= new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT,6.0f);
            tr.setLayoutParams(trParam);
            tr.setTag("padded-row");

            LinearLayout ll = new LinearLayout(mainAlarmActivity);
            TableRow.LayoutParams llParam = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.alarm_row_padded));
            ll.setLayoutParams(llParam);

            tr.addView(ll);
        }

        alarm_table.addView(tr);
    }


    /**
     * Function: Checks if any alarm with specific date is in the past compared to the current system time.
     *           Then turn it off, if its not off already.
     *           This however doesn't turn on any off alarms, if system time is set to previous dates.
     *
     * Stimuli: Called every time when fragment comes to life.
     */
    private void turnOffPastAlarms() {
        for(int i = 0; i < alarm_table.getChildCount(); i++) {
            if (Strategy.isInteger( alarm_table.getChildAt(i).getTag().toString() ) ) {
                int index = Integer.parseInt(alarm_table.getChildAt(i).getTag().toString());
                Alarm a = alarmObjList[index];
                // if not on repeat then check if specific date is less than current sys time.
                if(!a.isRepeat() && (a.getTimeMillis() - System.currentTimeMillis()) < 0) {
                    a.setAlarmSet(false);

                    TableRow tr = (TableRow) alarm_table.getChildAt(i);
                    if (tr.getChildAt(1) instanceof LinearLayout) {
                        LinearLayout ll = (LinearLayout) tr.getChildAt(1);
                        if(ll.getChildAt(1) instanceof Switch) {
                            Switch s = (Switch) ll.getChildAt(ll.getChildCount() - 1);
                            s.setChecked(false);
                        }
                    }
                }
            }
        }
    }


/** ------ Tab fragment stuff -------- **/

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MainAlarmFragment () {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainAlarmFragment.
     */
    public static MainAlarmFragment newInstance(String param1, String param2) {
        MainAlarmFragment fragment = new MainAlarmFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Activity)
            mainAlarmActivity = (Activity) context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            System.out.println("In attached");
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}

