package com.qbot.cq.business.user.strategy.msg.handler;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.qbot.cq.business.common.entity.bo.MsgBO;
import com.qbot.cq.business.common.entity.dto.MsgDTO;
import com.qbot.cq.business.common.entity.dto.hook.SendAtTextDTO;
import com.qbot.cq.business.common.entity.dto.hook.SendTextMsgDTO;
import com.qbot.cq.business.common.enums.BooleanEnum;
import com.qbot.cq.business.common.enums.CommandEnum;
import com.qbot.cq.business.common.enums.MsgTypeEnum;
import com.qbot.cq.business.common.utils.CommandUtil;
import com.qbot.cq.business.common.utils.HookRequestUtil;
import com.qbot.cq.business.common.utils.MsgUtil;
import com.qbot.cq.business.user.entity.contants.TextContant;
import com.qbot.cq.business.user.entity.po.ConfigGlobalCommand;
import com.qbot.cq.business.user.service.IConfigGlobalCommandService;
import com.qbot.cq.business.user.strategy.command.CommandChannel;
import com.qbot.cq.business.user.strategy.command.CommandFactory;
import com.qbot.cq.business.user.strategy.msg.MsgChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class TextHandler implements MsgChannel {
    public static Cache<String,String> timedCaches = CacheUtil.newTimedCache(60*1000);

    @Autowired
    private CommandFactory commandFactory;

    @Autowired
    private IConfigGlobalCommandService configGlobalCommandService;
    @Override
    public Integer getGuideType() {
        return MsgTypeEnum.TEXT.getValue();
    }

    @Override
    public void msgHandler(MsgDTO msg) {
        MsgBO msgBO = MsgUtil.msgConvert(msg);
        String key = msgBO.getRoomId() + msgBO.getFromUser();
        System.out.println(JSON.toJSONString(msgBO));
        // 命令行提取
        CommandUtil.CommandRequest commandRequest = CommandUtil.parseCommand(msgBO.getContent());
        CommandEnum match = CommandEnum.match(commandRequest.getCommand());
        String cache = timedCaches.get(key);
        if (Objects.isNull(match) && StrUtil.isEmpty(cache)) return;
        // 缓存没失效设置口令
        if (StrUtil.isNotBlank(cache) && Objects.isNull(match)) {
            commandRequest.setCommand(cache);
            commandRequest.setValue(msgBO.getContent());
        }
        // 查询配置命令
        ConfigGlobalCommand configGlobalCommand = configGlobalCommandService.lambdaQuery().eq(ConfigGlobalCommand::getIncludes, commandRequest.getCommand()).one();
        if (Objects.isNull(configGlobalCommand)) return;
        // 非缓存状态直接返回数据
        if (BooleanEnum.NO.getValue().equals(configGlobalCommand.getCacheStatus())){
            HookRequestUtil.sendAtText(msgBO.getToUser(),SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg(configGlobalCommand.getSuccess()).build());
        } else {
            if (Objects.isNull(commandRequest.getValue())) {
                HookRequestUtil.sendAtText(msgBO.getToUser(),SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg(configGlobalCommand.getSuccess()).build());
                timedCaches.put(key,commandRequest.getCommand());
                if (Objects.nonNull(cache) && Objects.nonNull(commandRequest.getCommand())){
                    HookRequestUtil.sendAtText(msgBO.getToUser(),SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg(StrFormatter.format(TextContant.REPLACE_INSTRUCTION,cache,commandRequest.getCommand())).build());
                }
                return;
            }
            // 缓存数据业务处理
            CommandChannel commandChannel = commandFactory.guideChannel(commandRequest.getCommand());

            try {
                commandChannel.commandHandler(commandRequest,msgBO);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                HookRequestUtil.sendAtText(msgBO.getToUser(),SendAtTextDTO.builder().chatRoomId(msgBO.getRoomId()).wxids(msgBO.getFromUser()).msg(StrFormatter.format(TextContant.THE_INSTRUCTION_HAS_EXPIRED,commandRequest.getCommand())).build());
            }

        }
    }
}
