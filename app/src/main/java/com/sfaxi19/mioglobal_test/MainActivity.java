package com.sfaxi19.mioglobal_test;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.sfaxi19.mioglobal_test.sections.MainSection;
import com.sfaxi19.mioglobal_test.sections.SettingsSection;
import com.sfaxi19.mioglobal_test.view.SectionsPagerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private MyViewPager mViewPager;
    private final static String TEST_TAG ="Create and Destroy";
    private final static String LOG_TAG ="myLog";
    public MainSection mainSection;
    public SettingsSection settingsSection;

    public Thread threadHeartRate;
    public TextView nameTextView;
    public HashMap<String,String> settings;

    private boolean dialogDisplayed = false;
    private boolean dialogOff = false;
    private DiscovererBluetoothDevice discovererDevice;
    private AlertDialog.Builder dialog;

    private ForegroundService mService;
    public boolean mBound = false;

    @Override
    protected void onDestroy() {
        Log.d(TEST_TAG, "onDestroy main activity: " + this.hashCode());
        if(mBound) {
            unbindService(mConnection);
            mBound=false;
        }
        if(threadHeartRate!=null)threadHeartRate.interrupt();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(TEST_TAG, "onStop main activity: " + this.hashCode());
        if(mBound) {
            unbindService(mConnection);
            mBound=false;
        }
        super.onStop();
    }


    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        Log.d(TEST_TAG, "onRetain main activity: " + this.hashCode());
        mSectionsPagerAdapter.unlink();
        return mViewPager;
    }

    @Override
    protected void onStart() {
        dialogOff = false;
        if(isServiceRunning(ForegroundService.class)) {
            if(!mBound) {
                bindService(new Intent(this, ForegroundService.class), mConnection, Context.BIND_AUTO_CREATE);
            }
            hGetSettingsFromService.sendEmptyMessage(0);
        }
        super.onStart();
    }

    public void showDialog(){
        dialogDisplayed = true;
        dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Устройство не найдено");
        dialog.setMessage("Устройство по умолчанию не найдено.\n" +
                "Включите устройство и нажмине \"Обновить\". Или выберите новое устройство.\n");
        dialog.setPositiveButton("Обновить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyCallback dialogCallback = new MyCallback() {
                    @Override
                    public void viewDevice(String name, String address) {

                    }

                    @Override
                    public void finishDiscover(int devCount) {
                        dialogDisplayed = false;
                    }
                };
                discovererDevice = new DiscovererBluetoothDevice(MainActivity.this);
                discovererDevice.getDiscoveredDevices(dialogCallback);
            }
        });
        dialog.setNeutralButton("Выбрать устройство", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mViewPager.setCurrentItem(2);
            }
        });
        dialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogOff = true;
            }
        });
        dialog.show();
    }

    public void bluetoothEnable(){
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TEST_TAG, "onCreate main activity: " + this.hashCode());
        super.onCreate(null);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        settings = getSavedSettings();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bluetoothEnable();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.link(this);
        mViewPager = (MyViewPager) findViewById(R.id.container);
        //mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        //hGetSettingsFromService.sendEmptyMessage(0);
                        mViewPager.setActive(true);
                        break;
                    case 1:
                        mViewPager.setActive(false);
                        if(discovererDevice!=null) {
                            if(!discovererDevice.isScanning()) {
                                dialogDisplayed = false;
                            }
                        }else{
                            dialogDisplayed = false;
                        }
                        break;
                    case 2:
                        //hGetSettingsFromService.sendEmptyMessage(0);
                        mViewPager.setActive(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1, false);
    }

    private final int MENU_STOP_SERVICE = 1;
    private final int MENU_SETTINGS = 0;
    private final int MENU_DISCOVER = 2;
    private final int MENU_CLEAR_SETTINGS = 3;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        CharSequence stopServiceItem = "Остановить сервис";
        CharSequence settingsItem = "Настройки";
        CharSequence discoverItem = "Сканирование";
        menu.add(0, MENU_STOP_SERVICE, 0, stopServiceItem);
        menu.add(0, MENU_SETTINGS, 0, settingsItem);
        menu.add(0, MENU_DISCOVER, 0, discoverItem);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem()!=1){
            mViewPager.setCurrentItem(1);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case MENU_STOP_SERVICE:
                Log.d(TEST_TAG,"Stop service from " + this.hashCode());
                stopService(new Intent(this, ForegroundService.class));
                if(mBound){
                    unbindService(mConnection);
                    mBound = false;
                }
                mainSection.stopButton.setText("Старт");
                break;
            case MENU_SETTINGS:
                mViewPager.setCurrentItem(0,true);
                break;
            case MENU_DISCOVER:
                mViewPager.setCurrentItem(2,true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private HashMap<String, String> getSavedSettings() {
        File file = new File(this.getFilesDir(), "default_settings");
        ObjectInputStream ois = null;
        HashMap<String, String> savedSettings;
        try {
            FileInputStream inFile = new FileInputStream(file);
            ois = new ObjectInputStream(inFile);
            savedSettings = (HashMap<String, String>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            savedSettings = new HashMap<>();
        }
        return savedSettings;
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public String getHeartRateFromService(){
        if(!mBound) return "";
        return mService.getHeartRate();
    }

    public void startService(){
        Intent intent = new Intent(this, ForegroundService.class);
        if(!isServiceRunning(ForegroundService.class)){
            if(!settings.containsKey("port")) {
                settings.put("period", "5");
            }
            if(!settings.containsKey("ip")) {
                settings.put("ip", "192.168.1.14");
            }
            if(!settings.containsKey("port")) {
                settings.put("port", "5554");
            }
            if(!settings.containsKey("name")){
                settings.put("name", "4");
            }
            Log.d(LOG_TAG, "Start service with settings: " + settings);
            startService(intent.putExtra("settings", settings.toString()));
        }
        if(!mBound) {
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ForegroundService.LocalBinder binder = (ForegroundService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public void saveNetworkSettings() {
        if (settings == null) return;
        String path = getFilesDir().toString();
        File file = new File(path, "default_settings");
        HashMap<String, String> lastSettings = getSavedSettings();
        try {


            lastSettings.put("ip", settings.get("ip"));
            lastSettings.put("port", settings.get("port"));
            lastSettings.put("period", settings.get("period"));
            lastSettings.put("name", settings.get("name"));
            ObjectOutputStream oos = null;

            FileOutputStream outFile = new FileOutputStream(file);
            oos = new ObjectOutputStream(outFile);
            oos.writeObject(lastSettings);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "Saved:\n" + lastSettings + "\n to " + path + "/default_settings");
    }

    public void saveDeviceSettings() {
        if (settings == null) return;
        String path = getFilesDir().toString();
        File file = new File(path, "default_settings");
        HashMap<String, String> lastSettings = getSavedSettings();
        try {


            lastSettings.put("device", settings.get("device"));
            ObjectOutputStream oos = null;
            FileOutputStream outFile = new FileOutputStream(file);
            oos = new ObjectOutputStream(outFile);
            oos.writeObject(lastSettings);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "Saved:\n" + lastSettings + "\n to " + path + "/default_settings");
    }

    public Handler hUpdateNetworkSettings = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(!mBound) return;
            mService.updateNetworkSettings(settings);
        }
    };

    public Handler hUpdateDeviceSettings = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(!mBound) return;
            System.out.println(settings.get("device"));
            mService.updateDeviceSettings(settings);
        }
    };

    public Handler hGetSettingsFromService = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(!mBound) return;
            settings = mService.getSettings();
        }
    };

    public Handler hCheckConnect = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(mService.getConnectionState()==2) dialogOff=true;
            if(dialogOff) return;
            if(mService.getDefaultConnect()){
                if(!dialogDisplayed){
                    dialogDisplayed = true;
                    showDialog();
                }
            }
        }
    };
}
