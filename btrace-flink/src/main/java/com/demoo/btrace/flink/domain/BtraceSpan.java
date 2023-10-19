package com.demoo.btrace.flink.domain;

import com.demoo.btrace.flink.domain.span.SpanV1;
import com.demoo.btrace.flink.utils.HexCodec;
import com.demoo.btrace.flink.utils.IpUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhxy
 * @Date 2021/7/1 4:18 下午
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BtraceSpan implements Serializable {

    /**
     * 父级span-id，如果是顶级链路，则为traceId后16位
     */
    private String parentId;
    /**
     * 本级span-id，traceId前16位
     */
    private String id;
    /**
     * 32位md5加密串
     */
    private String traceId;
    /**
     * 种类，表示调用的server还是client端
     */
    private String kind;
    /**
     * 使用极少 span之间的一个关系
     */
    private String reference;
    /**
     * span名称 比如一个url
     */
    private String name;
    /**
     * 当前span产生时的时间戳精确到毫秒
     */
    private Long timestamp;
    /**
     * 当前span耗时 精确到微妙
     */
    private Long duration;
    /**
     * 当前span产生所在的服务名称 比如:portal-h5-web
     */
    private String serviceName;
    /**
     * 当前span产生时所在的服务ip
     */
    private String ip;
    /**
     * 标识当前span是否错误， false 否，true 是
     */
    private Boolean error;
    /**
     * 全局k v键值对，多个span共享
     */
    private List<Pair> baggages = new ArrayList<>();
    /**
     * 标签k v键值对，除了一些基础信息外 用户也可以自己定义
     */
    private List<Pair> tags = new ArrayList();
    /**
     * 日志信息 当error为true时候logs会包含一些错误信息
     */
    private List<Log> logs = new ArrayList();

    public static BtraceSpanBuilder newBuilder() {
        return new BtraceSpanBuilder();
    }

    public static BtraceSpan buildFromSpanV1(SpanV1 span) {
        BtraceSpan sspan = new BtraceSpan();
        sspan.setTraceId(HexCodec.toLowerHex(span.getTraceIdHigh(), span.getTraceId()));
        sspan.setTimestamp(span.getTimestamp());
        sspan.setId(HexCodec.toLowerHex(span.getId()));
        sspan.setDuration(span.getDuration());
        sspan.setName(span.getName());
        sspan.setParentId(HexCodec.toLowerHex(span.getParentId()));
        return sspan;
    }

    public static BtraceSpan buildFromPbSpan(PbSpan.Span span) {
        BtraceSpan sspan = new BtraceSpan();
        sspan.setName(span.getName());
        sspan.setServiceName(span.getServiceName());
        sspan.setId(span.getId());
        sspan.setReference(span.getReference().name());
        sspan.setDuration(span.getDuration());
        sspan.setKind(span.getKind().name());
        sspan.setIp(IpUtil.getIpByInt(span.getIp()));
        sspan.setParentId(span.getParentId());
        sspan.setTimestamp(span.getTimestamp());
        sspan.setTraceId(span.getTraceId());
        return sspan;
    }

    @Data
    public static class BtraceSpanBuilder {
        private String parentId;
        private String id;
        private String traceId;
        private String kind;
        private String reference;
        private String name;
        private Long timestamp;
        private Long duration;
        private String serviceName;
        private String ip;
        private Boolean error;
        private List<Pair> baggages = new ArrayList();
        private List<Pair> tags = new ArrayList();
        private List<Log> logs = new ArrayList();

        public BtraceSpanBuilder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public BtraceSpanBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BtraceSpanBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public BtraceSpanBuilder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public BtraceSpanBuilder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public BtraceSpanBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BtraceSpanBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public BtraceSpanBuilder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public BtraceSpanBuilder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public BtraceSpanBuilder duration(Long duration) {
            this.duration = duration;
            return this;
        }

        public BtraceSpanBuilder error(Boolean error) {
            this.error = error;
            return this;
        }

        public BtraceSpanBuilder baggages(List<Pair> baggages) {
            this.baggages = baggages;
            return this;
        }

        public BtraceSpanBuilder tags(List<Pair> tags) {
            this.tags = tags;
            return this;
        }

        public BtraceSpanBuilder logs(List<Log> logs) {
            this.logs = logs;
            return this;
        }

        public BtraceSpan build() {
            return new BtraceSpan(this.parentId, this.id, this.traceId, this.kind, this.reference, this.name,
                    this.timestamp, this.duration, this.serviceName, this.ip, this.error, this.baggages, this.tags,
                    this.logs);
        }
    }

    @Data
    public static class Pair {

        private String key;
        private String value;

        // 事件Key
        public static final String COMPONENT_KEY = "component";
        public static final String APPVERSION_KEY = "app.version";
        public static final String OSVERSION_KEY = "os.version";
        public static final String NETWORK_KEY = "network";
        public static final String USERID_KEY = "userId";
        public static final String LOGIN_KEY = "loginId";
        public static final String APPREQUESTUUID_KEY = "app.request.uuid";
        public static final String OSTOKENID_KEY = "os.tokenId";
        public static final String STATUSCODE_KEY = "http.status_code";
        public static final String DATASIZE_KEY = "dataSize";
        public static final String ERROR = "error";
        public static final String APP_VSERION_TYPE = "versionType";
        public static final String UA = "userAgent";
        public static final String CDN_IP = "cdn.ip";
        public static final String AREA_NAME = "areaName";

        public static final String START_TIME = "startTime";
        public static final String END_TIME = "endTime";

        public static Pair build(String key, String value) {
            return new Pair(key, value);
        }

        public Pair() {
        }

        public Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            StringBuffer stringBuffer = new StringBuffer(getClass().getSimpleName() + ":{ ");
            stringBuffer.append("key=" + this.key == null ? " " : this.key + " ");
            stringBuffer.append("value=" + this.value == null ? " " : this.value + " ");
            stringBuffer.append(" }");
            return stringBuffer.toString();
        }
    }

    @Data
    public static class Log {
        private Long timestamp;
        private String key;
        private String value;
        // 事件Key
        public static final String EVENT_KEY = "event";
        public static final String DESC_KEY = "desc";
        // 时间value
        public static final String CLIENT_RECEIVE = "cr";
        public static final String CLIENT_SEND = "cs";
        // span的kind
        public static final String CLIENT = "CLIENT";

        public static Log build(Long timestamp) {
            Log log = new Log();
            log.setTimestamp(timestamp);
            return log;
        }

        public static Log build(Long timestamp, String key, String value) {
            return new Log(timestamp, key, value);
        }

        public Log() {
        }

        public Log(Long timestamp, String key, String value) {
            this.timestamp = timestamp;
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            StringBuffer stringBuffer = new StringBuffer(getClass().getSimpleName() + ":{ ");
            stringBuffer.append("timestamp=" + this.timestamp == null ? " " : this.timestamp + " ");
            stringBuffer.append("key=" + this.key == null ? " " : this.key + " ");
            stringBuffer.append("value=" + this.value == null ? " " : this.value + " ");
            stringBuffer.append(" }");
            return stringBuffer.toString();
        }

    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer(getClass().getSimpleName() + ":{ ");
        stringBuffer.append("parentId=" + this.parentId == null ? " " : this.parentId + " ");
        stringBuffer.append("id=" + this.id == null ? " " : this.id + " ");
        stringBuffer.append("traceId=" + this.traceId == null ? " " : this.traceId + " ");
        stringBuffer.append("kind=" + this.kind == null ? " " : this.kind + " ");
        stringBuffer.append("reference=" + this.reference == null ? " " : this.reference + " ");
        stringBuffer.append("name=" + this.name == null ? " " : this.name + " ");
        stringBuffer.append("timestamp=" + this.timestamp == null ? " " : this.timestamp + " ");
        stringBuffer.append("duration=" + this.duration == null ? " " : this.duration + " ");
        stringBuffer.append("serviceName=" + this.serviceName == null ? " " : this.serviceName + " ");
        stringBuffer.append("ip=" + this.ip == null ? " " : this.ip + " ");
        stringBuffer.append("baggages=" + this.baggages == null ? " " : Arrays.toString(this.baggages.toArray()) + " ");
        stringBuffer.append("tags=" + this.tags == null ? " " : Arrays.toString(this.tags.toArray()) + " ");
        stringBuffer.append("logs=" + this.logs == null ? " " : Arrays.toString(this.logs.toArray()) + " ");
        return stringBuffer.toString();
    }
}
