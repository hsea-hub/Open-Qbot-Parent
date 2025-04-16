package com.qbot.cq.business.user.strategy.msg;

import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MsgFactory {
    private static Map<Integer, MsgChannel> factoryMaps = Maps.newHashMap();

    @Autowired
    private List<MsgChannel> channelList;

    @PostConstruct
    private void init() {
        channelList.forEach(channel -> {
            factoryMaps.put(channel.getGuideType(),channel);
        });
    }

    public MsgChannel guideChannel(Integer event){
        return factoryMaps.get(event);
    }
}
