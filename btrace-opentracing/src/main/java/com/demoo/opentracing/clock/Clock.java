package com.demoo.opentracing.clock;

/**
 * 时钟
 * Created by freeway on 2017/9/15.
 */
public interface Clock {

    /**
     * 当前的毫秒
     * @return
     */
    long currentTimeMicros();
    long currentNanoTime();

}
