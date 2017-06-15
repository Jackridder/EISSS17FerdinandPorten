package com.example.nicoferdinand.travelcompats;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;


/**
 * Created by nicoferdinand on 15.06.17.
 */

public class Home extends AppCompatActivity {
    private TextView alert;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#77CC00")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#8F7A70'> TravelCompats </font>"));
        alert = (TextView) findViewById(R.id.alert);
        alert.setText("Hallo " + Sign.user);
    }
}
