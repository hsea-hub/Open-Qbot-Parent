package com.qbot.cq.business.user.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author cq
 * @since 2025-04-18
 */
@Getter
@Setter
@ToString
@TableName("game_mah_jong")
public class GameMahJong implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String botId;

    private String roomId;

    private String wxid;

    private String nickName;

    private String totalCardWall;

    private String curCardWall;
    private Integer curIndex;
    private String curCard;

    private Integer roomStatus;

    private String createId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime  createTime;

    private String updateId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Integer strikeOut;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
