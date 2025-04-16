package com.qbot.cq.business.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommandEnum {
    MENU("测试菜单"),
    ;
    private String name;

    public static CommandEnum match(String name){
        for (CommandEnum value : CommandEnum.values()) {
            if (value.getName().equals(name)){
                return value;
            }
        }
        return null;
    }
}
