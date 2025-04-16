package com.qbot.cq.business.common.utils;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.qbot.cq.business.common.entity.dto.hook.SendAtTextDTO;
import com.qbot.cq.business.common.entity.dto.hook.SendTextMsgDTO;

public class HookRequestUtil {
    private static final String baseUrl = "http://127.0.0.1:19088/api";

    public static void sendTextMsg(SendTextMsgDTO sendTextMsgDTO){
        String body = HttpRequest.post(baseUrl + "/sendTextMsg")
                .body(JSON.toJSONString(sendTextMsgDTO))
                .execute()
                .body();
    }
    public static void sendAtText(SendAtTextDTO sendAtTextDTO){
        sendAtTextDTO.setMsg("\n"+sendAtTextDTO.getMsg());
        String body = HttpRequest.post(baseUrl + "/sendAtText")
                .body(JSON.toJSONString(sendAtTextDTO))
                .execute()
                .body();
    }
}
