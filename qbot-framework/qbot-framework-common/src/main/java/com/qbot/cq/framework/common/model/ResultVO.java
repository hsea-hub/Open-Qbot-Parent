package com.qbot.cq.framework.common.model;

import com.qbot.cq.framework.common.enums.MessageEnum;
import com.qbot.cq.framework.common.enums.ResultCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultVO<T> implements Serializable {
    /**
     * code 码
     */
    private Integer code;
    /**
     * 国际化key
     */
    private String messageKey;
    /**
     * 信息
     */
    private String message;
    /**
     * 返回数据
     */
    private T data;
    /**
     * 时间戳
     */
    private LocalDateTime localDateTime;

    public static <T> ResultVO success(T t) {
        return ResultVO.builder()
                .code(ResultCodeEnum.SUCCESS.getCode())
                .messageKey(ResultCodeEnum.SUCCESS.getMessage())
                .message(ResultCodeEnum.SUCCESS.getMessage())
                .localDateTime(LocalDateTime.now())
                .data(t)
                .build();
    }

    public static ResultVO success() {
        return ResultVO.builder()
                .code(ResultCodeEnum.SUCCESS.getCode())
                .messageKey(ResultCodeEnum.SUCCESS.getMessage())
                .message(ResultCodeEnum.SUCCESS.getMessage())
                .localDateTime(LocalDateTime.now())
                .build();
    }

    public static ResultVO error() {
        return ResultVO.builder()
                .code(ResultCodeEnum.BUSINESS_ERROR.getCode())
                .messageKey(ResultCodeEnum.BUSINESS_ERROR.getMessage())
                .message(ResultCodeEnum.BUSINESS_ERROR.getMessage())
                .localDateTime(LocalDateTime.now())
                .build();
    }

    public static ResultVO error(MessageEnum messageEnum) {
        return ResultVO.builder()
                .code(ResultCodeEnum.BUSINESS_ERROR.getCode())
                .messageKey(messageEnum.getKey())
                .message(messageEnum.getMessage())
                .localDateTime(LocalDateTime.now())
                .build();
    }
}
