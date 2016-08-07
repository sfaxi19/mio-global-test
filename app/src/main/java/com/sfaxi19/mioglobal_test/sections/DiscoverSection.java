package com.sfaxi19.mioglobal_test.sections;

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
import com.sfaxi19.mioglobal_test.view.MessageListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

/**
 * Created by sfaxi19 on 06.07.16.
 */
public class DiscoverSection implements ISection{

    LinearLayout mainLayout;
    MainActivity activity;
    public MessageListView listView;
    DiscovererBluetoothDevice discovererDevice;
    public Button scanBtn;
    public Button connectBtn;
    private final static String LOG_TAG = "myLogs";

    public DiscoverSection(MainActivity activity, View rootView){
        this.activity = activity;
        mainLayout = (LinearLayout) rootView.findViewById(R.id.fragmentLayout);
        TextView titleTextView = new TextView(activity);
        titleTextView.setText("Поиск устройств");
        titleTextView.setTextSize(35);
        titleTextView.setPadding(0, 0, 0, 40);
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.addView(titleTextView);
        scanBtn = new Button(activity);
        scanBtn.setText("Сканировать");
        mainLayout.addView(scanBtn);
        listView = new MessageListView(activity,mainLayout);
        discovererDevice = new DiscovererBluetoothDevice(activity);
        listView.setPadding(0,15,0,15);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.removeAllViews();
                Button tmp = (Button) v;
                tmp.setText("Сканирование...");
                tmp.setEnabled(false);
                if(connectBtn!=null) mainLayout.removeView(connectBtn);
                discovererDevice.getDiscoveredDevices(new MyCallback(DiscoverSection.this));
            }
        });

    }

    public void connectButtonView(){
        connectBtn = new Button(activity);
        connectBtn.setText("Подключиться");
        mainLayout.addView(connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listView.getAddress()==null) return;
                activity.settings.put("device", listView.getAddress());
                Log.d(LOG_TAG, "Discovered: " + listView.getAddress());
                activity.handlerDeviceSettings.sendEmptyMessage(0);
            }
        });
    }

    @Override
    public void view() {
    }


}

