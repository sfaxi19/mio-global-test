package com.sfaxi19.mioglobal_test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.sfaxi19.mioglobal_test.data_transfer.UDPClient;
import com.sfaxi19.mioglobal_test.sensors.PulsometerSensor;
import com.sfaxi19.mioglobal_test.sensors.Sensors;

public class ForegroundService extends Service {

	private final static String TEST_TAG ="Create and Destroy";
	private final static String LOG_TAG ="myLog";

	private final int NOTIFY_STATE_DISCONNECT = 0;
	private final int NOTIFY_STATE_CONNECTING = 1;
	private final int NOTIFY_STATE_CONNECTED = 2;
	private final int NOTIFY_STATE_DEFAULT = 3;

	private int notificationState = NOTIFY_STATE_DISCONNECT;
	ScheduledExecutorService serviceSendUDP;
	private ScheduledFuture<?> sf;
	private HashMap<String, String> settings;

	private Sensors sensors;
	private String path;
	private WifiManager.WifiLock wifiLock = null;
	public void onCreate() {
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TEST_TAG, "onStartCommand");
		if (intent!=null) {
			this.path = intent.getStringExtra("path");
		}

		setDefaultNotification();
		startBLEandSendUDP();

		return Service.START_REDELIVER_INTENT;
	}

	public void onDestroy() {
		Log.d(TEST_TAG, "onDestroyService");
		sensors.pulsometer.disconnect();
		sensors.pulsometer.close();
		if(wifiLock.isHeld()){
			wifiLock.release();
		}
		sfCancel(true);
		super.onDestroy();
	}
	
	public class LocalBinder extends Binder {
		public ForegroundService getService() {
            return ForegroundService.this;
        }
    }

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

	public void startBLEandSendUDP() {
		getDefaultSettings();
		Log.d(TEST_TAG, "settigs: " + settings);
		sensors = new Sensors(this, settings.get("device"));
		startUDPSend();
	}

	private void startUDPSend(){
		WifiManager wManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
		wifiLock = wManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");
		if(!wifiLock.isHeld()) {
			wifiLock.acquire();
		}
		if (serviceSendUDP==null) {
			serviceSendUDP = Executors.newScheduledThreadPool(1);
		}
		try {
			sf = serviceSendUDP.scheduleAtFixedRate(
					new UDPClient(ForegroundService.this, sensors, settings),
					0, Integer.decode(settings.get("period")), TimeUnit.SECONDS);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public String getHeartRate(){
		if ((sensors == null)||(sensors.pulsometer==null)) return "-2";
		setNotificationState(settings.get("device"), sensors.pulsometer.getConnectionState());
		return sensors.pulsometer.getHeartRate();
	}

	public int getConnectionState(){
		return sensors.pulsometer.getConnectionState();
	}

	public String getCoordinate(){
		return sensors.accelerometer.getCoordinate();
	}

	public boolean getDefaultConnect(){
		return sensors.pulsometer.isBadDefaultConnecting();
	}
	public void sfCancel(boolean b){
		sf.cancel(b);
	}

	private void getDefaultSettings() {
		File file = new File(path, "default_settings");
		ObjectInputStream ois = null;
		try {
			FileInputStream inFile = new FileInputStream(file);
			ois = new ObjectInputStream(inFile);
			settings = (HashMap<String,String>)ois.readObject();
			ois.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			settings = new HashMap<>();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if(!settings.containsKey("ip")){
			settings.put("ip","192.168.1.14");
		}
		if(!settings.containsKey("port")){
			settings.put("port", "5554");
		}
		if(!settings.containsKey("period")){
			settings.put("period","5");
		}
		if(!settings.containsKey("device")){
			settings.put("device","-1");
		}
	}

	public void saveDefaultSettings(){
		File file = new File(path, "default_settings");
		ObjectOutputStream oos = null;
		try {
			FileOutputStream outFile = new FileOutputStream(file);
			oos = new ObjectOutputStream(outFile);
			oos.writeObject(settings);
			oos.close();
		}  catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(LOG_TAG, "Saved:\n" + settings + "\n to " + path + "/default_settings");
	}

	public void updateNetworkSettings(HashMap<String,String> update){
		sfCancel(true);
		settings.putAll(update);
		startUDPSend();
		saveDefaultSettings();
	}

	public HashMap<String,String> getSettings(){
		return settings;
	}

	public void updateDeviceSettings(HashMap<String, String> update){
		sensors.pulsometer.disconnect();
		settings.putAll(update);
		sensors.pulsometer.setDeviceAddress(settings.get("device"));
		System.out.println(settings.get("device"));
		sensors.pulsometer.connect();
		saveDefaultSettings();
	}

	public void setNotificationState(String deviceAddress, int state){
		String ticker="ticker";
		String contentText = "content text";
		int mipmap=0;
		long[] vibrate={};
		Log.d(LOG_TAG, deviceAddress + " " + state);
		switch(state) {
			case PulsometerSensor.STATE_DISCONNECTED:
				if (notificationState == NOTIFY_STATE_DISCONNECT) return;
				notificationState = NOTIFY_STATE_DISCONNECT;
				ticker = "Подключение не активно";
				contentText = "Нет связи с устройством: " + deviceAddress;
				vibrate = new long[]{100,100,100,100,100,100};
				mipmap = R.mipmap.pulce_disconnect;
				break;
			case PulsometerSensor.STATE_CONNECTING:
				if (notificationState == NOTIFY_STATE_CONNECTING) return;
				notificationState = NOTIFY_STATE_CONNECTING;
				ticker = "Идет подключение...";
				contentText = "Подключение к " + deviceAddress;
				mipmap = R.mipmap.pulce_connecting;
				break;
			case PulsometerSensor.STATE_CONNECTED:
				if (notificationState == NOTIFY_STATE_CONNECTED) return;
				notificationState = NOTIFY_STATE_CONNECTED;
				ticker = "Подключено!";
				contentText = "Подключено к " + deviceAddress;
				vibrate = new long[]{400,400};
				mipmap = R.mipmap.pulce_connect;
				break;
		}
		Intent notificationIntent = new Intent(this, MainActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(this,
				0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Notification.Builder builder = new Notification.Builder(this);
		Resources res = this.getResources();
		Notification notification;
		builder.setContentIntent(contentIntent)
				.setSmallIcon(mipmap)
				.setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.pulce))
				.setTicker(ticker)
				.setWhen(System.currentTimeMillis())
				.setAutoCancel(true)
				.setContentTitle("MioGlobal_test")
				.setContentText(contentText)
				.setVibrate(vibrate);
		notification = builder.build();
		final NotificationManager notifyManager = (NotificationManager)getApplicationContext()
				.getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
		notifyManager.notify(1,notification);
	}

	public void setDefaultNotification(){
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this,
				0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		Notification.Builder builder = new Notification.Builder(this);
		Resources res = this.getResources();
		builder.setContentIntent(contentIntent)
				.setSmallIcon(R.mipmap.pulce)
				.setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.pulce))
				.setTicker("Ожидание подключения")
				.setWhen(System.currentTimeMillis())
				.setAutoCancel(true)
				.setContentTitle("MIO GLOBAL LINK")
				.setContentText("Нет подключенных устройств");
		Notification notification = builder.build();
		startForeground(1, notification);
	}

}