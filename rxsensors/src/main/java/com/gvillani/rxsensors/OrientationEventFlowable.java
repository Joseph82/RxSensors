package com.gvillani.rxsensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;

public final class OrientationEventFlowable {

    private final SensorManager sensorManager;

    private final Sensor sensorRotationVector;

    private final int samplingPeriodUs;

    private final int maxReportLatencyUs;

    private int remappingCoordinateX = SensorManager.AXIS_X;

    private int remappingCoordinateY = SensorManager.AXIS_Y;

    private boolean enableLowPassFilter;

    private float lpfAlpha;

    private boolean isRemapping;

    private OrientationEventFlowable(Builder builder) {
        this.sensorManager = builder.sensorManager;
        this.sensorRotationVector = builder.sensorRotationVector;
        this.samplingPeriodUs = builder.samplingPeriodUs;
        this.maxReportLatencyUs = builder.maxReportLatencyUs;
        this.isRemapping = builder.isRemapping;
        this.remappingCoordinateX = builder.remappingCoordinateX;
        this.remappingCoordinateY = builder.remappingCoordinateY;
        this.enableLowPassFilter = builder.enableLowPassFilter;
        this.lpfAlpha = builder.lpfAlpha;
    }

    public static class Builder {
        private static final float DEFAULT_LPF_PARAMETER = 0.25F;

        private final SensorManager sensorManager;

        private final int samplingPeriodUs;

        private final Sensor sensorRotationVector;

        private int maxReportLatencyUs;

        private int remappingCoordinateX = SensorManager.AXIS_X;

        private int remappingCoordinateY = SensorManager.AXIS_Y;

        private boolean isRemapping;

        private boolean enableLowPassFilter;

        private float lpfAlpha = DEFAULT_LPF_PARAMETER;

        public Builder(SensorManager sensorManager, int samplingPeriodUs) {
            this.sensorManager = sensorManager;
            this.samplingPeriodUs = samplingPeriodUs;
            this.sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        public Builder remapCoordinateSystem(int x, int y) {
            this.remappingCoordinateX = x;
            this.remappingCoordinateY = y;
            this.isRemapping = true;
            return this;
        }

        public Builder setMaxReportLatencyUs(int maxReportLatencyUs) {
            this.maxReportLatencyUs = maxReportLatencyUs;
            return this;
        }

        public Builder enableLowPassFilter() {
            enableLowPassFilter = true;
            return this;
        }

        public Builder enableLowPassFilter(float alpha) {
            enableLowPassFilter = true;
            lpfAlpha = alpha;
            return this;
        }

        public Flowable<RxSensorEvent> build() {
            OrientationEventFlowable o = new OrientationEventFlowable(this);
            return Flowable.create(e -> {
                Listener listener = o.isRemapping ? new Listener(e, o.enableLowPassFilter, o.lpfAlpha,
                        o.remappingCoordinateX, o.remappingCoordinateY) :
                        new Listener(e, o.enableLowPassFilter, o.lpfAlpha);
                e.setCancellable(() -> o.sensorManager.unregisterListener(listener));
                if (Build.VERSION.SDK_INT < 19) {
                    o.sensorManager.registerListener(listener, o.sensorRotationVector, o.samplingPeriodUs);
                } else {
                    o.sensorManager.registerListener(listener, o.sensorRotationVector, o.samplingPeriodUs, o.maxReportLatencyUs);
                }
            }, BackpressureStrategy.MISSING);
        }
    }

    OrientationEventFlowable(SensorManager sensorManager, int samplingPeriodUs) {
        this(sensorManager, samplingPeriodUs, 0);
    }

    OrientationEventFlowable(SensorManager sensorManager, int samplingPeriodUs,
                             int maxReportLatencyUs) {
        this.sensorManager = sensorManager;
        this.samplingPeriodUs = samplingPeriodUs;
        this.maxReportLatencyUs = maxReportLatencyUs;
        this.sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    static final class Listener implements SensorEventListener {
        private final FlowableEmitter<RxSensorEvent> emitter;

        private final int remappingCoordinateX;

        private final int remappingCoordinateY;

        private final boolean isRemapping;

        private final boolean isLpfEnabled;

        private final float lpfAlpha;

        private float[] rotationMatrix = new float[16];

        private float[] rotationVector = new float[16];

        private float[] orientationValues = new float[3];

        Listener(FlowableEmitter<RxSensorEvent> emitter, boolean lpfEnabled, float lpfAlpha,
                 int remappingCoordinateX, int remappingCoordinateY) {
            this.emitter = emitter;
            this.remappingCoordinateX = remappingCoordinateX;
            this.remappingCoordinateY = remappingCoordinateY;
            this.isRemapping = true;
            this.isLpfEnabled = lpfEnabled;
            this.lpfAlpha = lpfAlpha;
        }

        Listener(FlowableEmitter<RxSensorEvent> emitter, boolean lpfEnabled, float lpfAlpha) {
            this.emitter = emitter;
            this.remappingCoordinateX = 0;
            this.remappingCoordinateY = 0;
            this.isRemapping = false;
            this.isLpfEnabled = lpfEnabled;
            this.lpfAlpha = lpfAlpha;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ROTATION_VECTOR:

                    if (isLpfEnabled) {
                        rotationVector = lowPass(event.values.clone(), rotationVector, lpfAlpha);
                    }

                    SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);
                    if (isRemapping) {
                        SensorManager.remapCoordinateSystem(rotationMatrix, remappingCoordinateX, remappingCoordinateY, rotationMatrix);
                    }
                    SensorManager.getOrientation(rotationMatrix, orientationValues);

                    orientationValues[0] = (float) Math.toDegrees(orientationValues[0]);
                    orientationValues[1] = (float) Math.toDegrees(orientationValues[1]);
                    orientationValues[2] = (float) Math.toDegrees(orientationValues[2]);

                    RxSensorEvent rxSensorEvent = new RxSensorEvent(orientationValues, event.sensor, event.accuracy, event.timestamp);

                    emitter.onNext(rxSensorEvent);

                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private static float[] lowPass(float[] input, float[] output, float alpha) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] * alpha + (1F - alpha) * input[i];
        }
        return output;
    }
}
