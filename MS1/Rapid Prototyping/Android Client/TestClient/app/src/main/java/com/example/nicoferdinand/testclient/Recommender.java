package com.example.nicoferdinand.testclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class Recommender extends AppCompatActivity {

    public TextView recommend;
    private String server_url = "http://192.168.0.248:3000/Empfehlung";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommender);
        LinearLayout ll = new LinearLayout(this);
        recommend = new TextView(this);
        ll.addView(recommend,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        setContentView(ll);
        final RequestQueue requestQueue = Volley.newRequestQueue(Recommender.this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, server_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                recommend.setText(response);
                Log.d("Response", "ResponseText: "+ response);
                requestQueue.stop();
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                recommend.setText("Fehler beim Aufrufen");
                requestQueue.stop();
            }
        });
        requestQueue.add(stringRequest);
    }


}
