package top.sssd.ddns.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * @author sssd
 * @careate 2023-09-26-15:40
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "com.mysql.cj.jdbc.Driver")
public class MySQLInitializer {

    private static final String TABLE_EXIST_SQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";

    private static final String TABLE_JOB_TASK = "job_task";

    private static final String TABLE_PARSING_RECORD = "parsing_record";

    private static final String MYSQL_SCRIPT_PATH = "sql/ddns4j_mysql.sql";

    @Resource
    private JdbcTemplate jdbcTemplate;


    @PostConstruct
    public void initSQL() throws SQLException {
        log.info("开始检查项目是否是第一次启动");
        if (!isTableExists(TABLE_JOB_TASK) && !isTableExists(TABLE_PARSING_RECORD)) {
            log.info("初始化mysql数据库脚本开始...");
            executeScript(MYSQL_SCRIPT_PATH);
            log.info("初始化mysql数据库脚本结束...");
        }
    }

    private boolean isTableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(TABLE_EXIST_SQL, Integer.class, tableName);
        return count != null && count > 0;
    }

    private void executeScript(String scriptPath) throws SQLException {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), resource);
    }
}
