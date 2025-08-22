package com.demoo.opentracing.samplers;

/**
 * 采样器
 */
public abstract class Sampler {

    public abstract boolean isSampled(String traceId);

    public static Sampler create(float rate) {
        return CountingSampler.create(rate);
    }

    public static final Sampler ALWAYS_SAMPLE = new Sampler() {
        @Override
        public boolean isSampled(String traceId) {
            return true;
        }

        @Override
        public String toString() {
            return "AlwaysSample";
        }
    };

    public static final Sampler NEVER_SAMPLE = new Sampler() {
        @Override
        public boolean isSampled(String traceId) {
            return false;
        }

        @Override
        public String toString() {
            return "NeverSample";
        }
    };

}
