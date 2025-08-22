package com.qbot.cq.business.user;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;

@Slf4j
@SpringBootApplication
@MapperScan(basePackages = "com.qbot.cq.business.user.mapper")
@EnableScheduling
public class UserApplication {

    public static void main(String[] args) {
        String userDbPath = System.getProperty("user.home") + File.separator + ".qbot" + File.separator + "qbot.db";
        System.setProperty("QBOT_DB_PATH", userDbPath);

        initDbIfNotExists(userDbPath);
        String templatePath = extractTemplateDbToTempFile();

        try {
            syncTemplateToUserDb(templatePath, userDbPath);
        } catch (Exception e) {
            log.error("❌ 同步表结构失败：{}", e.getMessage(), e);
        }

        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(UserApplication.class, args);
        Environment environment = configurableApplicationContext.getBean(Environment.class);
        log.info(" >>> Start successful，Access link: http://localhost:{}", environment.getProperty("server.port"));
    }

    private static void initDbIfNotExists(String userDbPath) {
        File dbFile = new File(userDbPath);
        if (dbFile.exists()) {
            log.info("📂 用户数据库已存在：{}", userDbPath);
            return;
        }
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("bin/qbot.db")) {
            if (in == null) throw new RuntimeException("未找到内置模板数据库 resources/bin/qbot.db");

            dbFile.getParentFile().mkdirs();
            Files.copy(in, dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("✅ 初始化用户数据库成功：{}", userDbPath);
        } catch (IOException e) {
            throw new RuntimeException("❌ 初始化数据库失败", e);
        }
    }

    private static String extractTemplateDbToTempFile() {
        try {
            File tempFile = File.createTempFile("qbot-template", ".db");
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("bin/qbot.db")) {
                if (in == null) throw new RuntimeException("未找到模板数据库");
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                tempFile.deleteOnExit();
                return tempFile.getAbsolutePath();
            }
        } catch (IOException e) {
            throw new RuntimeException("❌ 拷贝模板数据库失败", e);
        }
    }

    private static void syncTemplateToUserDb(String templateDbPath, String userDbPath) throws Exception {
        try (Connection tplConn = DriverManager.getConnection("jdbc:sqlite:" + templateDbPath);
             Connection userConn = DriverManager.getConnection("jdbc:sqlite:" + userDbPath)) {

            Set<String> userTables = getTableNames(userConn);
            Map<String, List<String>> userFields = getTableFields(userConn);
            Map<String, String> tplCreateSql = getCreateTableSql(tplConn);
            Map<String, List<String>> tplFields = getTableFields(tplConn);

            for (Map.Entry<String, String> entry : tplCreateSql.entrySet()) {
                String table = entry.getKey();
                String createSql = entry.getValue();

                if (!userTables.contains(table)) {
                    log.info("🆕 创建新表：{}", table);
                    userConn.createStatement().execute(createSql);
                    continue;
                }

                List<String> tplCols = tplFields.get(table);
                List<String> userCols = userFields.getOrDefault(table, new ArrayList<>());

                for (String col : tplCols) {
                    if (!userCols.contains(col)) {
                        log.info("➕ 表 {} 添加字段：{}", table, col);
                        String alterSql = String.format("ALTER TABLE %s ADD COLUMN %s TEXT;", table, col);
                        userConn.createStatement().execute(alterSql);
                    }
                }
            }

            log.info("✅ 数据库结构同步完成 ✅");
            syncTableOverwrite(tplConn, userConn, "config_global_command");
        }
    }

    private static Set<String> getTableNames(Connection conn) throws SQLException {
        Set<String> tables = new HashSet<>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
        while (rs.next()) tables.add(rs.getString(1));
        return tables;
    }

    private static Map<String, String> getCreateTableSql(Connection conn) throws SQLException {
        Map<String, String> map = new HashMap<>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT name, sql FROM sqlite_master WHERE type='table'");
        while (rs.next()) map.put(rs.getString("name"), rs.getString("sql"));
        return map;
    }

    private static Map<String, List<String>> getTableFields(Connection conn) throws SQLException {
        Map<String, List<String>> map = new HashMap<>();
        for (String table : getTableNames(conn)) {
            List<String> fields = new ArrayList<>();
            ResultSet rs = conn.createStatement().executeQuery("PRAGMA table_info('" + table + "')");
            while (rs.next()) fields.add(rs.getString("name"));
            map.put(table, fields);
        }
        return map;
    }

    private static void syncTableOverwrite(Connection tplConn, Connection userConn, String tableName) throws SQLException {
        List<Map<String, Object>> templateRows = readAllRows(tplConn, tableName);
        log.info("🧪 模板库 [{}] 行数：{}", tableName, templateRows.size());

        userConn.createStatement().execute("DELETE FROM " + tableName);
        log.info("✅ 已清空用户表：{}", tableName);

        for (Map<String, Object> row : templateRows) {
            StringBuilder cols = new StringBuilder();
            StringBuilder vals = new StringBuilder();
            for (String col : row.keySet()) {
                cols.append(col).append(",");
                vals.append("?,");
            }

            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                    tableName,
                    cols.substring(0, cols.length() - 1),
                    vals.substring(0, vals.length() - 1));

            try (PreparedStatement ps = userConn.prepareStatement(sql)) {
                int i = 1;
                for (Object val : row.values()) {
                    ps.setObject(i++, val);
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("❌ 插入失败，SQL = {}", sql, e);
            }
        }

        log.info("✅ 表 [{}] 同步完成，已覆盖 {} 行", tableName, templateRows.size());
    }

    private static List<Map<String, Object>> readAllRows(Connection conn, String tableName) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + tableName);
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(meta.getColumnName(i), rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }
}
