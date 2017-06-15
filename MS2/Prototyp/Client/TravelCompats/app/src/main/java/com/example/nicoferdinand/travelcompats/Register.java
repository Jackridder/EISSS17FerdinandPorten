package com.example.nicoferdinand.travelcompats;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nicoferdinand on 14.06.17.
 */


public class Register extends AppCompatActivity {
    private Button register;
    private EditText email;
    private EditText username;
    private EditText password;
    private TextView alert;
    private String server_url = "http://192.168.0.248:3000";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#77CC00")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#8F7A70'> TravelCompats </font>"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        alert = (TextView) findViewById(R.id.tv_Alert);
        email = (EditText) findViewById(R.id.e_email);
        username = (EditText) findViewById(R.id.e_userName);
        password = (EditText) findViewById(R.id.e_password);
        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                if( TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(username.getText().toString()) || TextUtils.isEmpty(password.getText().toString())){
                    alert.setText("Bitte alle Felder ausfüllen!");
                }
                else{
                    final RequestQueue requestQueue = Volley.newRequestQueue(Register.this);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url+"/userData", new Response.Listener<String>() {
                        public void onResponse(String response) {
                            requestQueue.stop();
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error", "Errorcode" + error);
                            requestQueue.stop();
                        }
                    }) {
                        //Post Method
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("username", username.getText().toString());
                            params.put("email", email.getText().toString());
                            params.put("password", password.getText().toString());
                            return params;
                        }
                    };
                    //Disable Multiple Requests
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS*5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                    startActivity(new Intent(Register.this, Home.class));
                }

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(Register.this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
