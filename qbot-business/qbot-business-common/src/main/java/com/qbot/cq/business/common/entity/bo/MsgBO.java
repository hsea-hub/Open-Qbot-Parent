package com.qbot.cq.business.common.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MsgBO {
    private String content;
    private List<String> atWxids;
    private String roomId;
    private String fromUser;
    private String toUser;
    private Integer type;
    private Integer memberCount;
    private Long msgId;
    private Long msgSequence;
}
