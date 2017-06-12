package com.gvillani.rxsensors;

import android.hardware.SensorEvent;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiPredicate;
import io.reactivex.functions.Predicate;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;

public final class RxSensorFilter {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SENSOR_STATUS_ACCURACY_HIGH, SENSOR_STATUS_ACCURACY_MEDIUM, SENSOR_STATUS_ACCURACY_LOW})
    public @interface Accuracy {
    }

    /**
     * It allows to define a filter based on the requested accuracy. Data will be returned if the
     * accuracy of the event received is greater or equal to the minAccuracy.
     *
     * @param minAccuracy minimum accuracy requested for the event for not being filter.
     * @return Predicate useful for filtering data based on the accuracy
     */
    public static Predicate<RxSensorEvent> minAccuracy(@Accuracy final int minAccuracy) {
        return new Predicate<RxSensorEvent>() {
            @Override
            public boolean test(@NonNull RxSensorEvent sensorEvent) throws Exception {
                switch (minAccuracy) {
                    case SENSOR_STATUS_ACCURACY_HIGH:
                        if (sensorEvent.accuracy == SENSOR_STATUS_ACCURACY_HIGH) {
                            return true;
                        }

                        return false;
                    case SENSOR_STATUS_ACCURACY_MEDIUM:
                        if (sensorEvent.accuracy == SENSOR_STATUS_ACCURACY_HIGH
                                || sensorEvent.accuracy == SENSOR_STATUS_ACCURACY_MEDIUM) {
                            return true;
                        }

                        return false;
                    case SENSOR_STATUS_ACCURACY_LOW:
                        if (sensorEvent.accuracy == SENSOR_STATUS_ACCURACY_HIGH
                                || sensorEvent.accuracy == SENSOR_STATUS_ACCURACY_MEDIUM
                                || sensorEvent.accuracy == SENSOR_STATUS_ACCURACY_LOW) {
                            return true;
                        }

                        return false;
                    default:
                        return false;
                }
            }
        };
    }

    /**
     * Returns unique events, based on a comparison between all the dimensions of the event's value.
     *
     * @return a BiPredicate useful for filtering
     */
    public static BiPredicate<RxSensorEvent, RxSensorEvent> uniqueEventValues() {
        return new BiPredicate<RxSensorEvent, RxSensorEvent>() {
            @Override
            public boolean test(@NonNull RxSensorEvent event1, @NonNull RxSensorEvent event2) throws Exception {
                return compareEventValues(event1.values, event2.values);
            }
        };
    }

    /**
     * See {@link #uniqueEventValues()}. It just compare the first dimension (x) of the event's
     * value.
     */
    public static BiPredicate<RxSensorEvent, RxSensorEvent> uniqueEventValuesX() {
        return new BiPredicate<RxSensorEvent, RxSensorEvent>() {
            @Override
            public boolean test(@NonNull RxSensorEvent event1, @NonNull RxSensorEvent event2) throws Exception {
                return compareValues(event1.values[0], event2.values[0]);
            }
        };
    }

    /**
     * See {@link #uniqueEventValues()}. It just compare the second dimension (y) of the event's
     * value.
     */
    public static BiPredicate<RxSensorEvent, RxSensorEvent> uniqueEventValuesY() {
        return new BiPredicate<RxSensorEvent, RxSensorEvent>() {
            @Override
            public boolean test(@NonNull RxSensorEvent event1, @NonNull RxSensorEvent event2) throws Exception {
                return compareValues(event1.values[1], event2.values[1]);
            }
        };
    }

    /**
     * See {@link #uniqueEventValues()}. It just compare the third dimension (z) of the event's
     * value.
     */
    public static BiPredicate<RxSensorEvent, RxSensorEvent> uniqueEventValuesZ() {
        return new BiPredicate<RxSensorEvent, RxSensorEvent>() {
            @Override
            public boolean test(@NonNull RxSensorEvent event1, @NonNull RxSensorEvent event2) throws Exception {
                return compareValues(event1.values[2], event2.values[2]);
            }
        };
    }

    private static boolean compareValues(float value1, float value2) {
        return Float.floatToIntBits(value1) == Float.floatToIntBits(value2);
    }

    /**
     * Compares two items based on all the dimensions of the events, and the accuracy.
     *
     * @return a Bipredicate that filter items that don't have all the values equalss withe the same
     * accuracy.
     */
    public static BiPredicate<SensorEvent, SensorEvent> uniqueEvent() {
        return new BiPredicate<SensorEvent, SensorEvent>() {
            @Override
            public boolean test(@NonNull SensorEvent event1, @NonNull SensorEvent event2) throws Exception {
                return compareEventValues(event1.values, event2.values)
                        || compareEventAccuracy(event1.accuracy, event2.accuracy);
            }
        };
    }

    private static boolean compareEventAccuracy(int accuracy1, int accuracy2) {
        return accuracy1 != accuracy2;
    }

    private static boolean compareEventValues(float[] values1, float[] values2) {
        boolean areEqual = true;

        for (int i = 0; i < values1.length; i++) {
            if (Float.floatToIntBits(values1[i]) != Float.floatToIntBits(values2[i])) {
                areEqual = false;
                break;
            }
        }

        return areEqual;
    }
}
