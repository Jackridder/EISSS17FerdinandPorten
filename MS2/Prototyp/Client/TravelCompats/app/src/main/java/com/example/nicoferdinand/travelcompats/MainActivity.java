package com.example.nicoferdinand.travelcompats;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button b_Register;
    private Button b_SignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#77CC00")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#8F7A70'> TravelCompats </font>"));

        b_Register = (Button) findViewById(R.id.register);
        b_Register.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                startActivity(new Intent(MainActivity.this, Register.class));
            }
        });

        b_SignIn = (Button) findViewById(R.id.signin);
        b_SignIn.setOnClickListener(new View.OnClickListener(){
                public void onClick (View v){
                    startActivity(new Intent(MainActivity.this, Sign.class));
                }
        });
    }

}
