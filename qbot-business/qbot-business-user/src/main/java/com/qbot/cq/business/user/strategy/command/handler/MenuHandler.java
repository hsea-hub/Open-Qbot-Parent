package com.qbot.cq.business.user.strategy.command.handler;

import com.qbot.cq.business.common.entity.bo.MsgBO;
import com.qbot.cq.business.common.enums.CommandEnum;
import com.qbot.cq.business.common.utils.CommandUtil;
import com.qbot.cq.business.user.strategy.command.CommandChannel;
import org.springframework.stereotype.Component;

@Component
public class MenuHandler implements CommandChannel {
    @Override
    public String getGuideType() {
        return CommandEnum.MENU.getName();
    }

    @Override
    public Boolean commandHandler(CommandUtil.CommandRequest commandRequest, MsgBO msgBO) {
        return null;
    }
}
