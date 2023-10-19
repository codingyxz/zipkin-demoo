package com.demoo.opentracing;

import com.demoo.opentracing.samplers.CountingSampler;
import com.demoo.opentracing.samplers.Sampler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhxy
 * @Date 2021/6/28 8:25 下午
 */
public class BtraceConfig {


    private Boolean enabled = true;

    private String sampleRate = "1";
    /**
     * userService.getById,userService.query
     */
    private String traceMethods = "";
    /**
     * userService.getById:123,userService.query:345
     */
    private String sceneBaggage = "";

    public Boolean isEnabled() {
        if (enabled == null) {
            return true;
        }
        return enabled;
    }

    public BtraceConfig setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getSampleRate() {
        return sampleRate;
    }

    public Sampler getSampler() {
        try {
            if (sampleRate != null && sampleRate.length() > 0) {
                float rate = Float.parseFloat(sampleRate);
                if (rate > 0 && rate <= 1) {
                    return CountingSampler.create(rate);
                }
            }
        } catch (Throwable throwable) {

        }
        return Sampler.ALWAYS_SAMPLE;
    }

    public BtraceConfig setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public String getTraceMethods() {
        return this.traceMethods;
    }

    public String[] getTraceMethodsArray() {
        if (traceMethods != null && traceMethods.length() > 0) {
            return traceMethods.split(",");
        }
        return new String[]{""};
    }

    public BtraceConfig setTraceMethods(String traceMethods) {
        this.traceMethods = traceMethods;
        return this;
    }

    public String getSceneBaggage() {
        return this.sceneBaggage;
    }

    public Map<String, String> getSceneBaggageMap() {
        Map<String, String> map = new HashMap<>();
        try {
            if (sceneBaggage != null && sceneBaggage.length() > 0) {
                String[] baggageArray = sceneBaggage.split(",");
                for (String baggage : baggageArray) {
                    if (baggage != null && baggage.length() > 0) {
                        map.put(baggage.split(":")[0], baggage.split(":")[1]);
                    }
                }
            }
        } catch (Throwable throwable) {

        }
        return map.size() == 0 ? Collections.emptyMap() : map;
    }

    public BtraceConfig setSceneBaggage(String sceneBaggage) {
        this.sceneBaggage = sceneBaggage;
        return this;
    }

}
