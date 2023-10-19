package com.demoo.btrace.flink.domain.span;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhxy
 * @Date 2021/7/1 4:38 下午
 */

@Data
public class BinaryAnnotation implements Serializable {

    private String key;

    private String value;

    private Endpoint endpoint;
}
