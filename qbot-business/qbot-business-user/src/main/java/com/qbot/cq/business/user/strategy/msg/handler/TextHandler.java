package com.qbot.cq.business.user.strategy.msg.handler;

import com.alibaba.fastjson2.JSON;
import com.qbot.cq.business.common.entity.bo.MsgBO;
import com.qbot.cq.business.common.entity.dto.MsgDTO;
import com.qbot.cq.business.common.entity.dto.hook.SendTextMsgDTO;
import com.qbot.cq.business.common.enums.BooleanEnum;
import com.qbot.cq.business.common.enums.CommandEnum;
import com.qbot.cq.business.common.enums.MsgTypeEnum;
import com.qbot.cq.business.common.utils.CommandUtil;
import com.qbot.cq.business.common.utils.HookRequestUtil;
import com.qbot.cq.business.common.utils.MsgUtil;
import com.qbot.cq.business.user.entity.po.ConfigGlobalCommand;
import com.qbot.cq.business.user.service.IConfigGlobalCommandService;
import com.qbot.cq.business.user.strategy.msg.MsgChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class TextHandler implements MsgChannel {
    @Autowired
    private IConfigGlobalCommandService configGlobalCommandService;
    @Override
    public Integer getGuideType() {
        return MsgTypeEnum.TEXT.getValue();
    }

    @Override
    public void msgHandler(MsgDTO msg) {
        MsgBO msgBO = MsgUtil.msgConvert(msg);
        System.out.println(JSON.toJSONString(msgBO));
        //命令行提取
        CommandUtil.CommandRequest commandRequest = CommandUtil.parseCommand(msgBO.getContent());
        CommandEnum match = CommandEnum.match(commandRequest.getCommand());
        if (Objects.isNull(match)) return;
        ConfigGlobalCommand configGlobalCommand = configGlobalCommandService.lambdaQuery().eq(ConfigGlobalCommand::getIncludes, match.getName()).one();
        if (Objects.isNull(configGlobalCommand)) return;
        if (BooleanEnum.NO.getValue().equals(configGlobalCommand.getCacheStatus())){
            HookRequestUtil.sendTextMsg(SendTextMsgDTO.builder().wxid(msgBO.getRoomId()).msg(configGlobalCommand.getSuccess()).build());
        }
    }
}
