package com.qbot.cq.business.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MsgTypeEnum {
    TEXT(1,"文本"),
    PAI(10002,"拍了拍"),
    ;

    private Integer value;
    private String name;
}
