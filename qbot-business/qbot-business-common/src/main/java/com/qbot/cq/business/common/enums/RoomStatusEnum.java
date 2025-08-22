package com.qbot.cq.business.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomStatusEnum {

    WAITING(1, "待加入"),
    STARTED(2, "已开始"),
    FINISHED(3, "已结束"),
    DISBANDED(4, "已解散"),
    AUTO_DISBANDED(5, "自动解散");

    private final int value;
    private final String name;

    public static RoomStatusEnum fromValue(Integer value) {
        if (value == null) return null;
        for (RoomStatusEnum status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }
}
