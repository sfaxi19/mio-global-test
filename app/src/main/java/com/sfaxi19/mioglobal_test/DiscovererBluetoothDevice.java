package com.sfaxi19.mioglobal_test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sfaxi19 on 05.07.16.
 */
public class DiscovererBluetoothDevice {

    BroadcastReceiver discoverReceiver;
    private List<BluetoothDevice> discoveredDevices;
    private BluetoothAdapter bluetooth;
    private Context context;
    private boolean scanning = false;

    public DiscovererBluetoothDevice(Context context) {
        this.context = context;
        discoveredDevices = new ArrayList<>();
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        /*if (!bluetooth.isEnabled()) {
            bluetooth.enable();
        }*/
    }

    public boolean isScanning(){return scanning;}

    public List<BluetoothDevice> getDiscoveredDevices(final MyCallback myCallback) {
        scanning = true;
        Toast.makeText(context, "Discovery start", Toast.LENGTH_LONG).show();
        discoveredDevices.clear();
        discoverReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(!discoveredDevices.contains(device)) {
                        discoveredDevices.add(device);
                        if(myCallback!=null) myCallback.viewDevice(device.getName(),device.getAddress());
                    }
                }
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //Toast.makeText(context, "Discovery FINISHED", Toast.LENGTH_LONG).show();
                    context.unregisterReceiver(discoverReceiver);
                    if(discoveredDevices.size()<=0){
                        //Toast.makeText(context, "Devices not found", Toast.LENGTH_LONG).show();
                    }
                    scanning = false;
                    if(myCallback!=null) myCallback.finishDiscover(discoveredDevices.size());
                }
            }


        };
        try {
            context.registerReceiver(discoverReceiver,
                    new IntentFilter(BluetoothDevice.ACTION_FOUND));
            context.registerReceiver(discoverReceiver,
                    new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        }catch(Exception ex){}
        bluetooth.startDiscovery();
        return discoveredDevices;
    }

    public boolean isDeviceExist(String deviceAddress){
        if(bluetooth.getRemoteDevice(deviceAddress).getName()==null){
            return false;
        }else {return true;}


    }
}
