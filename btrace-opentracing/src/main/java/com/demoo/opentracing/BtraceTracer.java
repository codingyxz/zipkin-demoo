package com.demoo.opentracing;

import com.demoo.opentracing.clock.Clock;
import com.demoo.opentracing.clock.DefaultClock;
import com.demoo.opentracing.idgenerators.IdGenerator;
import com.demoo.opentracing.idgenerators.ZipKin64bitIdGenerator;
import com.demoo.opentracing.idgenerators.ZipKinTraceId128BitIdGenerator;
import com.demoo.opentracing.propagation.B3TextMapCodec;
import com.demoo.opentracing.propagation.Extractor;
import com.demoo.opentracing.propagation.Injector;
import com.demoo.opentracing.propagation.TextMapCodec;
import com.demoo.opentracing.reporters.Reporter;
import com.demoo.opentracing.restrictions.BaggageRestrictionManager;
import com.demoo.opentracing.restrictions.LogFieldRestrictionManager;
import com.demoo.opentracing.samplers.Sampler;
import io.opentracing.*;
import io.opentracing.propagation.Format;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author zhxy
 * @Date 2021/6/27 2:53 下午
 */
public class BtraceTracer implements Tracer {

    private static final Logger LOGGER = Logger.getLogger(BtraceTracer.class.getName());

    private String serviceName;
    private Reporter reporter;
    private Sampler sampler;
    private Clock clock;

    private BtraceScopeManager scopeManager;
    private PropagationRegistry registry;
    private String localIpAddress;
    private IdGenerator spanIdGenerator;
    private IdGenerator traceIdGenerator;
    private LogFieldRestrictionManager logFieldRestrictionManager;
    private BaggageRestrictionManager baggageRestrictionManager;
    private String version;

    public static Builder newBuilder(String serviceName, Reporter reporter, Sampler sampler) {
        return new Builder(serviceName, reporter, sampler);
    }

    public BtraceTracer(String serviceName,
                        Reporter reporter,
                        Sampler sampler,
                        Clock clock,
                        BtraceScopeManager scopeManager,
                        PropagationRegistry registry,
                        IdGenerator spanIdGenerator,
                        IdGenerator traceIdGenerator,
                        LogFieldRestrictionManager logFieldRestrictionManager,
                        BaggageRestrictionManager baggageRestrictionManager) {
        this.serviceName = serviceName;
        this.reporter = reporter;
        this.sampler = sampler;
        this.clock = clock;
        this.scopeManager = scopeManager;
        this.registry = registry;
        this.localIpAddress = localIpAddress();
        this.spanIdGenerator = spanIdGenerator;
        this.traceIdGenerator = traceIdGenerator;
        this.logFieldRestrictionManager = logFieldRestrictionManager;
        this.baggageRestrictionManager = baggageRestrictionManager;
    }

    @Override
    public ScopeManager scopeManager() {
        return scopeManager;
    }

    @Override
    public Span activeSpan() {
        return scopeManager.activeSpan();
    }

    @Override
    public Scope activateSpan(Span span) {
        return scopeManager.activate(span);
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new BtraceSpanBuilder(this, operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        Injector<C> injector = registry.getInjector(format);
        if (injector == null) {
            LOGGER.warning("unsupported format:" + format.toString());
            return;
        }
        injector.inject((BtraceSpanContext) spanContext, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {

        Extractor<C> extractor = registry.getExtractor(format);
        if (extractor == null) {
            LOGGER.warning("unsupported format:" + format.toString());
            return null;
        }
        return extractor.extract(carrier);
    }

    @Override
    public void close() {
        reporter.close();
    }

    void reportSpan(Span span) {
        reporter.report(span);
    }

    public IdGenerator getSpanIdGenerator() {
        return spanIdGenerator;
    }

    public IdGenerator getTraceIdGenerator() {
        return traceIdGenerator;
    }

    public Sampler getSampler() {
        return sampler;
    }

    public Clock getClock() {
        return clock;
    }

    public String getServiceName() {
        return serviceName;
    }

    public LogFieldRestrictionManager getLogFieldRestrictionManager() {
        return logFieldRestrictionManager;
    }

    public BaggageRestrictionManager getBaggageRestrictionManager() {
        return baggageRestrictionManager;
    }

    public String getLocalIpAddress() {
        return localIpAddress;
    }

    private String localIpAddress() {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            if (nics == null) return null;
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                Enumeration<InetAddress> addresses = nic.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isSiteLocalAddress()) {
                        byte[] addressBytes = address.getAddress();
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // don't crash the caller if there was a problem reading nics.
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "error reading nics", e);
            }
        }
        return null;
    }

    private static class PropagationRegistry {
        private Map<Format<?>, Injector<?>> injectors = new HashMap<>();
        private Map<Format<?>, Extractor<?>> extractors = new HashMap<>();

        <T> Injector<T> getInjector(Format<T> format) {
            return (Injector<T>) injectors.get(format);
        }

        <T> Extractor<T> getExtractor(Format<T> format) {
            return (Extractor<T>) extractors.get(format);
        }

        public <T> void register(Format<T> format, Injector<T> injector) {
            injectors.put(format, injector);
        }

        public <T> void register(Format<T> format, Extractor<T> extractor) {
            extractors.put(format, extractor);
        }

    }

    public static final class Builder {
        private String serviceName;
        private Sampler sampler;
        private Reporter reporter;
        private TextMapCodec textMapCodec;
        private TextMapCodec httpMapCodec;

        private PropagationRegistry registry = new PropagationRegistry();
        private Clock clock = new DefaultClock();
        private BtraceScopeManager btraceScopeManager = new BtraceScopeManager();
        private BaggageRestrictionManager baggageRestrictionManager = BaggageRestrictionManager.DEFAULT;

        private IdGenerator spanIdGenerator = new ZipKin64bitIdGenerator();
        private IdGenerator traceIdGenerator = new ZipKin64bitIdGenerator();
        private LogFieldRestrictionManager logFieldRestrictionManager = LogFieldRestrictionManager.DEFAULT;

        public Builder(String serviceName, Reporter reporter, Sampler sampler) {
            if (serviceName == null || serviceName.trim().length() == 0) {
                throw new IllegalArgumentException("serviceName must not be null or empty");
            }
            this.serviceName = serviceName;
            this.reporter = reporter;
            this.sampler = sampler;

            textMapCodec = new B3TextMapCodec(false);
            textMapCodec.registerBaggageRestrictionManager(baggageRestrictionManager);
            this.registerInjector(Format.Builtin.TEXT_MAP, textMapCodec);
            this.registerExtractor(Format.Builtin.TEXT_MAP, textMapCodec);

            httpMapCodec = new B3TextMapCodec(true);
            httpMapCodec.registerBaggageRestrictionManager(baggageRestrictionManager);
            this.registerInjector(Format.Builtin.HTTP_HEADERS, httpMapCodec);
            this.registerExtractor(Format.Builtin.HTTP_HEADERS, httpMapCodec);
        }

        public <T> Builder traceId128Bit(boolean is128bit) {
            traceIdGenerator = is128bit ? new ZipKinTraceId128BitIdGenerator() : new ZipKin64bitIdGenerator();
            return this;
        }

        public <T> Builder registerInjector(Format<T> format, Injector<T> injector) {
            this.registry.register(format, injector);
            return this;
        }

        public <T> Builder registerExtractor(Format<T> format, Extractor<T> extractor) {
            this.registry.register(format, extractor);
            return this;
        }

        public Builder withBtraceScopeManager(BtraceScopeManager btraceScopeManager) {
            this.btraceScopeManager = btraceScopeManager;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withBaggageRestrictionManager(BaggageRestrictionManager baggageRestrictionManager) {
            this.baggageRestrictionManager = baggageRestrictionManager;
            textMapCodec.registerBaggageRestrictionManager(baggageRestrictionManager);
            httpMapCodec.registerBaggageRestrictionManager(baggageRestrictionManager);
            return this;
        }

        public Builder withLogFieldRestrictionManager(LogFieldRestrictionManager logFieldRestrictionManager) {
            this.logFieldRestrictionManager = logFieldRestrictionManager;
            return this;
        }

        public Tracer build() {
            return new BtraceTracer(this.serviceName,
                    this.reporter,
                    this.sampler,
                    this.clock,
                    this.btraceScopeManager,
                    this.registry,
                    this.spanIdGenerator,
                    this.traceIdGenerator,
                    this.logFieldRestrictionManager,
                    this.baggageRestrictionManager);
        }

    }


}
