package com.gvillani.rxsensors;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;

public class RxSensorTransformer {

    private static final float DEFAULT_LPF_PARAMETER = 0.2F;

    /**
     * Apply a low pass filter (LPF) to the upcoming stream, to all the "dimensions" of the
     * values contained in the event. The filter should not be applied to discontinuous (mathematical
     * discontinuity) sources of data.
     *
     * @param parameter the alpha parameter for the LPF. It has to be between 0F and 1.0F.
     *                  Values not in this range may produce wrong result.
     * @return a FlowableTransformer that can be used for filtering data in the input stream
     */
    public static FlowableTransformer<? super RxSensorEvent, ? extends RxSensorEvent> lowPassFilter(final float parameter) {
        return new FlowableTransformer<RxSensorEvent, RxSensorEvent>() {
            @Override
            public Publisher<RxSensorEvent> apply(@NonNull Flowable<RxSensorEvent> upstream) {
                return upstream.scan(new BiFunction<RxSensorEvent, RxSensorEvent, RxSensorEvent>() {
                    @Override
                    public RxSensorEvent apply(@NonNull RxSensorEvent rxSensorEvent1, @NonNull RxSensorEvent rxSensorEvent2) throws Exception {
                        for (int i = 0; i < rxSensorEvent2.values.length; i++) {
                            rxSensorEvent2.values[i] = applyLpf(rxSensorEvent1.values[i], rxSensorEvent2.values[i], parameter);
                        }

                        return rxSensorEvent2;
                    }
                });
            }
        };
    }

    /**
     * Apply a low pass filter (LPF) to the upcoming stream, only to the first "dimension" of the
     * values contained in the event (x). The filter should not be applied to discontinuous
     * (mathematical discontinuity) sources of data.
     *
     * @param parameter the alpha parameter for the LPF. It has to be between 0F and 1.0F.
     *                  Values not in this range may produce wrong result.
     * @return a FlowableTransformer that can be used for filtering data in the input stream
     */
    public static FlowableTransformer<? super RxSensorEvent, ? extends RxSensorEvent> lowPassFilterX(final float parameter) {

        return new FlowableTransformer<RxSensorEvent, RxSensorEvent>() {
            @Override
            public Publisher<RxSensorEvent> apply(@NonNull Flowable<RxSensorEvent> upstream) {
                return upstream.scan(new BiFunction<RxSensorEvent, RxSensorEvent, RxSensorEvent>() {
                    @Override
                    public RxSensorEvent apply(@NonNull RxSensorEvent rxSensorEvent1, @NonNull RxSensorEvent rxSensorEvent2) throws Exception {
                        rxSensorEvent2.values[0] = applyLpf(rxSensorEvent1.values[0], rxSensorEvent2.values[0], parameter);

                        return rxSensorEvent2;
                    }
                });
            }
        };
    }

    /**
     * Apply a low pass filter (LPF) to the upcoming stream, only to the second "dimension" of the
     * values contained in the event (y). The filter should not be applied to discontinuous
     * (mathematical discontinuity) sources of data.
     *
     * @param parameter the alpha parameter for the LPF. It has to be between 0F and 1.0F.
     *                  Values not in this range may produce wrong result.
     * @return a FlowableTransformer that can be used for filtering data in the input stream
     */
    public static FlowableTransformer<? super RxSensorEvent, ? extends RxSensorEvent> lowPassFilterY(final float parameter) {
        return new FlowableTransformer<RxSensorEvent, RxSensorEvent>() {
            @Override
            public Publisher<RxSensorEvent> apply(@NonNull Flowable<RxSensorEvent> upstream) {
                return upstream.scan(new BiFunction<RxSensorEvent, RxSensorEvent, RxSensorEvent>() {
                    @Override
                    public RxSensorEvent apply(@NonNull RxSensorEvent rxSensorEvent1, @NonNull RxSensorEvent rxSensorEvent2) throws Exception {
                        rxSensorEvent2.values[1] = applyLpf(rxSensorEvent1.values[1], rxSensorEvent2.values[1], parameter);

                        return rxSensorEvent2;
                    }
                });
            }
        };
    }

    /**
     * Apply a low pass filter (LPF) to the upcoming stream, only to the third "dimension" of the
     * values contained in the event (z).
     *
     * @param parameter the alpha parameter for the LPF. It has to be between 0F and 1.0F.
     *                  Values not in this range may produce wrong result.
     * @return a FlowableTransformer that can be used for filtering data in the input stream
     */
    public static FlowableTransformer<? super RxSensorEvent, ? extends RxSensorEvent> lowPassFilterZ(final float parameter) {
        return new FlowableTransformer<RxSensorEvent, RxSensorEvent>() {
            @Override
            public Publisher<RxSensorEvent> apply(@NonNull Flowable<RxSensorEvent> upstream) {
                return upstream.scan(new BiFunction<RxSensorEvent, RxSensorEvent, RxSensorEvent>() {
                    @Override
                    public RxSensorEvent apply(@NonNull RxSensorEvent rxSensorEvent1, @NonNull RxSensorEvent rxSensorEvent2) throws Exception {
                        rxSensorEvent2.values[2] = applyLpf(rxSensorEvent1.values[2], rxSensorEvent2.values[2], parameter);

                        return rxSensorEvent2;
                    }
                });
            }
        };
    }

    /**
     * See {@link #lowPassFilter(float)}. It uses a predefinend low pass filter parameter
     */
    public static FlowableTransformer<? super RxSensorEvent, ? extends RxSensorEvent> lowPassFilter() {
        return lowPassFilter(DEFAULT_LPF_PARAMETER);
    }

    /**
     * See {@link #lowPassFilterX(float)}. It uses a predefinend low pass filter parameter
     */
    public static FlowableTransformer<? super RxSensorEvent, ? extends RxSensorEvent> lowPassFilterX() {
        return lowPassFilterX(DEFAULT_LPF_PARAMETER);
    }

    /**
     * See {@link #lowPassFilterY(float)}. It uses a predefinend low pass filter parameter
     */
    public static FlowableTransformer<? super RxSensorEvent, ? extends RxSensorEvent> lowPassFilterY() {
        return lowPassFilterY(DEFAULT_LPF_PARAMETER);
    }

    /**
     * See {@link #lowPassFilterZ(float)}. It uses a predefinend low pass filter parameter
     */
    public static FlowableTransformer<? super RxSensorEvent, ? extends RxSensorEvent> lowPassFilterZ() {
        return lowPassFilterZ(DEFAULT_LPF_PARAMETER);
    }

    private static float applyLpf(float oldValue, float currentValue, float parameter) {
        return oldValue + parameter * (currentValue - oldValue);
    }
}
