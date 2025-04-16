package com.qbot.cq.business.user.controller;

import com.qbot.cq.framework.common.model.ResultVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CallbackController {
    @PostMapping("/api")
    public ResultVO<?> callBack(@RequestBody Map<String,Object> map){
        System.out.println(map);
        return ResultVO.success();
    }

}
