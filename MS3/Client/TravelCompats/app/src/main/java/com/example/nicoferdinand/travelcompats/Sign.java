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

public class Sign extends AppCompatActivity {
    private Button signin;
    private TextView alert;
    private EditText username;
    private EditText password;
    public static String user;
    //Testen
    //private String server_url = "http://10.3.205.250:3000";
    //Zuhause
    private String server_url = "http://192.168.0.248:3000";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        //Header Design
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#77CC00")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#8F7A70'> TravelCompats </font>"));
        //Zurück Button einfügen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        alert = (TextView) findViewById(R.id.alert);
        username = (EditText) findViewById(R.id.e_userName);
        password = (EditText) findViewById(R.id.e_password);

        //Einloggen
        signin = (Button) findViewById(R.id.signin);
        signin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Alle Felder müssen ausgefüllt sein
                if (TextUtils.isEmpty(username.getText().toString()) || TextUtils.isEmpty(password.getText().toString())) {
                    alert.setText("Bitte alle Felder ausfüllen!");
                } else {
                    //Request an Server erstellen
                    final RequestQueue requestQueue = Volley.newRequestQueue(Sign.this);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url + "/login", new Response.Listener<String>() {
                        public void onResponse(String response) {
                            Log.e("Response", response);
                            //Benutzer ist nicht angelegt
                            if (response.equals("0")) {
                                alert.setText("Benutzer konnte nicht gefunden werden. Versuchen Sie es nochmal.");
                            } else {
                                //Benutzer ist angelegt und wird angemeldet
                                user = response;
                                startActivity(new Intent(Sign.this, Home.class));
                            }
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
                            params.put("password", password.getText().toString());
                            return params;
                        }
                    };
                    //Disable Multiple Requests
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);

                }
            }
        });
    }

    //Zurück zum Home Menü
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(Sign.this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
