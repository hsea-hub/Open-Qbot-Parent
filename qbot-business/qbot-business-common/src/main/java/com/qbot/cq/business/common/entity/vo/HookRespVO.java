package com.qbot.cq.business.common.entity.vo;

import lombok.Data;

@Data
public class HookRespVO<T> {
    private Integer code;
    private String msg;
    private T data;
}
