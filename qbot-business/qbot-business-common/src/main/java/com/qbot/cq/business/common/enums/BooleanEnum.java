package com.qbot.cq.business.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BooleanEnum {
    NO(0, "否"),

    YES(1,"是")
    ;
    private Integer value;
    private String name;

    public static BooleanEnum match(Integer param){
        for (BooleanEnum value : BooleanEnum.values()) {
            if (value.getValue().equals(param)){
                return value;
            }
        }
        return null;
    }
}
