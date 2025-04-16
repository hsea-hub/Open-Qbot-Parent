package com.qbot.cq.business.user.strategy.msg;

import com.qbot.cq.business.common.entity.dto.MsgDTO;

public interface MsgChannel {
    Integer getGuideType();
    void msgHandler(MsgDTO msg);
}
