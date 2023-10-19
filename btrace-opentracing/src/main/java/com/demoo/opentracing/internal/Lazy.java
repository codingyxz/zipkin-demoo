/**
 * Copyright 2015-2016 The OpenZipkin Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.demoo.opentracing.internal;

import java.util.concurrent.TimeUnit;

/**
 * modify form zipkin
 */
public abstract class Lazy<T> {

    volatile T instance = null;

    /** Remembers the result, if the operation completed unexceptionally. */
    protected abstract T compute();

    /** Returns the same value, computing as necessary */
    public final T get() {
        T result = instance;
        if (result == null) {
            synchronized (this) {
                result = instance;
                if (result == null) {
                    instance = result = tryCompute();
                }
            }
        }
        return result;
    }

    /** How long to cache an exception computing a value */
    final long exceptionExpirationDuration = TimeUnit.SECONDS.toNanos(1);
    // the below fields are guarded by this, and visible due to writes inside a synchronized block
    RuntimeException lastException;
    long exceptionExpiration;

    T tryCompute() {
        // if last attempt was an exception, and we are within an cache interval, throw.
        if (lastException != null) {
            if (exceptionExpiration - System.nanoTime() <= 0) {
                lastException = null;
            } else {
                throw lastException;
            }
        }
        try {
            return compute();
        } catch (RuntimeException e) {
            // this attempt failed. Remember the exception so that we can throw it.
            lastException = e;
            exceptionExpiration = System.nanoTime() + exceptionExpirationDuration;
            throw e;
        }
    }

}
