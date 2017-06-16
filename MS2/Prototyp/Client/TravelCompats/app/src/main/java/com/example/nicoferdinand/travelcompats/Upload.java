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
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nicoferdinand on 15.06.17.
 */

public class Upload extends AppCompatActivity {
    private List<NameValuePair> params;
    private Button record;
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

    // Requesting permission to RECORD_AUDIO, ACCESS_FINE_LOCATION, ACCES_COARSE_LOCATION
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToAccesFine = false;
    private boolean permissionToAccesCoarse = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};//, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#77CC00")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#8F7A70'> TravelCompats </font>"));
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
        alert = (TextView) findViewById(R.id.alert);
        record = (Button) findViewById(R.id.record);
        record.setOnClickListener(new View.OnClickListener() {
            boolean mStartRecording = true;

            public void onClick(View v) {
                //onRecord(mStartRecording);
                //if (mStartRecording) {
                //    maxAmp = 0;
                //} else {
                mStartRecording = !mStartRecording;
                params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", Home.user));
                params.add(new BasicNameValuePair("noise", "" + 35));
                ServerRequest sr = new ServerRequest();
                JSONObject json = sr.getJSON(server_url + "/audioData", params);
                Log.e("FirstStep", "Anfang");
                if (json != null) {
                    try {
                        Log.e("SecondStop", "Recorded");
                        String jsonstr = json.getString("response");
                        if (json.getBoolean("res")) {
                            String token = json.getString("token");
                            String grav = json.getString("grav");
                            SharedPreferences.Editor edit = pref.edit();

                            //Storing Data using SharedPreferences
                            edit.putString("token", token);
                            edit.putString("grav", grav);
                            edit.commit();
                            finish();
                        }

                        Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        Log.e("Failed", "Klappt nicht");
                        e.printStackTrace();
                    }
                }
            }

            // }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                alert.setText("Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude() );
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            locationUpdate();
        }
        alert.setText("Latitude: " + latitude + "\nLongitude: " + longitude );


    }

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

    private void locationUpdate(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
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
            else{
                locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
            }
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
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        timer = new Timer();
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
