/*
package com.example.safwan.onetimealarm;

import android.annotation.TargetApi;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

*/
/**
 * Created by safwan on 22/12/2017.
 *//*


public class tmep {



    */
/**
     * Function: This is a big method responsible for creating both the header and child rows. Child rows are created by another method, but are called from this one. Maybe we can change that?
     * Stimuli: Called by setLocationHashMap.
     * Layout of header rows: TV/TV/ImgV
     *//*

    private void createLocationRows() {
        for(String s : locationMap.keySet()) {
            // create header rows
            TableRow tr = new TableRow(locationActivity);

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
            // tr = addHeaderRowDesign(tr);

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


    */
/**
     * Function: Creates child row which is hidden to start off with.
     * Stimuli: Called by createLocationRows for each alarm for a location key.
     * Return: Child Table Row.
     * Layout: VL:(HL:(VL: TV/TV) / HL: TV) / VL: TV
     *//*

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


    // header row params, colors, desgin
    @TargetApi(23)
    private TableRow addHeaderRowDesign(TableRow tr) {

        // table row
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        rowParams.setMargins(0,0,0,2);
        tr.setLayoutParams(rowParams);
        tr.setBackgroundColor(getResources().getColor(R.color.mainRowBackground));

        // location text view
        TextView locationView = (TextView) tr.getChildAt(0);
        TableRow.LayoutParams locationParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, R.dimen.location_header_height,4.0f);
        locationView.setLayoutParams(locationParams);
        locationView.setGravity(Gravity.CENTER| Gravity.LEFT);
        locationView.setTextAppearance(R.style.TextAppearance_AppCompat);


        // number text view
        TextView sizeView = (TextView) tr.getChildAt(1);
        TableRow.LayoutParams numberParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT,1.0f);
        sizeView.setLayoutParams(numberParam);
        sizeView.setTextAppearance(R.style.TextAppearance_AppCompat);

        // img view
        TextView imgView = (TextView) tr.getChildAt(2);
        TableRow.LayoutParams imgParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, R.dimen.height_down_arrow_location ,1.0f);
        imgView.setLayoutParams(imgParams);

        return tr;
    }

    private TableRow addChildRowDesign(TableRow tr) {

        return tr;
    }

}
*/
