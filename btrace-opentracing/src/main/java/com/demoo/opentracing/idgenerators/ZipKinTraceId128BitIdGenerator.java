package com.demoo.opentracing.idgenerators;

import com.demoo.opentracing.utils.HexCodec;
import com.demoo.opentracing.utils.Java6CompatibleThreadLocalRandom;

/**
 * ZipKin TraceId 128Bit Id Generator
 * Created by freeway on 2017/9/23.
 */
public class ZipKinTraceId128BitIdGenerator implements IdGenerator {

    public String getId() {
        long low = 0, high = 0;
        while (low == 0) {
            low = Java6CompatibleThreadLocalRandom.current().nextLong();
        }
        while (high == 0) {
            high = Java6CompatibleThreadLocalRandom.current().nextLong();
        }
        return HexCodec.toLowerHex(high, low);
    }

}