package com.gvillani.rxsensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.gvillani.rxsensors.internal.Preconditions;

import io.reactivex.Flowable;

/**
 * Helper class, useful for creating source of data connected to sensors.
 */
public final class RxSensor {

    /**
     * Create a Flowable that emits {@link RxSensorEvent} items with a default sampling period
     * {@link SensorManager#SENSOR_DELAY_NORMAL} of the default sensor for the
     * provided sensorType.
     *
     * @param context    A Context object.
     * @param sensorType A Context object.
     * @return A Flowable object that emits {@link RxSensorEvent}.
     */
    public static Flowable<RxSensorEvent> sensorEvent(Context context, int sensorType) {
        return sensorEvent(context, sensorType, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Create a Flowable that emits {@link RxSensorEvent} items of the default sensor for the
     * provided sensorType and with the provided sampling rate.
     *
     * @param context          A Context object.
     * @param sensorType       A Context object.
     * @param samplingPeriodUs Sampling rate of the emitter. It is an indication for the system
     *                         about how fast the events should be emitted.
     * @return A Flowable object that emits {@link RxSensorEvent}.
     */
    public static Flowable<RxSensorEvent> sensorEvent(Context context, int sensorType, int samplingPeriodUs) {
        Preconditions.checkNotNull(context, "Context is null");
        SensorManager sensorManager = getSensorManager(context);
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        Preconditions.checkSensorExists(sensor);

        return SensorEventFlowable.create(sensorManager, sensor, samplingPeriodUs);
    }

    /**
     * Create a Flowable that emits {@link RxSensorEvent} items for the specified sensor and with
     * the provided sampling rate.
     *
     * @param context          A Context object.
     * @param sensor           The Sensor that we want to observe.
     * @param samplingPeriodUs Sampling rate of the emitter. It is an indication for the system
     *                         about how fast the events should be emitted.
     * @return A Flowable object that emits {@link RxSensorEvent}.
     */
    public static Flowable<RxSensorEvent> sensorEvent(Context context, Sensor sensor, int samplingPeriodUs) {
        Preconditions.checkNotNull(context, "Context is null");
        Preconditions.checkNotNull(sensor, "Sensor is null");
        SensorManager sensorManager = getSensorManager(context);
        return SensorEventFlowable.create(sensorManager, sensor, samplingPeriodUs);
    }

    /**
     * Create a Flowable that emits {@link RxAccuracyEvent} items, based on the specified sensor and
     * with the provided sampling rate, and eventually on the accuracy change events generated from
     * the observed sensor.
     *
     * @param context          A Context object.
     * @param sensor           The Sensor that we want to observe.
     * @param samplingPeriodUs Sampling rate of the emitter. It is an indication for the system
     *                         about how fast the events should be emitted.
     * @return A Flowable object that emits {@link RxAccuracyEvent}.
     */
    public static Flowable<RxAccuracyEvent> accuracyChangeEvent(Context context, Sensor sensor,
                                                                int samplingPeriodUs) {
        Preconditions.checkNotNull(context, "Context is null");
        SensorManager sensorManager = getSensorManager(context);
        Preconditions.checkNotNull(sensor, "Sensor is null");
        return AccuracyEventFlowable.create(sensorManager, sensor, samplingPeriodUs);
    }

    /**
     * Create a Flowable that emits {@link RxSensorEvent} items that represent the orientation
     * expressed in degrees for the three axis (x,y,z): Azimuth (angle around the z-axis),
     * Pitch (angle around the x-axis), Roll (angle around the y-axis), allowing the remap of the
     * coordinate, which means it rotates the rotation matrix, based on the provided parameters
     * (remappingCoordinateX and remappingCoordinateY).
     *
     * @param context              A Context object.
     * @param remappingCoordinateX Defines the axis of the new cooridinate system that coincide with the X axis of the
     *                             original coordinate system. See {@link SensorManager#remapCoordinateSystem(float[], int, int, float[])}
     *                             for more details
     * @param remappingCoordinateY Defines the axis of the new cooridinate system that coincide with the Y axis of the
     *                             original coordinate system.See {@link SensorManager#remapCoordinateSystem(float[], int, int, float[])}
     *                             for more details
     * @param samplingPeriodUs     Sampling rate of the emitter. It is an indication for the system
     *                             about how fast the events should be emitted.
     * @return A Flowable that emits {@link RxSensorEvent}
     */
    public static Flowable<RxSensorEvent> orientationEventWithRemap(Context context, int remappingCoordinateX,
                                                                    int remappingCoordinateY, int samplingPeriodUs) {
        Preconditions.checkNotNull(context, "Context is null");
        SensorManager sensorManager = getSensorManager(context);

        return new OrientationEventFlowable.Builder(sensorManager, samplingPeriodUs)
                .remapCoordinateSystem(remappingCoordinateX, remappingCoordinateY)
                .enableLowPassFilter()
                .build();
    }

    /**
     * See {@link #orientationEventWithRemap(Context, int, int, int)}. It uses a predefined sampling
     * period {@link SensorManager#SENSOR_DELAY_NORMAL}.
     */
    public static Flowable<RxSensorEvent> orientationEventWithRemap(Context context, int remappingCoordinateX,
                                                                    int remappingCoordinateY) {
        return orientationEventWithRemap(context, remappingCoordinateX, remappingCoordinateY,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Create a Flowable that emits {@link RxSensorEvent} items that represent the orientation
     * expressed in degrees for the three axis (x,y,z): Azimuth (angle around the z-axis),
     * Pitch (angle around the x-axis), Roll (angle around the y-axis).
     *
     * @param context          A Context object.
     * @param samplingPeriodUs Sampling rate of the emitter. It is an indication for the system
     *                         about how fast the events should be emitted.
     * @return A Flowable that emits {@link RxSensorEvent}
     */
    public static Flowable<RxSensorEvent> orientationEvent(Context context, int samplingPeriodUs) {
        Preconditions.checkNotNull(context, "Context is null");
        SensorManager sensorManager = getSensorManager(context);
        return new OrientationEventFlowable.Builder(sensorManager, samplingPeriodUs).build();
    }

    /**
     * See {@link #orientationEvent(Context, int)}. It uses a predefined sampling
     * period {@link SensorManager#SENSOR_DELAY_NORMAL}.
     */
    public static Flowable<RxSensorEvent> orientationEvent(Context context) {
        return orientationEvent(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private static SensorManager getSensorManager(Context context) {
        return (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }
}