package com.demoo.opentracing.idgenerators;


import com.demoo.opentracing.utils.HexCodec;
import com.demoo.opentracing.utils.Java6CompatibleThreadLocalRandom;

/**
 * zip kin 64 bit id
 * Created by freeway on 2017/9/23.
 */
public class ZipKin64bitIdGenerator implements IdGenerator {

    @Override
    public String getId() {
        long val = 0;
        while (val == 0) {
            val = Java6CompatibleThreadLocalRandom.current().nextLong();
        }
        return HexCodec.toLowerHex(val);
    }
}
