package com.demoo.opentracing.restrictions;

/**
 * baggage 限定类
 */
public class Restriction {

    private boolean keyAllowed;
    private int maxValueLength;

    public static Restriction of(boolean keyAllowed, int maxValueLength) {
        return new Restriction(keyAllowed, maxValueLength);
    }

    public Restriction() {
    }

    public Restriction(boolean keyAllowed, int maxValueLength) {
        this.keyAllowed = keyAllowed;
        this.maxValueLength = maxValueLength;
    }

    public boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return value.length() < maxValueLength;
    }

    /**
     * 限定只返回满足要求的字符长度
     * @param value
     * @return
     */
    public String getRestrictionValue(String value) {
        if (value == null) {
            return value;
        }
        return value.length() <= maxValueLength? value : value.substring(0, maxValueLength);
    }

    public boolean isKeyAllowed() {
        return keyAllowed;
    }

    public int getMaxValueLength() {
        return maxValueLength;
    }

    public void setKeyAllowed(boolean keyAllowed) {
        this.keyAllowed = keyAllowed;
    }

    public void setMaxValueLength(int maxValueLength) {
        this.maxValueLength = maxValueLength;
    }
}
