package com.qbot.cq.business.user.entity.po;

import com.baomidou.mybatisplus.annotation.*;
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
 * @since 2025-04-21
 */
@Getter
@Setter
@ToString
@TableName("game_mah_jong_detail")
public class GameMahJongDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer baseId;

    private String botId;

    private String roomId;

    private String wxid;

    private String nickName;

    private String totalCardWall;

    private String curCardWall;

    private Integer curIndex;

    private String que;

    private String pg;
    private Integer roomStatus;

    @TableField(fill = FieldFill.INSERT)
    private String createId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateId;

    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Integer strikeOut;

    @TableField(fill = FieldFill.INSERT)
    @Version
    private Integer version;
}
