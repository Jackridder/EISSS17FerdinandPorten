package com.example.nicoferdinand.travelcompats;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nicoferdinand on 07.07.17.
 */

public class Profile extends AppCompatActivity implements MultiSpinner.OnMultipleItemsSelectedListener{
    private String server_url = "http://192.168.0.248:3000";
    private TextView alert;
    private Button save;
    private CheckBox children;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        alert = (TextView) findViewById(R.id.alert);
        save = (Button) findViewById(R.id.save);
        children = (CheckBox) findViewById(R.id.children);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#77CC00")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#8F7A70'> TravelCompats </font>"));
        final String[] array = {"Deutsch", "Englisch", "Chinesisch", "Italienisch", "Türkisch"};
        final MultiSpinner multiSelectionSpinner = (MultiSpinner) findViewById(R.id.language);

        multiSelectionSpinner.setItems(array);
        //multiSelectionSpinner.setSelection(new int[]{0});
        multiSelectionSpinner.setListener(this);

        alert.setText("Bitte folgende Einstellungen vornehmen");
        Log.d("Spinner Items", multiSelectionSpinner.getSelectedItemsAsString());

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!multiSelectionSpinner.getSelectedItemsAsString().isEmpty()){
                    final RequestQueue requestQueue = Volley.newRequestQueue(Profile.this);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url + "/userData/config", new Response.Listener<String>() {

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
                            params.put("username", Home.user);
                            params.put("language", multiSelectionSpinner.getSelectedItemsAsString());
                            params.put("children",children.isChecked() ? "1" : "0");
                            return params;
                        }
                    };
                    //Disable Multiple Requests
                    startActivity(new Intent(Profile.this, Home.class));
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                }else{
                    alert.setText("Bitte mindestens eine Sprache auswählen");
                }

            }

        });
    }
    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {
        Toast.makeText(this, strings.toString(), Toast.LENGTH_LONG).show();
    }

}
