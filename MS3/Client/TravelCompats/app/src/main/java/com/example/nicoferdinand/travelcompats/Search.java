package com.example.nicoferdinand.travelcompats;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;

/**
 * Created by nicoferdinand on 15.06.17.
 */

//Platzhalter für zukünftige Elemente
public class Search extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        //Header Design anpassen
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#77CC00")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#8F7A70'> TravelCompats </font>"));
        //Navigationsleiste
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            public boolean onNavigationItemSelected(@NonNull MenuItem item){
                switch (item.getItemId())
                {
                    case R.id.action_home:
                        startActivity(new Intent(Search.this, Home.class));
                        break;
                    case R.id.action_search:
                        startActivity(new Intent(Search.this, Search.class));
                        break;
                    case R.id.action_upload:
                        startActivity(new Intent(Search.this, Upload.class));
                        break;
                    /*case R.id.action_settings:

                        break;
                        */
                }
                return true;
            }
        });
    }
}
