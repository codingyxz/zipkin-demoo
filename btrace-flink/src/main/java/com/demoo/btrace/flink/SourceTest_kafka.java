package com.demoo.btrace.flink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;

/**
 * @author zhxy
 * @Date 2021/7/2 10:33 上午
 */
public class SourceTest_kafka {

    public static void main(String[] args) throws Exception{
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 设置并行度1
        env.setParallelism(1);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        // 下面这些次要参数
//        properties.setProperty("group.id", "consumer-group-sensor");
//        properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        properties.setProperty("auto.offset.reset", "latest");

        // flink添加外部数据源
        DataStream<String> dataStream = env.addSource(new FlinkKafkaConsumer011<String>("sensor", new SimpleStringSchema(),properties));

        // 打印输出
        dataStream.print();

        env.execute();
    }
}
