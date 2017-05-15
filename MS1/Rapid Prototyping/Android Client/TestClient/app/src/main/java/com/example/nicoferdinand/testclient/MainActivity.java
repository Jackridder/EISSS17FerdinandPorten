package com.example.nicoferdinand.testclient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private RadioButton rbWarm;
    private RadioButton rbCool;
    private RadioButton rbSpring;
    private RadioButton rbloud;
    private RadioButton rbQuiet;
    private RadioButton rbRoomVolume;
    private Button button;
    private Button bnAufnahme;
    private TextView textView;
    private TextView tvTemp;
    private TextView tvVolume;
    private EditText name;
    private EditText secondname;
    private String server_url = "http://192.168.0.248:3000";
    private Timer timer;

    //MediaRecorderPart
    private static final String LOG_TAG = "AudioRecord";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private int maxAmp = 0;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.bn);
        textView = (TextView) findViewById(R.id.txt);
        name = (EditText) findViewById(R.id.username);
        secondname = (EditText) findViewById(R.id.usersecondname);
        bnAufnahme = (Button) findViewById(R.id.bnAufnahme);
        rbWarm = (RadioButton) findViewById(R.id.warm);
        rbCool = (RadioButton) findViewById(R.id.cool);
        rbSpring = (RadioButton) findViewById(R.id.spring);
        rbloud = (RadioButton) findViewById(R.id.loud);
        rbQuiet = (RadioButton) findViewById(R.id.quiet);
        rbRoomVolume = (RadioButton) findViewById(R.id.roomVolume);
        tvTemp = (TextView) findViewById(R.id.tvTemp);
        tvVolume = (TextView) findViewById(R.id.tvVolume);
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/noise.3gp";
        //Quick and Dirty:
        textView.setX(141);
        textView.setY(26);
        button.setX(260);
        button.setY(437);
        bnAufnahme.setX(65);
        bnAufnahme.setY(435);
        name.setX(16);
        name.setY(85);
        secondname.setX(16);
        secondname.setY(138);
        rbWarm.setX(16);
        rbWarm.setY(217);
        rbCool.setX(163);
        rbCool.setY(217);
        rbSpring.setX(16);
        rbSpring.setY(262);
        rbloud.setX(16);
        rbloud.setY(344);
        rbQuiet.setX(16);
        rbQuiet.setY(388);
        rbRoomVolume.setX(200);
        rbRoomVolume.setY(344);
        tvVolume.setX(43);
        tvVolume.setY(327);
        tvTemp.setX(47);
        tvTemp.setY(200);

        bnAufnahme.setOnClickListener(new View.OnClickListener() {
            boolean mStartRecording = true;
               public void onClick(View v) {
                   onRecord(mStartRecording);
                   if (mStartRecording) {
                       maxAmp = 0;
                       bnAufnahme.setText("Stop recording");
                   } else {
                       bnAufnahme.setText("Start recording");
                       final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

                       StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url+"/insertAudio", new Response.Listener<String>() {

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
                               params.put("aufnahme", maxAmp+"");
                               return params;
                           }
                       };
                       //Disable Multiple Requests
                       stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS*5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                       requestQueue.add(stringRequest);
                   }
                   mStartRecording = !mStartRecording;
           };
        });
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if((!rbSpring.isChecked() && !rbWarm.isChecked() && !rbCool.isChecked()) || (!rbloud.isChecked() && !rbQuiet.isChecked() && !rbRoomVolume.isChecked())){
                    textView.setText("Bitte Temperatur und Lautstärke auswählen");
                }
                else {
                    final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url + "/insertUserData", new Response.Listener<String>() {

                        public void onResponse(String response) {
                            textView.setText("InsertTest");
                            requestQueue.stop();
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            textView.setText("Server nicht gestartet oder Ressource nicht gefunden.");
                            requestQueue.stop();
                        }
                    }) {
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("name", name.getText().toString());
                            params.put("secondname", secondname.getText().toString());
                            params.put("volume", rbloud.isChecked() ? "loud" : rbQuiet.isChecked() ? "quiet" : "roomvolume");
                            params.put("weather", rbCool.isChecked() ? "cool" : rbWarm.isChecked() ? "warm" : "spring");
                            return params;
                        }
                    };
                    //Disable Multiple Requests
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                    startActivity(new Intent(MainActivity.this,Recommender.class));
                }
            }
        });
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

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.cool:
                if (checked)
                    if(rbSpring.isChecked() || rbWarm.isChecked()){
                        rbSpring.setChecked(false);
                        rbWarm.setChecked(false);
                    }
                    break;
            case R.id.spring:
                if (checked)
                    if(rbCool.isChecked() || rbWarm.isChecked()){
                        rbCool.setChecked(false);
                        rbWarm.setChecked(false);
                    }

                    break;
            case R.id.warm:
                if(checked)
                    if(rbSpring.isChecked() || rbCool.isChecked()){
                        rbSpring.setChecked(false);
                        rbCool.setChecked(false);
                    }
                    break;
            case R.id.loud:
                if (checked)
                    if(rbQuiet.isChecked() || rbRoomVolume.isChecked()){
                        rbQuiet.setChecked(false);
                        rbRoomVolume.setChecked(false);
                    }
                break;
            case R.id.quiet:
                if (checked)
                    if(rbloud.isChecked() || rbRoomVolume.isChecked()){
                        rbloud.setChecked(false);
                        rbRoomVolume.setChecked(false);
                    }

                break;
            case R.id.roomVolume:
                if(checked)
                    if(rbQuiet.isChecked() || rbloud.isChecked()){
                        rbQuiet.setChecked(false);
                        rbloud.setChecked(false);
                    }
                break;
        }
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
//Muster:
//Get:
 /*button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

                StringRequest stringRequest = new StringRequest(Request.Method.GET, server_url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        textView.setText(response);
                        requestQueue.stop();
                    }
                }, new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textView.setText("Server nicht gestartet oder Ressource nicht gefunden.");
                        requestQueue.stop();
                    }
                });
                requestQueue.add(stringRequest);
            }
        });
        */

 /*Post
 bnAufnahme.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, server_url, new Response.Listener<String>() {

                    public void onResponse(String response) {
                        requestQueue.stop();
                    }
                }, new Response.ErrorListener(){

                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error","Errorcode"+error);
                        requestQueue.stop();
                    }
                }) {
                    //Post Methode
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        try {
                            params.put("aufnahme", ""+aufnahme());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return params;
                    }
                };
                requestQueue.add(stringRequest);
            }
        });
  */