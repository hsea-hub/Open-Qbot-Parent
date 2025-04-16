package com.qbot.cq.business.common.entity.dto;

import lombok.Data;

@Data
public class MsgDTO {
    private String content;
    private String displayFullContent;
    private String fromUser;
    private Long msgId;
    private Long msgSequence;
    private String toUser;
    private String signature;
    private Integer type;
}
