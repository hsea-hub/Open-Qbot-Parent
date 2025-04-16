package com.qbot.cq.business.user.strategy.command;

import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CommandFactory {
    private static Map<String, CommandChannel> factoryMaps = Maps.newHashMap();

    @Autowired
    private List<CommandChannel> channelList;

    @PostConstruct
    private void init() {
        channelList.forEach(channel -> {
            factoryMaps.put(channel.getGuideType(),channel);
        });
    }

    public CommandChannel guideChannel(String event){
        return factoryMaps.get(event);
    }
}
