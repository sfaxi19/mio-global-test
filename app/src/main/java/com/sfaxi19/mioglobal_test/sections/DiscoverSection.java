package com.sfaxi19.mioglobal_test.sections;

import android.app.PendingIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sfaxi19.mioglobal_test.DiscovererBluetoothDevice;
import com.sfaxi19.mioglobal_test.MainActivity;
import com.sfaxi19.mioglobal_test.MyCallback;
import com.sfaxi19.mioglobal_test.R;
import com.sfaxi19.mioglobal_test.view.DeviceListView;

/**
 * Created by sfaxi19 on 06.07.16.
 */
public class DiscoverSection {

    private LinearLayout mainLayout;
    private MainActivity activity;
    private DiscovererBluetoothDevice discovererDevice;
    public DeviceListView deviceListView;
    public Button scanBtn;
    public Button saveBtn;
    public Button connectBtn;
    private final static String LOG_TAG = "myLogs";

    public DiscoverSection(MainActivity activity, View rootView){
        this.activity = activity;
        mainLayout = (LinearLayout) rootView.findViewById(R.id.fragmentLayout);
    }

    public void connectButtonView(){
        connectBtn = new Button(activity);
        connectBtn.setText("Подключиться");
        mainLayout.addView(connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deviceListView.getAddress()==null) return;
                activity.settings.put("device", deviceListView.getAddress());
                Log.d(LOG_TAG, "Discovered: " + deviceListView.getAddress());
                activity.hUpdateDeviceSettings.sendEmptyMessage(0);
            }
        });
    }

    public void saveButtonView(){
        saveBtn = new Button(activity);
        saveBtn.setText("Запомнить устройство");
        mainLayout.addView(saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deviceListView.getAddress()==null) return;
                activity.settings.put("device", deviceListView.getAddress());
                Log.d(LOG_TAG, "Save device: " + deviceListView.getAddress());
                activity.saveDeviceSettings();
            }
        });
    }

    public void view(){
        TextView titleTextView = new TextView(activity);
        titleTextView.setText("Поиск устройств");
        titleTextView.setTextSize(35);
        titleTextView.setPadding(0, 0, 0, 40);
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.addView(titleTextView);
        scanBtn = new Button(activity);
        scanBtn.setText("Сканировать");
        mainLayout.addView(scanBtn);
        deviceListView = new DeviceListView(activity,mainLayout);
        discovererDevice = new DiscovererBluetoothDevice(activity);
        deviceListView.setPadding(0, 25, 0, 25);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceListView.removeAllViews();
                Button tmp = (Button) v;
                tmp.setText("Сканирование...");
                tmp.setEnabled(false);
                if(connectBtn!=null) mainLayout.removeView(connectBtn);
                if(saveBtn!=null) mainLayout.removeView(saveBtn);
                discovererDevice.getDiscoveredDevices(new MyCallback(DiscoverSection.this));
            }
        });
    }

}

