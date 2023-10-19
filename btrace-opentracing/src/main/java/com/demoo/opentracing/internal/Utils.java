package com.demoo.opentracing.internal;

public class Utils {

    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * Copy of {@code com.google.common.base.Preconditions#checkArgument}.
     */
    public static void checkArgument(boolean expression,
                                     String errorMessageTemplate,
                                     Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, errorMessageArgs));
        }
    }
    /**
     * Copy of {@code com.google.common.base.Preconditions#checkNotNull}.
     */
    public static <T> T checkNotNull(T reference, String errorMessage) {
        if (reference == null) {
            // If either of these parameters is null, the right thing happens anyway
            throw new NullPointerException(errorMessage);
        }
        return reference;
    }

}
