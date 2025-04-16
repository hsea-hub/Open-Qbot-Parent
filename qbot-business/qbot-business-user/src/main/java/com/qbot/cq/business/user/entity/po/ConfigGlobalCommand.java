package com.qbot.cq.business.user.entity.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @since 2025-04-16
 */
@Getter
@Setter
@ToString
@TableName("config_global_command")
public class ConfigGlobalCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String includes;

    private String success;

    private String fail;

    private Integer cacheStatus;

    private Integer permission;

    @TableField(fill = FieldFill.INSERT)
    private String createId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateId;

    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Integer strikeOut;
}
