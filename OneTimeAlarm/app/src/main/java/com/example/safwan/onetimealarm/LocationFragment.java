package com.example.safwan.onetimealarm;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationFragment extends Fragment {

    private static Activity locationActivity;
    static HashMap<String, ArrayList> locationMap;
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
        child_row = (TableRow) view.findViewById(R.id.child_row);
        child_row.setVisibility(View.GONE);
        demoCollapse = false;
        img1 = (ImageView) view.findViewById(R.id.img1);
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(demoCollapse) {
                    demoCollapse = false;
                    child_row.setVisibility(View.GONE);
                }else {
                    demoCollapse = true;
                    child_row.setVisibility(View.VISIBLE);
                }
            }
        });




        return view;
    }

    public void setLocationHashMap(HashMap<String, ArrayList> map) {
        locationMap = map;
        location_table = locationActivity .findViewById(R.id.location_table);
        // getAllLocation();
//        = myView;
        createLocationRows();
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



    private void createLocationRows() {
            for(String s : locationMap.keySet()) {
            // create header rows
            TableRow tr = new TableRow(locationActivity);

            // create header row elements
            final TextView locationView = new TextView(locationActivity);
            TextView sizeView = new TextView(locationActivity);
            final ImageView imgView = new ImageView(locationActivity);

            // set params for header elements
            TableRow.LayoutParams p1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT,5.0f);
            locationView.setLayoutParams(p1);
            TableRow.LayoutParams p2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT,1.0f);
            sizeView.setLayoutParams(p2);
            TableRow.LayoutParams p3 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.MATCH_PARENT,0.0f);
            imgView.setLayoutParams(p3);

            // set contents of header elements
            locationView.setText(s);
            sizeView.setText(Integer.toString(locationMap.get(s).size()));
            imgView.setImageResource(R.drawable.down_arrow);


            // set click listener on header arrow down
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("hit me harder!");
                    TableRow trParent = (TableRow) imgView.getParent();
                    TextView strTv = (TextView) trParent.getChildAt(0);
                    String strLocation = strTv.getText().toString();
                    int len = locationMap.get(strLocation).size();
                    int index = location_table.indexOfChild(trParent);


                    if(isExpandedMap.get(strLocation )) {
                        for(int i = index+1; i <= index+len; i++) {
                            location_table.getChildAt(i).setVisibility(View.GONE);
                        }
                        isExpandedMap.put(strLocation ,false);
                    }else {
                        for(int i = index+1; i <= index+len; i++) {
                            location_table.getChildAt(i).setVisibility(View.VISIBLE);
                        }
                        isExpandedMap.put(strLocation ,true);
                    }
                }
            });

            tr.addView(locationView);
            tr.addView(sizeView);
            tr.addView(imgView);

            location_table.addView(tr);
            System.out.println("Added header row inside fragment table " + s);
            // set isExpanded of this row to false


            ArrayList<Alarm> arr = locationMap.get(s);
            for(Alarm a : arr) {
                location_table.addView(createChildRow(a));
                // hide this child row
                location_table.getChildAt(location_table.getChildCount()-1).setVisibility(View.GONE);
                System.out.println("Child count: " + location_table.getChildCount());
            }
            isExpandedMap.put(s,false);
        }
    }

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

        // set params for views
        TableRow.LayoutParams mainVerticalParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT,6.0f);

        LinearLayout.LayoutParams firstHorizontalParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);

        LinearLayout.LayoutParams minorVerticalParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,2.0f);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,2.0f);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,2.0f);

        LinearLayout.LayoutParams repeatingDaysParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,2.0f);

        LinearLayout.LayoutParams secondHorizontalParms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,2.0f);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);

        // assign params
        mainVerticalLayout.setLayoutParams(mainVerticalParams);
        firstHorizontalLayout.setLayoutParams(firstHorizontalParams);
        minorVerticalLayout.setLayoutParams(minorVerticalParams);
        timeView.setLayoutParams(timeParams);
        titleView.setLayoutParams(titleParams);

        repeatingDaysView.setLayoutParams(repeatingDaysParams);

        secondHorizontalLayout.setLayoutParams(secondHorizontalParms);
        descriptionView.setLayoutParams(descriptionParams);


        // set contents in view elements
        timeView.setText(alarmObj.getTimeString());
        System.out.println(alarmObj.getTimeString());

        titleView.setText(alarmObj.getTitle());
        System.out.println(alarmObj.getTitle());
        descriptionView.setText(alarmObj.getDescription());
        System.out.println(alarmObj.getDescription());
        repeatingDaysView.setText("S T W");

        // add elements to parents in order
        mainVerticalLayout.addView(firstHorizontalLayout,0);
        mainVerticalLayout.addView(secondHorizontalLayout,1);

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











/** ------ Tab fragment stuff -------- **/

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
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
    // TODO: Rename and change types and number of parameters
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



    // TODO: Rename method, update argument and hook method into UI event
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
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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