package com.demoo.btrace.flink.domain.span;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhxy
 * @Date 2021/7/1 4:37 下午
 */
@Data
public class Annotation implements Serializable {

    private Long timestamp;

    private String value;

    private Endpoint endpoint;

}
