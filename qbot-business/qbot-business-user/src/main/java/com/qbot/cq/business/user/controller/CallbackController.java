package com.qbot.cq.business.user.controller;

import com.alibaba.fastjson2.JSON;
import com.qbot.cq.business.common.contexts.UserRequestContext;
import com.qbot.cq.business.common.entity.dto.MsgDTO;
import com.qbot.cq.business.user.strategy.msg.MsgChannel;
import com.qbot.cq.business.user.strategy.msg.MsgFactory;
import com.qbot.cq.framework.common.model.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
public class CallbackController {
    @Autowired
    private MsgFactory msgFactory;
    @PostMapping("/api")
    public ResultVO<?> callBack(@RequestBody MsgDTO msgDTO){
        UserRequestContext.set(msgDTO.getToUser());
        MsgChannel msgChannel = msgFactory.guideChannel(msgDTO.getType());
        if (Objects.isNull(msgChannel)){
            log.info(">>> type unrealized：{}",JSON.toJSONString(msgDTO));
            return ResultVO.success();
        }
        msgChannel.msgHandler(msgDTO);
        UserRequestContext.remove();
        return ResultVO.success();
    }
}
