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
import android.widget.TextView;


/**
 * Created by nicoferdinand on 15.06.17.
 */

public class Home extends AppCompatActivity {
    private TextView alert;
    public static String user;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#77CC00")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#8F7A70'> TravelCompats </font>"));
        alert = (TextView) findViewById(R.id.alert);
        user = Sign.user;
        if(user != null){
            alert.setText("Hallo " + user);
        }
        else{
            user = Register.user;
            alert.setText("Hallo " + user);
        }


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            public boolean onNavigationItemSelected(@NonNull MenuItem item){
                switch (item.getItemId())
                {
                    case R.id.action_home:
                        startActivity(new Intent(Home.this, Home.class));
                        break;
                    case R.id.action_search:
                        startActivity(new Intent(Home.this, Search.class));
                        break;
                    case R.id.action_upload:
                        startActivity(new Intent(Home.this, Upload.class));
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
