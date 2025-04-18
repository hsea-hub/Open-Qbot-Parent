package com.qbot.cq.business.user.strategy.command.handler;

import com.qbot.cq.business.common.entity.bo.MsgBO;
import com.qbot.cq.business.common.entity.dto.hook.SendAtTextDTO;
import com.qbot.cq.business.common.enums.CommandEnum;
import com.qbot.cq.business.common.utils.CommandUtil;
import com.qbot.cq.business.common.utils.HookRequestUtil;
import com.qbot.cq.business.user.strategy.command.CommandChannel;
import org.springframework.stereotype.Component;

@Component
public class PlayMahjongHandler implements CommandChannel {
    private String CREATE = "创建房间";
    @Override
    public String getGuideType() {
        return CommandEnum.PLAY_MAHJONG.getName();
    }

    @Override
    public Boolean commandHandler(CommandUtil.CommandRequest commandRequest, MsgBO msgBO) {
        System.out.println(commandRequest.getCommand());
        // 创建房间
        if (CREATE.equals(commandRequest.getValue())) {
            HookRequestUtil.sendAtText(msgBO.getToUser(),SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg("正在开发...").build());
        }

        return true;
    }
}
