package com.karim.safwan.notifyme;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.Toast;


public class StartupActivity extends AppCompatActivity implements MainAlarmFragment.OnFragmentInteractionListener, LocationFragment.OnFragmentInteractionListener{


    /** Final Variables**/
    public static final int OPTION_MENU_FLAG_NONE = 0;
    public static final int OPTION_MENU_FLAG_DELETE = 1;
    public static final int OPTION_MENU_FLAG_LOCATION = 2;

    /** Static Variables**/
    private static boolean isDeleteSet = false;
    private static int optionMenuFlag = OPTION_MENU_FLAG_NONE;
    static ViewPager startup_pager;
    static PagerAdapter startupPageAdapter;
    /** Plain Old Variables**/
    TabLayout startup_tabLayout;
    private Menu currentMenu;
    MainAlarmFragment mainAlarmFragmentObj;
    LocationFragment locationFragmentObj;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        setupTabbedFragments(savedInstanceState);
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

                // change option menu to give delete option and view
                if(tabIndex == 0){
                    setDeleteMenu();
                }
                return true;

            case R.id.action_delete_alarm:
                tabIndex = startup_tabLayout.getSelectedTabPosition();
                // change option menu to remove delete option and view
                if(tabIndex == 0){
                    removeDeleteMenu();
                    toastMessage("Deleting selected alarms...", Toast.LENGTH_SHORT);
                }
                return true;

            case R.id.refresh:
                mainAlarmFragmentObj.checkDstAlarms();
                toastMessage("Refreshing all alarms...", Toast.LENGTH_SHORT);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    public void setDeleteMenu() {
        mainAlarmFragmentObj.setAlarmDeleteView();
        optionMenuFlag = OPTION_MENU_FLAG_DELETE;
        onPrepareOptionsMenu(currentMenu);
    }

    public void removeDeleteMenu() {
        mainAlarmFragmentObj.performMenuDeleteAction();
        optionMenuFlag = OPTION_MENU_FLAG_NONE;
        onPrepareOptionsMenu(currentMenu);
    }


    public boolean onPrepareOptionsMenu(Menu menu) {
        if(menu != null)
            menu.clear();

        if(optionMenuFlag == OPTION_MENU_FLAG_LOCATION) {
            ActivityCompat.invalidateOptionsMenu(StartupActivity.this);
            getMenuInflater().inflate(R.menu.menu_startup_locations, menu);
            System.out.println("trying to show menu locations");
        }
        else if(optionMenuFlag == OPTION_MENU_FLAG_DELETE) {
            ActivityCompat.invalidateOptionsMenu(StartupActivity.this);
            getMenuInflater().inflate(R.menu.menu_startup_delete, menu);

        } else {
            ActivityCompat.invalidateOptionsMenu(StartupActivity.this);
            getMenuInflater().inflate(R.menu.menu_startup, menu);
        }

        return true;
    }


    public void inflateLocationMenu() {

        optionMenuFlag = OPTION_MENU_FLAG_LOCATION;
        onPrepareOptionsMenu(currentMenu);
        System.out.println("trying to show menu locations");

    }

    public void inflateAlarmMenu() {
        optionMenuFlag = OPTION_MENU_FLAG_NONE;
        onPrepareOptionsMenu(currentMenu);
        mainAlarmFragmentObj.restoreMainStartupView();
        System.out.println("trying to show menu startup");
    }


    @Override
    public void onBackPressed(){
        System.out.println(optionMenuFlag);
        if(optionMenuFlag == OPTION_MENU_FLAG_DELETE) {
            inflateAlarmMenu();
        } else{
            optionMenuFlag = OPTION_MENU_FLAG_NONE;
            super.onBackPressed();
        }
    }

    public void toastMessage(String str, int duration) {
        Context context = StartupActivity.this;
        CharSequence text = str;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**-------- Tab layout stuff --------**/
    protected void setupTabbedFragments(Bundle savedInstanceState) {

        startup_tabLayout = (TabLayout) findViewById(R.id.startup_tabLayout);

        // Create a new Tab named
        TabLayout.Tab firstTab = startup_tabLayout.newTab();
        firstTab.setText("Alarms");
        startup_tabLayout.addTab(firstTab);

        // Create a new Tab named
        TabLayout.Tab secondTab = startup_tabLayout.newTab();
        secondTab.setText("Locations");
        startup_tabLayout.addTab(secondTab);

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

                // selected locations tab
                if(tab.getPosition() == 1) {
                    locationFragmentObj.setLocationHashMap(mainAlarmFragmentObj.getLocationHashMap() );
                    inflateLocationMenu();
                }else {
                    // selected alarms tab
                    TableLayout location_table = (TableLayout) findViewById(R.id.location_table);
                    location_table.removeAllViews();
                    inflateAlarmMenu();
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


//    private FragmentTransaction getFragmentTransaction() {
//        if(fragmentTransaction == null)
//            fragmentTransaction = getFragmentTransaction();
//    }


    /**-------- Tab layout stuff --------**/
}