package com.demoo.opentracing.utils;

import org.jvnet.animal_sniffer.IgnoreJRERequirement;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class Java6CompatibleThreadLocalRandom {

    static boolean threadLocalRandomPresent = true;

    private static final String THREAD_LOCAL_RANDOM_CLASS_NAME =
            "java.util.concurrent.ThreadLocalRandom";

    static {
        try {
            Class.forName(THREAD_LOCAL_RANDOM_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            threadLocalRandomPresent = false;
        }
    }

    private static final ThreadLocal<Random> threadLocal =
            new ThreadLocal<Random>() {
                @Override
                protected Random initialValue() {
                    return new Random();
                }
            };

    /**
     * Calls {@link ThreadLocalRandom#current()}, if this class is present (if you are using Java 7).
     * Otherwise uses a Java 6 compatible fallback.
     *
     * @return the current thread's {@link Random}
     */
    public static Random current() {
        if (threadLocalRandomPresent) {
            return ThreadLocalRandomAccessor.getCurrentThreadLocalRandom();
        } else {
            return threadLocal.get();
        }
    }

    /**
     * This class prevents that {@link ThreadLocalRandom} gets loaded unless
     * {@link #getCurrentThreadLocalRandom()} is called
     */
    private static class ThreadLocalRandomAccessor {
        @IgnoreJRERequirement
        private static Random getCurrentThreadLocalRandom() {
            return ThreadLocalRandom.current();
        }
    }

    private Java6CompatibleThreadLocalRandom() {
    }
}
