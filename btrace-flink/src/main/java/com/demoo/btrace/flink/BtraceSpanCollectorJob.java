package com.demoo.btrace.flink;

import com.demoo.btrace.flink.domain.BtraceSpan;
import com.demoo.btrace.flink.schema.ProtobufSchema;
import com.demoo.btrace.flink.sink.TraceElasticsearchSinkFunction;
import com.demoo.btrace.flink.utils.IndexNameFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.*;

/**
 * @author zhxy
 * @Date 2021/7/1 3:49 下午
 */

@Slf4j
public class BtraceSpanCollectorJob {


    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);

        // 从kafka的btrace队列中消费链路消息
        Properties btraceProp = new Properties();
        btraceProp.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        FlinkKafkaConsumer011<List<BtraceSpan>> zipkinKafkaConsumer =
                new FlinkKafkaConsumer011<>("btrace_flink", new ProtobufSchema(), btraceProp);
        DataStream<List<BtraceSpan>> btraceKafkaSource = env.addSource(zipkinKafkaConsumer);

        // 将数据批量插入到elasticsearch中
        List<HttpHost> httpHosts = new ArrayList<>();
        httpHosts.add(new HttpHost("localhost", 9200));
        IndexNameFormatter traceIndexNameFormatter = IndexNameFormatter.createIndexNameFormatter("btrace:opentracing:span");
        log.info(traceIndexNameFormatter.getCachedIndex(System.currentTimeMillis() / 1000));
        btraceKafkaSource.addSink(new ElasticsearchSink.Builder<>(httpHosts, new TraceElasticsearchSinkFunction(traceIndexNameFormatter)).build()).name("esSink");
        env.execute();
    }

}
