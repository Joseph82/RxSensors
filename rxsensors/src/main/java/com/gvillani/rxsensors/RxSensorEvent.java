package com.gvillani.rxsensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class RxSensorEvent {

    public float[] values;

    /**
     * The sensor that generated this event. See
     * {@link android.hardware.SensorManager SensorManager} for details.
     */
    public Sensor sensor;

    /**
     * The accuracy of this event. See {@link android.hardware.SensorManager
     * SensorManager} for details.
     */
    public int accuracy;

    /**
     * The time in nanosecond at which the event happened
     */
    public long timestamp;

    public RxSensorEvent(SensorEvent sensorEvent) {
        values = sensorEvent.values.clone();
        sensor = sensorEvent.sensor;
        accuracy = sensorEvent.accuracy;
        timestamp = sensorEvent.timestamp;
    }

    public RxSensorEvent(float[] values, Sensor sensor, int accuracy, long timestamp) {
        this.values = values.clone();
        this.sensor = sensor;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
    }

    public RxSensorEvent(RxSensorEvent sensorEvent) {
        values = sensorEvent.values.clone();
        sensor = sensorEvent.sensor;
        accuracy = sensorEvent.accuracy;
        timestamp = sensorEvent.timestamp;
    }

    public void setData(SensorEvent sensorEvent) {
        values = sensorEvent.values.clone();
        sensor = sensorEvent.sensor;
        accuracy = sensorEvent.accuracy;
        timestamp = sensorEvent.timestamp;
    }
}
