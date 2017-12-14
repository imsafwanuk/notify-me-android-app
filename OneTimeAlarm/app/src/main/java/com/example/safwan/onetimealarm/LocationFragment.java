package com.example.safwan.onetimealarm;

import android.app.AlarmManager;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
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

/**
 * Created by safwan on 11/12/2017.
 */

public class LocationFragment extends Fragment {

    HashMap<String, ArrayList<Alarm> > locationMap;
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

        location_table = (TableLayout) view.findViewById(R.id.location_table);
        isExpandedMap.clear();
        Bundle b = this.getArguments();
        if(b != null && b.getSerializable("hashmap") != null){
            locationMap = (HashMap<String, ArrayList<Alarm> >) b.getSerializable("hashmap");
            System.out.println("found map in fragment");
//            getAllLocation();
            createLocationRows(view);
        }

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

        // Inflate the layout for this fragment
        return view;
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


    private void createLocationRows(View view) {
        for(String s : locationMap.keySet()) {
            // create header rows
            TableRow tr = new TableRow(view.getContext());

            // create header row elements
            final TextView locationView = new TextView(view.getContext());
            TextView sizeView = new TextView(view.getContext());
            final ImageView imgView = new ImageView(view.getContext());

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
            System.out.println("Added header row " + s);
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
        LinearLayout mainVerticalLayout = new LinearLayout(view.getContext());
                mainVerticalLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout firstHorizontalLayout = new LinearLayout(view.getContext());
                    firstHorizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout minorVerticalLayout = new LinearLayout(view.getContext());
                    minorVerticalLayout.setOrientation(LinearLayout.VERTICAL);

                TextView timeView = new TextView(view.getContext());
                TextView titleView = new TextView(view.getContext());
                TextView repeatingDaysView = new TextView(view.getContext());

            LinearLayout secondHorizontalLayout = new LinearLayout(view.getContext());
                    secondHorizontalLayout .setOrientation(LinearLayout.HORIZONTAL);
                TextView descriptionView = new TextView(view.getContext());

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
        TableRow tr = new TableRow(view.getContext());
        tr.addView(mainVerticalLayout);
        System.out.println("returning minor rows");
        return tr;
    }
}
