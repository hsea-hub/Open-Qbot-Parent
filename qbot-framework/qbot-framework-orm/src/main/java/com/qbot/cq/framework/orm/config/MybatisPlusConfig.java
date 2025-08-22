package com.qbot.cq.framework.orm.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.qbot.cq.framework.orm.interceptor.DataOperationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author QBOT
 */
@Configuration
@MapperScan(basePackages = "com.qbot.**.mapper")
public class MybatisPlusConfig {

    /**
     * @return
     */
    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return mybatisPlusInterceptor;
    }

    /**
     * 数据自动插入拦截器
     * 自动插入的数据包括：创建人、创建时间、修改人、修改时间
     */
    @Bean
    public DataOperationInterceptor dataOperationInterceptor() {
        return new DataOperationInterceptor();
    }

}
