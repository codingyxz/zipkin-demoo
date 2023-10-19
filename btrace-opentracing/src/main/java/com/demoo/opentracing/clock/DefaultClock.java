package com.demoo.opentracing.clock;

/**
 * 默认的时钟
 * Created by freeway on 2017/9/16.
 */
public class DefaultClock implements Clock {

    private long currentTimeMicros;
    private long currentNanoTime;
    public DefaultClock() {
        currentTimeMicros = System.currentTimeMillis()*1000;
        currentNanoTime = System.nanoTime();
    }

    @Override
    public long currentTimeMicros() {
        return (System.nanoTime() - currentNanoTime)/1000 + currentTimeMicros;
    }

    @Override
    public long currentNanoTime() {
        return System.nanoTime();
    }
}
