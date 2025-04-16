package com.qbot.cq.business.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;

public class CommandUtil {

    @Data
    @AllArgsConstructor
    public static class CommandRequest {
        private String command;
        private String value;
    }

    /**
     * 解析命令格式，如：搜小说=内容 或 搜小说＝内容
     * @param input 用户输入的命令字符串
     * @return CommandRequest 对象，包含 command 和 value 字段
     */
    public static CommandRequest parseCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new CommandRequest(null, null);
        }

        // 同时支持半角 = 和全角 ＝
        String[] parts = input.split("[=＝]", 2);

        if (parts.length == 2) {
            return new CommandRequest(parts[0].trim(), parts[1].trim());
        } else {
            return new CommandRequest(input.trim(), null);
        }
    }

    // 示例 main 方法
    public static void main(String[] args) {
        CommandRequest cmd1 = parseCommand("搜小说=啊asdsad");
        CommandRequest cmd2 = parseCommand(null);

        System.out.println(cmd1); // CommandRequest(command=搜小说, value=啊asdsad)
        System.out.println(cmd2); // CommandRequest(command=搜小说, value=啊asdsad)
    }
}
