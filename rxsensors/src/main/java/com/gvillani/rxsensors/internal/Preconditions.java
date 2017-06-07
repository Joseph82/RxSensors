package com.gvillani.rxsensors.internal;

import android.hardware.Sensor;
import android.support.annotation.RestrictTo;

import com.gvillani.rxsensors.exceptions.SensorNotFoundException;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

@RestrictTo(LIBRARY_GROUP)
public final class Preconditions {
    public static void checkNotNull(Object value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
    }

    public static void checkSensorExists(Sensor sensor) {
        if (sensor == null) {
            throw new SensorNotFoundException();
        }
    }

    private Preconditions() {
        throw new AssertionError("No instances.");
    }
}
