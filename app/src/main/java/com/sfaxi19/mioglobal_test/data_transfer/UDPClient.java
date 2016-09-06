package com.sfaxi19.mioglobal_test.data_transfer;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.sfaxi19.mioglobal_test.ForegroundService;
import com.sfaxi19.mioglobal_test.sensors.Coordinate;
import com.sfaxi19.mioglobal_test.sensors.PulsometerSensor;
import com.sfaxi19.mioglobal_test.sensors.Sensors;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by sfaxi19 on 29.06.16.
 */
public class UDPClient implements Runnable {

    private final static String TEST_TAG ="Creat and Destroy";
    private final static String UDP_TAG ="UDP send";

    public static WifiManager wManager;

    private DatagramSocket clientSocket;
    private InetAddress serverAddress;
    private int serverPort;
    private int packetType;
    private int RSSI;
    private StringBuffer st = new StringBuffer();
    private Locale local = new Locale("ru","RU");
    private DateFormat df = DateFormat.getTimeInstance(DateFormat.DEFAULT, local);
    private Sensors sensors;

    public UDPClient(ForegroundService context, Sensors sensors, HashMap<String,String> settings)
            throws UnknownHostException, SocketException {
        this.sensors = sensors;
        if(settings.get("ip").equals("-1")||(settings.get("port").equals("-1"))){
            return;
        }
        serverPort = Integer.decode(settings.get("port"));
        serverAddress = InetAddress.getByName(settings.get("ip"));
        packetType = Integer.decode(settings.get("packet"));

        clientSocket = new DatagramSocket();
        wManager =(WifiManager) context.getSystemService(Context.WIFI_SERVICE);

    }

    public void run() {

        try {
            if (sensors.pulsometer.getConnectionState() == PulsometerSensor.STATE_DISCONNECTED) {
                sensors.pulsometer.connect();
            }
            String macAP;
            if (wManager != null) {
                WifiInfo wifiInfo = wManager.getConnectionInfo();
                macAP = wifiInfo.getBSSID();
                RSSI = wifiInfo.getRssi();
            } else {
                return;
                //macAP = "00:00:00:00:00:00";
            }
            if(packetType==0) {
                Date currentDate = new Date();
                st.append(sensors.pulsometer.getHeartRate());
                st.append(" ");st.append(String.valueOf(RSSI));
                st.append(" ");st.append(macAP);
                Coordinate accel = sensors.accelerometer.getAllCoordinate();
                st.append(" ");st.append(accel.X);
                st.append(" ");st.append(accel.Y);
                st.append(" ");st.append(accel.Z);
                st.append(" ");st.append(df.format(currentDate));


            }else {
                st.append("0")
                        .append(" ").append(sensors.pulsometer.getHeartRate())
                        .append(" ").append(sensors.accelerometer.getCoordinate())
                        .append(" ").append(macAP);
            }

            byte[] message = st.toString().getBytes();
            DatagramPacket clientPacket = new DatagramPacket(
                    message, message.length, serverAddress, serverPort);
            Log.d(UDP_TAG, st.toString() + " to " + serverAddress + ":" + serverPort);

            st.delete(0, st.length());
            clientSocket.send(clientPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
