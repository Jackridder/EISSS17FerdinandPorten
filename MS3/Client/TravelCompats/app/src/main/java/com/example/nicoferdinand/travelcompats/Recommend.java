package com.example.nicoferdinand.travelcompats;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
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
 * Created by nicoferdinand on 07.07.17.
 */

public class Recommend extends AppCompatActivity {
    private String server_url = "http://192.168.0.248:3000";
    private RadioButton restaurant;
    private TextView alert;
    private boolean used = false;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static double longitude;
    private static double latitude;
    private String empfehlungselemente;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend);
        //Header Design Anpassung
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#77CC00")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#8F7A70'> TravelCompats </font>"));

        alert = (TextView) findViewById(R.id.alert);
        restaurant = (RadioButton) findViewById(R.id.restaurant);
        //LocationManager und -Listener sind für die GPS Koordinaten
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(!used) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    //Request starten
                    final RequestQueue requestQueue = Volley.newRequestQueue(Recommend.this);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url + "/audioData", new Response.Listener<String>() {

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
                            Log.d("Post", "Koordinaten übermittelt");
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("longitude", longitude+"");
                            params.put("latitude", latitude+"");
                            return params;
                        }
                    };
                    //Disable Multiple Requests
                    //stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                    Log.d("Latitude", location.getLatitude() + "");
                    Log.d("Latitude", location.getLongitude() + "");
                }
                used = true;


            }
            //Methoden sind noch leer, da für PoC irrelevant
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        locationUpdate();


    }

    private void locationUpdate(){
        //Für Version ab Android 6.0
        if(Build.VERSION.SDK_INT >= 23){
            //Fehlen Rechte, werden sie angefordert
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET},10);
                return;
            }
            //Sind alle Rechte gegeben, dürfen die Koordinaten ermittelt werden
            else{
                locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
            }
        }
        else{
            //Für ältere Versionen als Android 6.0
            locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
        }


    }
    public void onRadioButtonClicked(View view) {

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.restaurant:
                if (restaurant.isChecked()){
                    Log.d("Test", "longitude: " + longitude + " latitude " + latitude);
                    if(longitude != 0 && latitude != 0){
                        final RequestQueue requestQueue = Volley.newRequestQueue(Recommend.this);

                        //Erstelle Request an Server.
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url + "/Empfehlung", new Response.Listener<String>() {

                            public void onResponse(String response) {
                                empfehlungselemente = response;
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
                                Log.d("Gesendet")
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("type","restaurant");
                                params.put("latitude",latitude+"");
                                params.put("longitude", longitude+"");
                                return params;
                            }
                        };
                        //Disable Multiple Requests
                        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        requestQueue.add(stringRequest);
                    }

                }
                break;
        }
    }

}
