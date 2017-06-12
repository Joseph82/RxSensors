package com.gvillani.rxsensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Cancellable;

public final class SensorEventFlowable {

    /**
     * Creates a Flowable that subscirbe to the source of data (sensor) and emits {@link RxSensorEvent}
     * items with a predefined frequency.
     *
     * @param sensorManager      A {@link SensorManager} object.
     * @param sensor             The {@link Sensor Sensor} to register to.
     * @param samplingPeriodUs   See {@link SensorManager#registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs)}
     * @param maxReportLatencyUs See {@link SensorManager#registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs)}
     * @return A Flowable that generates {@link RxSensorEvent} based on the provided parameters.
     */
    public static Flowable<RxSensorEvent> create(final SensorManager sensorManager, final Sensor sensor,
                                                 final int samplingPeriodUs, final int maxReportLatencyUs) {
        return Flowable.create(new FlowableOnSubscribe<RxSensorEvent>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<RxSensorEvent> e) throws Exception {
                final Listener listener = new Listener(e);
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        sensorManager.unregisterListener(listener);
                    }
                });

                if (Build.VERSION.SDK_INT < 19) {
                    sensorManager.registerListener(listener, sensor, samplingPeriodUs);
                } else {
                    sensorManager.registerListener(listener, sensor, samplingPeriodUs, maxReportLatencyUs);
                }
            }
        }, BackpressureStrategy.MISSING);
    }

    /**
     * See {@link #create(SensorManager, Sensor, int, int)}. It used a predefined value for
     * maxReportLatencyUs equal to zero. The events will then be delivered as soon as they will be
     * available.
     */
    public static Flowable<RxSensorEvent> create(SensorManager sensorManager, Sensor sensor,
                                                 int samplingPeriodUs) {
        return create(sensorManager, sensor, samplingPeriodUs, 0);
    }

    static final class Listener implements SensorEventListener {
        private final FlowableEmitter<RxSensorEvent> emitter;

        Listener(FlowableEmitter<RxSensorEvent> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            emitter.onNext(new RxSensorEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}