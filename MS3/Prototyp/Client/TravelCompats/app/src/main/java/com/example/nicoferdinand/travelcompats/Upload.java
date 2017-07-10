package com.example.nicoferdinand.travelcompats;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.NameValuePair;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nicoferdinand on 15.06.17.
 */

public class Upload extends AppCompatActivity {
    private List<NameValuePair> params;
    private Button record;
    private Button upload;
    private TextView rating;
    private Timer timer;
    private String server_url = "http://192.168.0.248:3000";
    private SharedPreferences pref;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView alert;

    private static double longitude;
    private static double latitude;
    //MediaRecorderPart
    private static final String LOG_TAG = "AudioRecord";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private int maxAmp = 0;

    private boolean used = false;
    // Requesting permission to RECORD_AUDIO, ACCESS_FINE_LOCATION, ACCES_COARSE_LOCATION
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};//, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override


    //Nach Rechten Fragen erst ab 6.0 möglich
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults){

        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case 10:
                if(grantResults.length>0 && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    locationUpdate();
                }
                break;
        }

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);
        //Header Design einstellen
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#77CC00")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#8F7A70'> TravelCompats </font>"));
        //Navigationsleiste
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        startActivity(new Intent(Upload.this, Home.class));
                        break;
                    case R.id.action_search:
                        startActivity(new Intent(Upload.this, Search.class));
                        break;
                    case R.id.action_upload:
                        startActivity(new Intent(Upload.this, Upload.class));
                        break;
                    /*case R.id.action_settings:

                        break;
                        */
                }
                return true;
            }
        });

        upload = (Button) findViewById(R.id.upload);
        alert = (TextView) findViewById(R.id.alert);
        record = (Button) findViewById(R.id.record);
        rating = (TextView) findViewById(R.id.rating);
        //Pfad von der Aufnahme
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/noise.3gp";
        //Wird aufgenommen, startet die Aufnahme und der Buttontext wird angepasst
        //Findet eine Aufnahme statt und der Button wird erneut betätigt, wird alles zurückgesetzt
        record.setOnClickListener(new View.OnClickListener() {
            boolean mStartRecording = true;

            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    maxAmp = 0;
                    record.setText("Stoppe Aufnahme");
                } else {
                    record.setText("Aufnahme");
                }
                mStartRecording = !mStartRecording;
            }
        });

        //Daten sollen hochgeladen werden
        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Wenn alle Felder ausgefüllt sind (ausgenommen sind Audio und Rating, da muss mindestens eins von beidem ausgefüllt sein)
                if((longitude != 0 && latitude != 0 && maxAmp != 0) || (longitude != 0 && latitude != 0 && (!rating.getText().toString().equals("Bewertung für aktuellen Standort") && !rating.getText().toString().equals("")))){
                    //Erstelle Request
                    final RequestQueue requestQueue = Volley.newRequestQueue(Upload.this);
                    StringRequest stringRequest = new StringRequest(Request.Method.PUT, server_url + "/audioData", new Response.Listener<String>() {
                        public void onResponse(String response) {
                            requestQueue.stop();
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error", "Errorcode" + error);
                            requestQueue.stop();
                        }
                    }) {
                        //PUT Method
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("noise", maxAmp + "");
                            //TH Bibl.51.022166, 7.562483
                            params.put("longitude", longitude+"");
                            params.put("latitude", latitude+"");
                            params.put("username", Home.user);
                            //Sollte das Rating leer sein, dann wird ein leerer String übermittelt
                            params.put("rating", rating.getText().equals("Bewertung für aktuellen Standort") ? "" : rating.getText().toString());
                            return params;
                        }
                    };
                    //Disable Multiple Requests
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                }else{
                    alert.setText("Koordinaten konnten nicht geholt werden oder es wurde keine Aufnahme vorgenommen oder kein Text eingegeben.");
                }
            }
        });

        //LocationManager und -Listener sind für die GPS Koordinaten
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(!used) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    //Request starten
                    final RequestQueue requestQueue = Volley.newRequestQueue(Upload.this);
                    StringRequest stringRequest = new StringRequest(Request.Method.PUT, server_url + "/audioData", new Response.Listener<String>() {

                        public void onResponse(String response) {
                            //Dem Benutzer wird der aktuelle Standort angezeigt zu welchem er Daten hochladen möchte
                            Log.d("Location","Empfangen ");
                            alert.setText(response);
                            requestQueue.stop();
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error", "Errorcode" + error);
                            requestQueue.stop();
                        }
                    }) {
                        //PUT Method
                        protected Map<String, String> getParams() {
                            Log.d("PUT", "Koordinaten übermittelt");
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
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        //Mediarecorder wird erstellt für die Aufnahme
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        timer = new Timer();
        //Einstellung für im 500ms Takt
        timer.scheduleAtFixedRate(new RecorderTask(mRecorder), 0, 500);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
    }

    class RecorderTask extends TimerTask {
        private MediaRecorder recorder;

        public RecorderTask(MediaRecorder recorder) {
            this.recorder = recorder;
        }

        //Setze maxAmp auf aufgenommenen Wert (nur wenn der neue Wert größer als der alte ist)
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    int amplitude = recorder.getMaxAmplitude();
                    double amplitudeDb = 20 * Math.log10((double)Math.abs(amplitude));
                    Log.e("MaxAmp", "MaxAmp: "+maxAmp);
                    if(maxAmp < amplitudeDb){
                        maxAmp = (int)amplitudeDb;
                    }
                }
            });
        }
    }
}
