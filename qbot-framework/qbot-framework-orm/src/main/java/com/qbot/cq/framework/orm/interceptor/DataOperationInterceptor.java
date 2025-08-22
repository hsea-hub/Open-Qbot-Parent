package com.qbot.cq.framework.orm.interceptor;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
public class DataOperationInterceptor implements MetaObjectHandler {

    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_TIME = "updateTime";
    private static final String UPDATE_ID = "updateId";
    private static final String VERSION = "version";


    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, CREATE_TIME, ()->now, LocalDateTime.class);
        this.strictInsertFill(metaObject, UPDATE_TIME, ()->now, LocalDateTime.class);
        this.strictInsertFill(metaObject, VERSION, () -> 0, Integer.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictUpdateFill(metaObject, UPDATE_TIME, () -> now, LocalDateTime.class);
    }

}
