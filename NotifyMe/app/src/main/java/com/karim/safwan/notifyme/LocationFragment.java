package com.karim.safwan.notifyme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.InputFilter;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class LocationFragment extends Fragment {

/** Final Variables**/


/** Static Variables**/
    private static Activity locationActivity;
    static HashMap<String, ArrayList> locationMap;

/** Plain Old Variables**/
    HashMap<String, Boolean > isExpandedMap = new HashMap<String, Boolean >();
    TableLayout location_table;
    View view;
    // demo purposes
    boolean demoCollapse;
    ImageView img1;
    TableRow child_row, header_row;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_location, container, false);

        // Inflate the layout for this fragment

        // existing demo rows
//        child_row = (TableRow) view.findViewById(R.id.child_row);
//        child_row.setVisibility(View.GONE);
//        demoCollapse = false;
//        img1 = (ImageView) view.findViewById(R.id.img1);
//        img1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(demoCollapse) {
//                    demoCollapse = false;
//                    child_row.setVisibility(View.GONE);
//                }else {
//                    demoCollapse = true;
//                    child_row.setVisibility(View.VISIBLE);
//                }
//            }
//        });

        return view;
    }


    /**
     * Function: Sets up the view of location fragment by creating and adding the table row with necessary details.
     * Stimuli: Called by startup_activity.
     * @param map: Needs to be passed a hashmap that contains <String> as locations and arraylist of <Alarm> as values.
     */
    public void setLocationHashMap(HashMap<String, ArrayList> map) {
        locationMap = map;
        location_table = locationActivity .findViewById(R.id.location_table);
        // getAllLocation();
        location_table.removeAllViews();
        setUpLocationRows();
    }


    // helper to show map content
    private void getAllLocation() {
        System.out.println("In get loaction frag loca");


        for(String s : locationMap.keySet()) {
            System.out.println(s);
            ArrayList<Alarm> arr = locationMap.get(s);
            for(Alarm a : arr) {
                System.out.println(a.getTimeString());
            }
        }
    }

    private void setUpLocationRows() {
        for(String s : locationMap.keySet()) {
            if(!s.equals("Empty Locations"))
                createLocationRows(s);
        }
        if(locationMap.containsKey("Empty Locations"))
            createLocationRows("Empty Locations");
    }


    /**
     * Function: This is a big method responsible for creating both the header and child rows. Child rows are created by another method, but are called from this one. Maybe we can change that?
     * Stimuli: Called by setLocationHashMap.
     * Layout of header rows: TV/TV/ImgV
     */
    private void createLocationRows(String s) {
            // create header rows
            TableRow tr = new TableRow(locationActivity);
            tr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rowClickListener((TableRow) view);
                }
            });

            // create header row elements
            final TextView locationView = new TextView(locationActivity);
            TextView sizeView = new TextView(locationActivity);
            final ImageView imgView = new ImageView(locationActivity);

            // set contents of header elements
            locationView.setText(s);
            sizeView.setText(Integer.toString(locationMap.get(s).size()));
            imgView.setImageResource(R.drawable.down_arrow);


            // set click listener on header arrow down
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TableRow trParent = (TableRow) imgView.getParent();
                    rowClickListener(trParent);
                }
            });

            tr.addView(locationView);
            tr.addView(sizeView);
            tr.addView(imgView);
            tr = addHeaderRowDesign(tr);

            location_table.addView(tr);
            System.out.println("Added header row inside fragment table " + s);
            // set isExpanded of this row to false


            ArrayList<Alarm> arr = locationMap.get(s);
            for(Alarm a : arr) {
                TableRow trChild = createChildRow(a);
                trChild = addChildRowDesign(trChild);
                location_table.addView(trChild);
                // hide this child row
                location_table.getChildAt(location_table.getChildCount()-1).setVisibility(View.GONE);
                System.out.println("Child count: " + location_table.getChildCount());
            }
            isExpandedMap.put(s,false);
    }


    protected void rowClickListener(TableRow trParent) {
        System.out.println("hit me harder!");
        TextView strTv = (TextView) trParent.getChildAt(0);
        String strLocation = strTv.getText().toString();
        int len = locationMap.get(strLocation).size();
        int index = location_table.indexOfChild(trParent);


        if (isExpandedMap.get(strLocation)) {
            for (int i = index + 1; i <= index + len; i++) {
                location_table.getChildAt(i).setVisibility(View.GONE);
            }
            isExpandedMap.put(strLocation, false);
        } else {
            for (int i = index + 1; i <= index + len; i++) {
                location_table.getChildAt(i).setVisibility(View.VISIBLE);
            }
            isExpandedMap.put(strLocation, true);
        }
    }


    /**
     * Function: Creates child row which is hidden to start off with.
     * Stimuli: Called by createLocationRows for each alarm for a location key.
     * Return: Child Table Row.
     * Layout: VL:(HL:(VL: TV/TV) / HL: TV) / VL: TV
     */
    private TableRow createChildRow(Alarm alarmObj) {
        // create child elements
        LinearLayout mainVerticalLayout = new LinearLayout(locationActivity);
        mainVerticalLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout firstHorizontalLayout = new LinearLayout(locationActivity);
        firstHorizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout minorVerticalLayout = new LinearLayout(locationActivity);
        minorVerticalLayout.setOrientation(LinearLayout.VERTICAL);

        TextView timeView = new TextView(locationActivity);
        TextView titleView = new TextView(locationActivity);
        TextView repeatingDaysView = new TextView(locationActivity);

        LinearLayout secondHorizontalLayout = new LinearLayout(locationActivity);
        secondHorizontalLayout .setOrientation(LinearLayout.HORIZONTAL);
        TextView descriptionView = new TextView(locationActivity);

        LinearLayout thirdHorizontalLayout = new LinearLayout(locationActivity);


        // set contents in view elements
        timeView.setText(alarmObj.getTimeString());
        System.out.println(alarmObj.getTimeString());

        titleView.setText(alarmObj.getTitle());
        System.out.println(alarmObj.getTitle());
        descriptionView.setText(alarmObj.getDescription());
        System.out.println(alarmObj.getDescription());
        // add rep days
        String tvDayStr;
        if(alarmObj.isRepeat()){
            tvDayStr = Strategy.repDays(alarmObj.getRepeatingAlarmDays());
            repeatingDaysView.setText(Html.fromHtml(tvDayStr));
        }
        else{
            Calendar ca = Calendar.getInstance();
            ca.setTimeInMillis(alarmObj.getTimeMillis());
            tvDayStr = new SimpleDateFormat("E, dd/MM/yyyy").format(ca.getTime());
            repeatingDaysView.setText(tvDayStr);
        }


        // add elements to parents in order
        mainVerticalLayout.addView(firstHorizontalLayout,0);
        mainVerticalLayout.addView(secondHorizontalLayout,1);
        mainVerticalLayout.addView(thirdHorizontalLayout,2);

        firstHorizontalLayout.addView(minorVerticalLayout,0);
        minorVerticalLayout.addView(timeView,0);
        minorVerticalLayout.addView(titleView,1);
        firstHorizontalLayout.addView(repeatingDaysView,1);

        secondHorizontalLayout.addView(descriptionView,0);

        // create new table row and add main vertical
        TableRow tr = new TableRow(locationActivity);
        tr.addView(mainVerticalLayout);
        System.out.println("returning minor rows");
        return tr;
    }


    // header row params, colors, desgin
    @TargetApi(23)
    private TableRow addHeaderRowDesign(TableRow tr) {

        // table row
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        rowParams.setMargins(0,0,0,2);
        tr.setLayoutParams(rowParams);
        tr.setBackgroundColor(locationActivity.getResources().getColor(R.color.mainRowBackground));
        tr.setPadding(5,0,0,0);

        // location text view
        TextView locationView = (TextView) tr.getChildAt(0);
        TableRow.LayoutParams locationParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, (int) locationActivity.getResources().getDimension(R.dimen.location_header_height),6.0f);
        locationView.setLayoutParams(locationParams);
        locationView.setGravity(Gravity.CENTER| Gravity.LEFT);
        locationView.setTextAppearance(R.style.TextAppearance_AppCompat);

        // number text view
        TextView sizeView = (TextView) tr.getChildAt(1);
        TableRow.LayoutParams numberParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        numberParam.gravity = Gravity.CENTER;
        sizeView.setLayoutParams(numberParam);
        sizeView.setTextAppearance(R.style.TextAppearance_AppCompat);

        // img view
        ImageView imgView = (ImageView) tr.getChildAt(2);
        TableRow.LayoutParams imgParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, (int) locationActivity.getResources().getDimension(R.dimen.height_down_arrow_location));
        imgParams.gravity = Gravity.CENTER;
        imgView.setLayoutParams(imgParams);


        return tr;
    }

    private TableRow addChildRowDesign(TableRow tr) {
        // child table row
        tr.setBackgroundColor(locationActivity.getResources().getColor(R.color.child_location_rows));
        tr.setPadding(5,0,5,0);

        // main vertical layout
        LinearLayout mainVL = (LinearLayout) tr.getChildAt(0);
        TableRow.LayoutParams lvParams = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) locationActivity.getResources().getDimension(R.dimen.location_height_child_row),6.0f);
        lvParams.setMargins(0,0,0,4);
        mainVL.setLayoutParams(lvParams);


/* first horizontal layout */
        LinearLayout firstHL = (LinearLayout) mainVL.getChildAt(0);
        LinearLayout.LayoutParams firstHLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 4.0f);
        firstHL.setLayoutParams(firstHLParams);
        firstHL.setMinimumHeight((int) locationActivity.getResources().getDimension(R.dimen.location_child_first_hl_min_height));

        // minor vertical layout
        LinearLayout minorVL = (LinearLayout) firstHL.getChildAt(0);
        LinearLayout.LayoutParams minorVLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f);
        minorVL.setLayoutParams(minorVLParams);

                // location text view
                TextView tv_time = (TextView) firstHL.getChildAt(1);
                LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tv_time.setLayoutParams(timeParams);
                tv_time.setPadding(0,0,0,15);
                tv_time.setGravity(Gravity.LEFT | Gravity.CENTER);

                // title text view
                TextView tv_title = (TextView) minorVL.getChildAt(1);
                LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tv_title.setLayoutParams(titleParams);
                tv_title.setMaxWidth(200);
                tv_title.setMaxLines(2);
                tv_title.setMaxHeight((int) locationActivity.getResources().getDimension(R.dimen.location_child_title_max_height));



        // rep days text view
        TextView tv_repDays = (TextView) firstHL.getChildAt(1);
        LinearLayout.LayoutParams repDaysParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f);
        tv_repDays.setLayoutParams(repDaysParams);
        tv_time.setGravity(Gravity.RIGHT | Gravity.CENTER);


/* second horizontal layout */
        LinearLayout secondHL = (LinearLayout) mainVL.getChildAt(1);
//        LinearLayout.LayoutParams secondHLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 2.0f);
//        secondHL.setLayoutParams(secondHLParams);

            // description text view
            TextView tv_description = (TextView) secondHL.getChildAt(0);
            LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            tv_description.setLayoutParams(descriptionParams);
            tv_description.setPadding(0,0,0,5);
            tv_description.setGravity(Gravity.LEFT | Gravity.CENTER);
            tv_description.setMaxWidth(200);
            tv_description.setMaxLines(5);
            tv_description.setVerticalScrollBarEnabled(true);
            tv_description.setMovementMethod(new ScrollingMovementMethod());
            tv_description.setMaxHeight((int) locationActivity.getResources().getDimension(R.dimen.location_child_descrip_max_height));


/* third horizontal layout */
        LinearLayout thirdHL = (LinearLayout) mainVL.getChildAt(2);
        LinearLayout.LayoutParams thirdHLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) locationActivity.getResources().getDimension(R.dimen.table_row_lower_border), 0.0f);
        thirdHL.setLayoutParams(thirdHLParams );
        thirdHL.setBackgroundColor(locationActivity.getResources().getColor(R.color.darker_grey));


        return tr;
    }









/** ------ Tab fragment stuff -------- **/

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public LocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationFragment.
     */
    public static LocationFragment newInstance(String param1, String param2) {
        LocationFragment fragment = new LocationFragment();
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
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            System.out.println("In on attach location fraggie");
            locationActivity = (Activity) context;
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