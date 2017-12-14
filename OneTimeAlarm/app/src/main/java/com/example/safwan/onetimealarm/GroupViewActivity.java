package com.example.safwan.onetimealarm;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupViewActivity extends AppCompatActivity {

    HashMap<String, ArrayList<Alarm> > locationMap;
    Button grpbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);


        Intent i = getIntent();
        if(i.hasExtra("hashmap")){
            locationMap = (HashMap<String, ArrayList<Alarm> >) i.getSerializableExtra("hashmap");
            System.out.println("found map");
        }

        grpbtn = (Button) findViewById(R.id.grpbtn);
//        grpbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });


        // add frag
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LocationFragment locationFragment = new LocationFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("hashmap",locationMap);
        locationFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.frag, locationFragment);
        fragmentTransaction.commit();
//


    }


}
