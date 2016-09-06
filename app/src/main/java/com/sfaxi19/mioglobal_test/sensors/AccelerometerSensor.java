package com.sfaxi19.mioglobal_test.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

/**
 * Created by sfaxi19 on 29.06.16.
 */
public class AccelerometerSensor {

    private float X = 0.0f;
    private float Y = 0.0f;
    private float Z = 0.0f;
    private int cnt = 0;



    public Coordinate getAllCoordinate(){
        Coordinate coord = new Coordinate();
        if(cnt == 0) {
            coord.X = "0.0";
            coord.Y = "0.0";
            coord.Z = "0.0";
        }
        else {
            coord.X = String.format("%.1f", X/cnt);
            coord.Y = String.format("%.1f", Y/cnt);
            coord. Z = String.format("%.1f", Z/cnt);
        }

        X = 0.0f;
        Y = 0.0f;
        Z = 0.0f;
        cnt = 0;
        return coord;
    }

    public String getCoordinate() {
        String result;
        if(cnt == 0) {
            result = "0.0";
        }
        else {
            result = String.format("%.1f", (X + Y + Z)/cnt);
            X = 0.0f;
            Y = 0.0f;
            Z = 0.0f;
            cnt = 0;
        }
        return result;
    }

    public AccelerometerSensor(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        if(sensors.size() != 0) {
            sensorManager.registerListener(
                    sensorEventListener, sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x, y, z;
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
            X = X + x;
            Y = Y + y;
            Z = Z + z;
            cnt++;
        }
    };

}
