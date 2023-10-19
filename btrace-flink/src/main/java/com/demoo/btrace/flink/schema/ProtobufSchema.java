package com.demoo.btrace.flink.schema;

import com.demoo.btrace.flink.domain.BtraceSpan;
import com.demoo.btrace.flink.domain.PbSpan;
import com.demoo.btrace.flink.utils.SpanTransformerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.util.List;

/**
 * @author zhxy
 * @Date 2021/7/1 5:40 下午
 */

@Slf4j
public class ProtobufSchema implements DeserializationSchema<List<BtraceSpan>>, SerializationSchema<List<BtraceSpan>> {

    private static final long serialVersionUID = 7370734496080687268L;

    @Override
    public List<BtraceSpan> deserialize(byte[] bytes) throws IOException {
        if(bytes.length > 0){
            try {
                List<PbSpan.Span> spans = PbSpan.SpanList.parseFrom(bytes).getSpansList();
                return SpanTransformerUtil.pbSpanToBtraceSpan(spans);
            }catch (Exception e){
                log.error("Protobuf序列化出错 : {}", e.toString());
            }
        }
        return Lists.newArrayList();
    }

    @Override
    public boolean isEndOfStream(List<BtraceSpan> btraceSpans) {
        return false;
    }

    @Override
    public byte[] serialize(List<BtraceSpan> btraceSpans) {
        return new byte[0];
    }

    @Override
    public TypeInformation<List<BtraceSpan>> getProducedType() {
        return TypeInformation.of(new TypeHint<List<BtraceSpan>>() {

        });
    }
}
