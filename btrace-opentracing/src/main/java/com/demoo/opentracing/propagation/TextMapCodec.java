package com.demoo.opentracing.propagation;


import com.demoo.opentracing.restrictions.BaggageRestrictionManager;
import io.opentracing.propagation.TextMap;

/**
 * text map codec
 * Created by freeway on 2017/9/23.
 */
public abstract class TextMapCodec implements Injector<TextMap>, Extractor<TextMap> {

    protected BaggageRestrictionManager baggageRestrictionManager;

    public void registerBaggageRestrictionManager(BaggageRestrictionManager baggageRestrictionManager) {
        this.baggageRestrictionManager = baggageRestrictionManager;
    }
}
