package com.qbot.cq.framework.common.enums;

import lombok.Getter;

/**
 * @description: web enum
 * @author: cq
 * @create: 2020-12-04 16:44
 **/
@Getter
@SuppressWarnings("all")
public enum ResultCodeEnum {

    SUCCESS(true, 200, "success"),
    UNKNOWN_ERROR(false, 500, "network not available"),
    GATEWAY_ERROR(false, 561, "network busy"),
    PARAM_ERROR(false, 20002, "parameter error"),
    NULL_POINT(false, 20003, "null pointer exception"),
    HTTP_CLIENT_ERROR(false, 20004, "client connection exception"),
    VALIDATE_ERROR(false, 20005, "validate failed"),
    METHOD_NOT_SUPPORT_ERROR(false, 20006, "method not support"),
    BUSINESS_ERROR(false, 20007, "business error"),
    MESSAGE_IS_NOT_READABLE(false, 20008, "message is not readable"),
    DUPLICATE_KEY_ERROR(false, 20009, "duplicate parameter exception"),
    SERVLET_REQUEST_BINDING_ERROR(false, 20010, "servlet request binding exception"),
    AUTH_ERROR(false, 20011, "auth error"),
    HTTP_MEDIA_TYPE_NOT_SUPPORTED(false, 20012, "http media type not supported"),
    ;

    /**
     * 响应是否成功
     */
    private Boolean success;
    /**
     * 响应状态码
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String message;

    ResultCodeEnum(boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}