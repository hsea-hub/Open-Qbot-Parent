package com.qbot.cq.business.user.generator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;

public class CodeGenerator {

    public static void main(String[] args) {

        // 💡 修正：包含 qbot-business 中间层路径
        String moduleDir = System.getProperty("user.dir") + "/qbot-business/qbot-business-user";
        String dbUrl = "jdbc:sqlite:" + moduleDir + "/src/main/resources/bin/qbot.db";

        String author = "cq";
        String parentPackage = "com.qbot.cq.business.user";

        FastAutoGenerator.create(dbUrl, "", "")
                .globalConfig(builder -> builder
                        .author(author)
                        .outputDir(moduleDir + "/src/main/java") // 保证生成到当前模块
                        .disableOpenDir()
                )
                .packageConfig(builder -> builder
                        .parent(parentPackage)
                        .entity("entity.po")
                        .mapper("mapper")
                        .service("service")
                        .controller("controller")
                )
                .strategyConfig(builder -> builder
                        .addInclude("config_global_command") // 可支持多个表
                        .entityBuilder()
                        .enableLombok()
                        .addTableFills(
                                new Column("create_time", FieldFill.INSERT),
                                new Column("create_id", FieldFill.INSERT),
                                new Column("strike_out", FieldFill.INSERT)
                        )
                        .mapperBuilder().enableBaseResultMap()
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
