package com.demoo.opentracing;

import com.demoo.opentracing.property.BtraceProperty;
import com.demoo.opentracing.property.DynamicBtraceProperty;
import com.demoo.opentracing.property.PropertyListener;

import java.util.logging.Logger;

/**
 * @author zhxy
 * @Date 2021/6/28 8:46 下午
 */
public class BtraceConfigManager {

    private static final Logger LOGGER = Logger.getLogger(BtraceConfigManager.class.getName());
    private static volatile BtraceConfig btraceConfig = new BtraceConfig();
    private static final BtracePropertyListener LISTENER = new BtracePropertyListener();
    private static BtraceProperty<BtraceConfig> currentProperty = new DynamicBtraceProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }

    public static void register2Property(BtraceProperty<BtraceConfig> property){
        synchronized (LISTENER){
            LOGGER.info("[BtraceConfigManager] Registering new property to btrace config manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    public static BtraceConfig getConfig(){
        return btraceConfig;
    }

    private static final class BtracePropertyListener implements PropertyListener<BtraceConfig>{
        @Override
        public void configUpdate(BtraceConfig config) {
            applyConfig(config);
        }

        private synchronized void applyConfig(BtraceConfig config){
            if(config == null){
                LOGGER.warning("[BtraceConfigManager] Empty initial client assignment config");
                return;
            }
            btraceConfig = config;
        }

        @Override
        public void configLoad(BtraceConfig config) {
            applyConfig(config);
        }
    }

}
