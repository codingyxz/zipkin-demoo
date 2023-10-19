package com.demoo.btrace.flink.sink;

import com.alibaba.fastjson.JSONObject;
import com.demoo.btrace.flink.domain.BtraceSpan;
import com.demoo.btrace.flink.utils.IndexNameFormatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhxy
 * @Date 2021/7/2 9:09 上午
 */
public class TraceElasticsearchSinkFunction implements ElasticsearchSinkFunction<List<BtraceSpan>>, Serializable {

    private IndexNameFormatter traceIndexNameFormatter;

    public TraceElasticsearchSinkFunction(IndexNameFormatter traceIndexNameFormatter) {
        this.traceIndexNameFormatter = traceIndexNameFormatter;
    }

    @Override
    public void process(List<BtraceSpan> btraceSpans, RuntimeContext runtimeContext, RequestIndexer requestIndexer) {
        for (BtraceSpan serverSpan : btraceSpans) {
            String index = traceIndexNameFormatter.getCachedIndex(serverSpan.getTimestamp() / 1000);
            if (StringUtils.isNotBlank(index)) {
                // 用index发送请求
                requestIndexer.add(createUcIndexRequest(index, serverSpan));
            }
        }
    }

    public IndexRequest createUcIndexRequest(String index, BtraceSpan span) {
        // 创建请求，作为向es发起的写入命令(ES7统一type就是_doc，不再允许指定type)
        System.out.println(JSONObject.toJSONString(span));
        return Requests.indexRequest()
                .index(index)
                .source(JSONObject.toJSONString(span), XContentType.JSON);
    }
}
