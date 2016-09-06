package com.sfaxi19.mioglobal_test.sensors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.sfaxi19.mioglobal_test.DiscovererBluetoothDevice;
import com.sfaxi19.mioglobal_test.ForegroundService;
import java.util.UUID;

public class PulsometerSensor {

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String           deviceAddress;
    private BluetoothGatt    bluetoothGatt;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private int mConnectionState = STATE_DISCONNECTED;

    private final static String ACTION_GATT_CONNECTED =
            "ACTION_GATT_CONNECTED";
    private final static String ACTION_GATT_DISCONNECTED =
            "ACTION_GATT_DISCONNECTED";
    private final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED";
    private final static String ACTION_DATA_AVAILABLE =
            "ACTION_DATA_AVAILABLE";

    private final UUID UUID_HEART_RATE_SERVICE
            = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    private final UUID UUID_HEART_RATE_MEASUREMENT
            = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    private final UUID UUID_CLIENT_CHARACTERISTIC_CONFIG
            = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private String mHeartRate;

    private final static String LOG_TAG = "myLogs";
    private final static String TEST_TAG ="Create and Destroy";
    private final static String RESULT_TAG ="Result Log";

    private boolean wasConnecting = false;
    private ForegroundService context;
    private PowerManager.WakeLock wakeLock;
    DiscovererBluetoothDevice discovererDevice;
    private boolean badDefault = false;


    public String getHeartRate() {
        if(mHeartRate!=null) {
            return mHeartRate;
        }
        else return "-0";
    }

    public PulsometerSensor(ForegroundService context, String deviceAddress) {
        this.context = context;
        this.deviceAddress = deviceAddress;
    }

    public void setDeviceAddress(String address){
        this.deviceAddress = address;
    }
    public void run() {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyWakeLock");

        if(!wakeLock.isHeld()){
            wakeLock.acquire();
        }

        if (initialize()) {
            connect();
        }
    }

    public int getConnectionState(){
        return mConnectionState;
    }

    public String getDeviceAddress(){
        return deviceAddress;
    }

    public boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.d(LOG_TAG, "Serv. Unable to initialize BluetoothManager.");
                return false;
            }
        }
        Log.d(LOG_TAG, "getting the bluetooth Manager - true");

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.d(LOG_TAG, "Serv. Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if(deviceAddress.equals("-1")) return false;
        discovererDevice = new DiscovererBluetoothDevice(context);
        discovererDevice.getDiscoveredDevices(null);

        return true;
    }

    public boolean connect() {

        if (bluetoothAdapter == null || deviceAddress == null || deviceAddress.equals("-1")) {
            Log.d(LOG_TAG, " connect Serv. BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if (wasConnecting && bluetoothGatt != null) {
            Log.d(LOG_TAG, " connect Serv. Trying to use an existing bluetoothGatt for connection.");
            if (bluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        Log.d(LOG_TAG, "device: " + device.getName() + "  " + device.getAddress());
        if ((device==null)||(device.getName() == null)) {
            Log.d(LOG_TAG, "connect Serv. Device not found.  Unable to connect.");
            if(badDefault){
                mConnectionState = STATE_DISCONNECTED;
            }
            if(!discovererDevice.isScanning()) {
                badDefault = true;
            }
            return false;
        }
        bluetoothGatt = device.connectGatt(context, false, mGattCallback);
        Log.d(LOG_TAG, "connect Serv. Trying to create a new connection.");

        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public boolean isBadDefaultConnecting(){
        return badDefault;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(status!=0){
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.d(LOG_TAG, "Serv. Connected to GATT server.");
                Log.d(LOG_TAG, "Attempting to start service discovery:" +
                        bluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.d(LOG_TAG, "Serv. Disconnected from GATT server.");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    disconnect();
                    context.sfCancel(true);
                    context.stopSelf();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.d(LOG_TAG, "Serv.  onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(LOG_TAG, "---------onCharacteristicRead--------");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(LOG_TAG, "onCharacteristicRead when status == BluetoothGatt.GATT_SUCCESS");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(LOG_TAG, "onCharacteristicChanged");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        Log.d(LOG_TAG, "broadcastUpdate action: "+ action);
        if (ACTION_GATT_CONNECTED.equals(action)) {
            wasConnecting = true;
        } else
        if (ACTION_GATT_DISCONNECTED.equals(action)) {
            mHeartRate = "-0";
            close();
        } else
        if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
            Log.d(LOG_TAG, "Read service and characteristic");
            BluetoothGattCharacteristic characteristic = getGattCharacteristic();
            if (characteristic != null) {
                final int charaProp = characteristic.getProperties();
                if ((BluetoothGattCharacteristic.PROPERTY_READ | charaProp) > 0) {
                    if (bluetoothGattCharacteristic != null) {
                        setCharacteristicNotification(bluetoothGattCharacteristic, false);
                        bluetoothGattCharacteristic = null;
                    }
                    Log.d(LOG_TAG, "Set characteristic read");
                    readCharacteristic(characteristic);
                }
                if ((BluetoothGattCharacteristic.PROPERTY_NOTIFY | charaProp) > 0) {
                    bluetoothGattCharacteristic = characteristic;
                    Log.d(LOG_TAG, "Read characteristic when changing");
                    setCharacteristicNotification(characteristic, true);
                }
            }
        }
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic){
        Log.d(LOG_TAG, "Recoding characteristic");
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(LOG_TAG, "Serv. Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(LOG_TAG, "Serv. Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(RESULT_TAG, String.format("Received heart rate: %d", heartRate));
            mHeartRate = String.valueOf(heartRate);
        }
    }

    public void disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.d(LOG_TAG, "Serv. BluetoothAdapter not initialized");
            return;
        }
        Log.d(LOG_TAG, "Serv. Bluetooth Disconnect");
        bluetoothGatt.disconnect();
    }


    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
        if(wakeLock.isHeld()){
            wakeLock.release();
        }
        Log.d(LOG_TAG, "Serv. Bluetooth close");
    }

    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.d(LOG_TAG, "Serv. BluetoothAdapter not initialized");
            return;
        }
        Log.d(LOG_TAG, "ReadCharacteristic BluetoothGattCharacteristic ");
        bluetoothGatt.readCharacteristic(characteristic);
    }

    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.d(LOG_TAG, "Serv. BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        Log.d(LOG_TAG, "setCharacteristicNotification");

        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID_CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    private BluetoothGattCharacteristic getGattCharacteristic() {
        if (bluetoothGatt == null) return null;
        Log.d(LOG_TAG, "getSupportedGattServices");
        return bluetoothGatt
                .getService(UUID_HEART_RATE_SERVICE)
                .getCharacteristic(UUID_HEART_RATE_MEASUREMENT);
    }

}