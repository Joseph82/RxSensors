package com.gvillani.rxsensors;

import android.hardware.Sensor;

public class RxAccuracyEvent {
    final Sensor sensor;

    final int accuracy;

    public RxAccuracyEvent(Sensor sensor, int accuracy) {
        this.sensor = sensor;
        this.accuracy = accuracy;
    }
}
