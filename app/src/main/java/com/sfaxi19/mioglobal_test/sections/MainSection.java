package com.sfaxi19.mioglobal_test.sections;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sfaxi19.mioglobal_test.MainActivity;
import com.sfaxi19.mioglobal_test.ForegroundService;
import com.sfaxi19.mioglobal_test.R;


/**
 * Created by sfaxi19 on 28.06.16.
 */
public class MainSection {

    private final static String LOG_TAG = "myLogs";
    private MainActivity activity;
    private LinearLayout mainLayout;
    private final static String TEST_TAG ="Create and Destroy";
    private TextView heartRate;

    public Button stopButton;

    public MainSection(MainActivity activity, View rootView) {
        this.activity = activity;
        mainLayout = (LinearLayout) rootView.findViewById(R.id.fragmentLayout);
    }

    public void view() {
        Log.d(TEST_TAG, "MainSection use activity: " + activity.hashCode());
        //activity.hGetSettingsFromService.sendEmptyMessage(0);
        mainLayout.removeAllViews();
        startVisualisation();

        TextView titleTextView = new TextView(activity);
        titleTextView.setText("Пульс");
        titleTextView.setTextSize(35);
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.addView(titleTextView);

        heartRate = new TextView(activity.getApplicationContext());
        mainLayout.addView(heartRate);
        heartRate.setTextSize(150);
        heartRate.setGravity(Gravity.CENTER);

        stopButton = new Button(activity);
        if(activity.isServiceRunning(ForegroundService.class)) {
            stopButton.setText("Стоп");
        }else{
            stopButton.setText("Старт");
        }

        stopButton.setTextSize(20);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stopButton.getText().toString().equals("Стоп")){
                    activity.stopService(new Intent(activity, ForegroundService.class));
                    if(activity.mBound){
                        activity.unbindService(activity.mConnection);
                        activity.mBound = false;
                    }
                    stopButton.setText("Старт");
                }else {

                    if(BluetoothAdapter.getDefaultAdapter().isEnabled()){
                        activity.startService();
                        stopButton.setText("Стоп");
                    }else{
                        activity.bluetoothEnable();
                    }
                }
            }
        });
        mainLayout.addView(stopButton);

        TextView nameTitle = new TextView(activity);
        nameTitle.setText("Имя устройства:");
        nameTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.addView(nameTitle);
        activity.nameTextView = new TextView(activity);
        activity.nameTextView.setText(activity.settings.get("name"));
        activity.nameTextView.setTextSize(150);
        activity.nameTextView.setTextColor(Color.RED);
        activity.nameTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        activity.nameTextView.setPadding(0,20,0,0);
        mainLayout.addView(activity.nameTextView);

    }

    private void startVisualisation(){
        activity.threadHeartRate = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(1000);
                        if(Thread.interrupted()){
                            Log.d(LOG_TAG, "visualisation thread - close");
                            return;
                        }
                    } catch (InterruptedException e) {
                        Log.d(LOG_TAG, "visualisation thread - close");
                        //e.printStackTrace();

                        return;
                    }

                    handlerGetHeartRate.sendEmptyMessage(0);
                    handlerCheckConnect.sendEmptyMessage(0);
                }
            }
        });
        activity.threadHeartRate.start();
    }

    Handler handlerGetHeartRate = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            heartRate.setText(activity.getHeartRateFromService());
        }
    };

    Handler handlerCheckConnect = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(!activity.mBound) return;
            activity.hCheckConnect.sendEmptyMessage(0);
        }
    };

}
