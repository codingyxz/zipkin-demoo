package com.demoo.btrace.flink.domain.span;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhxy
 * @Date 2021/7/1 4:36 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpanV1 implements Serializable {

    private Long traceIdHigh;

    private long traceId;

    private String name;

    private long id;

    private Long parentId;

    private Long timestamp;

    private Long duration;

    private List<Annotation> annotations;

    private List<BinaryAnnotation> binaryAnnotations;

    private Boolean debug;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Long traceIdHigh;

        private Long traceId;

        private String name;

        private Long id;

        private Long parentId;

        private Long timestamp;

        private Long duration;

        private List<Annotation> annotations;

        private List<BinaryAnnotation> binaryAnnotations;

        private Boolean debug;

        public Builder traceIdHigh(Long value) {
            this.traceIdHigh = value;
            return this;
        }

        public Builder traceId(Long value) {
            this.traceId = value;
            return this;
        }

        public Builder name(String value) {
            this.name = value;
            return this;
        }

        public Builder id(Long value) {
            this.id = value;
            return this;
        }

        public Builder parentId(Long value) {
            this.parentId = value;
            return this;
        }

        public Builder timestamp(Long value) {
            this.timestamp = value;
            return this;
        }

        public Builder duration(Long value) {
            this.duration = value;
            return this;
        }

        public Builder annotation(Annotation value) {
            if (CollectionUtils.isEmpty(this.annotations)) {
                this.annotations = new ArrayList<>();
            }
            this.annotations.add(value);
            return this;
        }

        public Builder binaryAnnotation(BinaryAnnotation value) {
            if (CollectionUtils.isEmpty(this.binaryAnnotations)) {
                this.binaryAnnotations = new ArrayList<>();
            }
            this.binaryAnnotations.add(value);
            return this;
        }

        public Builder debug(Boolean value) {
            this.debug = value;
            return this;
        }

        public SpanV1 build() {
            return new SpanV1(this.traceIdHigh, this.traceId, this.name, this.id, this.parentId, this.timestamp, this.duration, this.annotations, this.binaryAnnotations, this.debug);
        }
    }
}
