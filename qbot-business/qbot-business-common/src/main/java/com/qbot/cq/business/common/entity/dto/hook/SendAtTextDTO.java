package com.qbot.cq.business.common.entity.dto.hook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendAtTextDTO {
    private String chatRoomId;
    private String wxids;
    private String msg;
}
