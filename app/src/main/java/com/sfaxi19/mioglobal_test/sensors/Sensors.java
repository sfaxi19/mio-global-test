package com.sfaxi19.mioglobal_test.sensors;

import com.sfaxi19.mioglobal_test.ForegroundService;

/**
 * Created by sfaxi19 on 29.06.16.
 */
public class Sensors {

    public AccelerometerSensor accelerometer;
    public PulsometerSensor pulsometer;

    public Sensors(ForegroundService context,
                   String deviceAddress) {
        pulsometer = new PulsometerSensor(context, deviceAddress);
        pulsometer.run();
        accelerometer = new AccelerometerSensor(context);
    }

}
