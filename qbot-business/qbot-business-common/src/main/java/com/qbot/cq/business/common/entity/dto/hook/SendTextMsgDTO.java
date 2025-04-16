package com.qbot.cq.business.common.entity.dto.hook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendTextMsgDTO {
    private String wxid;
    private String msg;
}
