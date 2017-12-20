package com.example.safwan.onetimealarm;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import java.util.HashMap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.safwan.onetimealarm.R;



public class StartupActivity extends AppCompatActivity implements MainAlarmFragment.OnFragmentInteractionListener, LocationFragment.OnFragmentInteractionListener{

    private static boolean isDeleteSet = false;
    static ViewPager startup_pager;
    static PagerAdapter startupPageAdapter;
    TabLayout startup_tabLayout;
    private Menu currentMenu;
    MainAlarmFragment mainAlarmFragmentObj;
    LocationFragment locationFragmentObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        setupTabbedFragments(savedInstanceState);
//        setReminder();
//        mainAlarmFragmentObj = (MainAlarmFragment)getFragmentManager().findFragmentByTag("MY_FRAGMENT");

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Override this method in the activity that hosts the Fragment and call super
        // in order to receive the result inside onActivityResult from the fragment.
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_startup, menu);
        currentMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int tabIndex;
        switch (item.getItemId()) {
            case R.id.delete_alarm:
                System.out.println("delete ops");
                Bundle bundle = new Bundle();
                bundle.putString("delete-ops", "From Activity");
                tabIndex = startup_tabLayout.getSelectedTabPosition();
                if(tabIndex == 0){
                    mainAlarmFragmentObj.setAlarmDeleteView(true);
                    // change option menu to give delete option
                    setDeleteMenu(true);
                }
                return true;

            case R.id.settings:
                System.out.println("settings ops");
                return true;

            case R.id.action_delete_alarm:
                tabIndex = startup_tabLayout.getSelectedTabPosition();
                if(tabIndex == 0){
                    mainAlarmFragmentObj.performMenuDeleteAction();
                    // change option menu to give delete option
                    setDeleteMenu(false);
                }
                return true;

            case R.id.refresh:
                mainAlarmFragmentObj.checkDstAlarms();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    public void setDeleteMenu(boolean val) {
        isDeleteSet = val;  // set static variable, used by onPrepareOptionsMenu
        onPrepareOptionsMenu(currentMenu);
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

   /* private void setReminder() {
        Intent i = new Intent(StartupActivity.this, NotifyService.class);
        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, pi);
//         Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.HOUR, 0);
//        calendar.set(Calendar.AM_PM, Calendar.AM);
//        calendar.add(Calendar.DAY_OF_MONTH, 1);

        System.out.println("creating alarm");

    }
*/



    /**-------- Tab layout stuff --------**/
    protected void setupTabbedFragments(Bundle savedInstanceState) {

        startup_tabLayout = (TabLayout) findViewById(R.id.startup_tabLayout);

// Create a new Tab named "First"
        TabLayout.Tab firstTab = startup_tabLayout.newTab();
        firstTab.setText("Alarms"); // set the Text for the first Tab
// first tab
        startup_tabLayout.addTab(firstTab); // add  the tab at in the TabLayout
// Create a new Tab named "Second"
        TabLayout.Tab secondTab = startup_tabLayout.newTab();
        secondTab.setText("Locations"); // set the Text for the second Tab
        startup_tabLayout.addTab(secondTab); // add  the tab  in the TabLayout

        startup_pager = (ViewPager)findViewById(R.id.startup_pager);
        startupPageAdapter = new PagerAdapter(getSupportFragmentManager(),startup_tabLayout.getTabCount());
        startup_pager.setAdapter(startupPageAdapter);
        startup_pager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(startup_tabLayout));


        // get frag objs
        mainAlarmFragmentObj = (MainAlarmFragment) startupPageAdapter.getItem(0);
        locationFragmentObj = (LocationFragment) startupPageAdapter.getItem(1);

        startup_tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                startup_pager.setCurrentItem(tab.getPosition());
                System.out.println("Selecting" + tab.getPosition());
                if(tab.getPosition() == 1) {
                    locationFragmentObj.setLocationHashMap(mainAlarmFragmentObj.getLocationHashMap() );
                }else{
                    TableLayout location_table = (TableLayout) findViewById(R.id.location_table);
                    for (int i=1;i<location_table.getChildCount(); i++)
                        location_table.removeViewAt(i);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**-------- Tab layout stuff --------**/
}