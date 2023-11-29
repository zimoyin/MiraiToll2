package github.zimoyin.application.dao.table;

import github.zimoyin.mtool.dao.H2ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 统一创建表
 */
@Slf4j
public class CreateTable {
    String[] sqls = new String[]{
            "userid(id BIGINT primary key,money SMALLINT)",
            "chat(id BIGINT primary key AUTO_INCREMENT,text_key VARCHAR_IGNORECASE,text_value VARCHAR_IGNORECASE)"
    };

    public CreateTable() {
        create();
    }

    public void create(long botID) {
        StringBuilder builder = new StringBuilder();
        for (String sql : sqls) {
            builder.append("create table if not exists").append(" ").append(sql).append(";");
        }
        try {
            try (Connection connection = H2ConnectionFactory.INSTANCE.getConnection(botID)) {
                connection.createStatement().execute(builder.toString());
            }
        } catch (SQLException e) {
            log.error("Error creating connection to " + botID, e);
        }
    }

    public void create() {
        StringBuilder builder = new StringBuilder();
        for (String sql : sqls) {
            builder.append("create table if not exists").append(" ").append(sql).append(";");
        }
        try {
            try (Connection connection = H2ConnectionFactory.INSTANCE.getConnection()) {
                connection.createStatement().execute(builder.toString());
            }
        } catch (SQLException e) {
            log.error("Error creating connection to global", e);
        }
    }
}
