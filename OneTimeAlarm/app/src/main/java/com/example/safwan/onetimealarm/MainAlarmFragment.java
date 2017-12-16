package com.example.safwan.onetimealarm;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
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
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by safwan on 14/12/2017.
 */

public class MainAlarmFragment extends Fragment {

/** Final Variables**/

    final static int ALARM_ID_START = 2000;
    final static int CHECKBOX_ID_START = 1000;


/** Static Variables**/

    private static boolean isDeleteSet = false;
    protected static int totalAlarmRows = 0;
    protected static int dialogViewIndex;
    protected static ArrayList<Alarm> alarmObjList;// = new ArrayList<Alarm>();
    private static View thisView;
    private static Activity mainAlarmActivity;

/** Plain Old Variables**/

    HashMap<String, ArrayList> locationMap;
    FloatingActionButton create_alarm_btn;
    TextView alarm_time_1;
    RelativeLayout startup_relative_layout;
    Button chgbtn;
    TableLayout alarm_table;
    int cbId = CHECKBOX_ID_START;

    private Menu currentMenu;
    Alarm alarmObj;


    /** Intent Request code usage **/

    // code = 1, usage = create new alarm
    // code = 2, usage = edit existing alarm
    // code = 3, usage = copy an alarm with different time

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thisView = (View) inflater.inflate(R.layout.fragment_main_alarm, container, false);
        alarm_table = (TableLayout) thisView.findViewById(R.id.alarm_table);
        System.out.println("Null  Alarm Table " + alarm_table);

        System.out.println("View is: "+ mainAlarmActivity);
        loadData();
        // check for saved alarm objects
        if(savedInstanceState != null && savedInstanceState.containsKey("alarmObjList")) {
            System.out.println("saved alarms found!");
            alarmObjList = savedInstanceState.getParcelableArrayList("alarmObjList");
            initAlarmTable();
            System.out.println("View is: "+ mainAlarmActivity);
        }else if(savedInstanceState != null && savedInstanceState.containsKey("save")) {
            System.out.println("No obj list found!");
        }else {
            System.out.println("No save found!");
        }

        create_alarm_btn = (FloatingActionButton) thisView.findViewById(R.id.create_alarm_btn);
        create_alarm_btn .setOnClickListener(createNewAlarm);

        // demo button
        chgbtn = (Button) mainAlarmActivity.findViewById(R.id.chngbtn);
//        chgbtn.setOnClickListener(callMe);

        create_alarm_btn .setOnClickListener(createNewAlarm);

//        callMe();

        return thisView;
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
    public void onStop() {
        super.onStop();
        saveData();
//        System.out.println("on stop");
    }


    private void saveData() {
        SharedPreferences sharedPref = mainAlarmActivity.getSharedPreferences("alarmSharedPreferences ", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(alarmObjList);
        editor.putString("alarmObjList", json);
        editor.commit();
    }

    private void loadData() {
        SharedPreferences sharedPref = mainAlarmActivity.getSharedPreferences("alarmSharedPreferences ", Context.MODE_PRIVATE);
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
                startActivityForResult(i, 1);
                break;

            case "edit-alarm":
                startActivityForResult(i, 2);
                break;

            case "copy-alarm":
                startActivityForResult(i, 3);
                break;

            default:
                System.out.printf("Ops '%s' not supported in '%s' method \n",ops, "gotoCreateAlarmaActivity");
        }
    }


    /**
     * Function: Handles activity for result intents.
     *           if(code == 1 && OK) Creates alarm valid.
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
     * Function: Adds a new alarm row with all its neccesary contents.
     * Stimuli: Launches when a new alarm is created. Most probably called from onActivityResult
     */

    protected TableRow createAlarmRow(Alarm alarmObj) {

        // linear vertical layout
        LinearLayout linear_vertical_layout = new LinearLayout(mainAlarmActivity);
        linear_vertical_layout.setOrientation(LinearLayout.VERTICAL);


        // TextView to display time
        TextView tv_time = new TextView(mainAlarmActivity);
        tv_time.setLayoutParams(new ViewGroup.LayoutParams(
                140,
                40));

        tv_time.setText(alarmObj.getTimeString());
        linear_vertical_layout.addView(tv_time,0);

        // TextView to display title
        TextView tv_title = new TextView(mainAlarmActivity);
        tv_title.setText(alarmObj.getTitle());
        linear_vertical_layout.addView(tv_title,1);

        // linear horizontal layout
        LinearLayout linear_horizontal_layout = new LinearLayout(mainAlarmActivity);
//        linear_horizontal_layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linear_horizontal_layout.setOrientation(LinearLayout.HORIZONTAL);

        // TextView to display days
        TextView tv_day = new TextView(mainAlarmActivity);
        tv_day.setLayoutParams(new ViewGroup.LayoutParams(
                200,
                ViewGroup.LayoutParams.MATCH_PARENT));
        tv_day.setText("S M T");
        linear_horizontal_layout.addView(tv_day,0);

        // switch to display on/off
        Switch row_switch = new Switch(mainAlarmActivity);

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
        TableRow tr = new TableRow(mainAlarmActivity);

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
//            System.out.println("D: "+isDeleteSet);

            if(isDeleteSet) {
                setCheck((TableRow) view);
            }else {
                // go to new activity
                Intent editAlarmIntent = new Intent(mainAlarmActivity, CreateAlarmActivity.class);
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


    View.OnLongClickListener alarmRowLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
            dialogViewIndex = alarm_table.indexOfChild(view);
//            AlertDialog.Builder builder = new AlertDialog.Builder(mainAlarmActivity, R.style.dialog_light);
            AlertDialog.Builder builder = new AlertDialog.Builder(mainAlarmActivity);
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

    protected void removeAlarm(int alarmIndex) {
        System.out.println("deleting index: " + alarmIndex);
        alarm_table.removeViewAt(alarmIndex);
        alarmObjList.remove(alarmIndex);
//        for(Alarm a : alarmObjList) {
//            System.out.println(a.getHr());
//        }
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
     * Function: When the Delete option is clicked on options menu, it does the following:
     *              remove delete options menu,
     *              removes all the selected alarms,
     *              removes all the delete boxes,
     *              adds on/off switches,
     *              show FAB create_alarm_btn,
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
     * Function: Sets delete option on action bar if input == true, else removes delete option.
     * Stimuli: Launches when an alarm row is long pressed.
     */

//    public void setDeleteMenu(boolean val) {
//        isDeleteSet = val;  // set static variable, used by onPrepareOptionsMenu
//        // IMPLEMENT THIS
//        onPrepareOptionsMenu(currentMenu);
//    }


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
            CheckBox cb = new CheckBox(mainAlarmActivity);
            cb.setId(cbId++);
            tr.addView(cb,0);
        }
    }

    /**
     * Function: When an delete option is pressed, this method does the following, if alarms are present:
     *              removes on/off alarm switches,
     *              adds delete boxes,
     *              checks the delete box of clicked row,
     *              change option menu to give delete option,
     *              hide FAB create_alarm_btn,
     * Stimuli: When an alarm row is long pressed.
     */

    public void setAlarmDeleteView(Boolean val) {
        isDeleteSet = true;
        alarm_table = (TableLayout) mainAlarmActivity.findViewById(R.id.alarm_table);
        create_alarm_btn = (FloatingActionButton)  mainAlarmActivity.findViewById(R.id.create_alarm_btn);
        System.out.println("Null OblList " +  alarm_table.getChildCount());

        if(alarmObjList.size() == 0 || alarm_table == null)
            return;


        // removes on/off alarm switches
        disableOnOffSwitch();

        // hide FAB create_alarm_btn
        create_alarm_btn.setVisibility(View.INVISIBLE);

        // adds delete boxes
        addDeleteBoxes();

    }

    private void createLocationGroup() {
        locationMap = new HashMap<String, ArrayList>();
        for(Alarm a : alarmObjList) {
            String location = a.getLocation();
            System.out.println("Loca: " + location);
            if(!location.isEmpty() && !locationMap.containsKey(location)) {
                locationMap.put(location,new ArrayList<Alarm>());
            }

            ArrayList<Alarm> arr = locationMap.get(location);
            if(arr != null && !arr.contains(a))
                arr.add(a);
        }
    }

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


    public HashMap<String, ArrayList> getLocationHashMap() {
        createLocationGroup();
        return locationMap;
    }














/** ------ Tab fragment stuff -------- **/

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
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
    // TODO: Rename and change types and number of parameters
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

    // TODO: Rename method, update argument and hook method into UI event
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
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        context = context;
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
