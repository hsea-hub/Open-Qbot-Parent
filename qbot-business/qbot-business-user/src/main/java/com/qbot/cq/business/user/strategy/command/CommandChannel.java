package com.qbot.cq.business.user.strategy.command;

import com.qbot.cq.business.common.entity.bo.MsgBO;
import com.qbot.cq.business.common.utils.CommandUtil;

public interface CommandChannel {
    String getGuideType();

    Boolean commandHandler(CommandUtil.CommandRequest commandRequest, MsgBO msgBO);
}
