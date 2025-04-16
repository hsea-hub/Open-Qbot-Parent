package com.qbot.cq.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;

/**
 * @Description: 信息枚举
 **/
@Getter
@AllArgsConstructor
public enum MessageEnum {
    DATA_IS_NULL("data_is_null"
            , "The required field cannot be empty"
    ),
    UNLAWFUL_OPERATIONS("unlawful_operations"
            , "Network error, please try again later"
    ),
    ;
    /**
     * key
     */
    private String key;
    /**
     * message
     */
    private String message;

    public static String matchKey(String key) {
        for (MessageEnum value : MessageEnum.values()) {
            if (value.getKey().equals(key)) {
                return value.getMessage();
            }
        }
        return null;
    }
}
