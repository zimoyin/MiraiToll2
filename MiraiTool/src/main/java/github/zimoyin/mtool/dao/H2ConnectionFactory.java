package github.zimoyin.mtool.dao;

import github.zimoyin.mtool.config.global.H2Config;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * H2 数据库
 */
@Slf4j
public class H2ConnectionFactory {
    private final HashMap<Long, JdbcConnectionPool> Pools = new HashMap<>();    public volatile static H2ConnectionFactory INSTANCE = getInstance();
    private final JdbcConnectionPool Pool;
    private H2ConnectionFactory() {
        H2Config.H2 h2 = H2Config.getInstance().getH2();
        Pool = JdbcConnectionPool.create(h2.getGlobal_jdbc(), h2.getUser(), h2.getPassword());
        log.info("JDBC 连接池创建成功；JDBC_URL:{}", h2.getGlobal_jdbc());
    }

    private static H2ConnectionFactory getInstance() {
        if (INSTANCE == null) synchronized (H2ConnectionFactory.class) {
            if (INSTANCE == null) INSTANCE = new H2ConnectionFactory();
        }
        return INSTANCE;
    }

    public void close() {
        Pools.values().forEach(JdbcConnectionPool::dispose);
        Pool.dispose();
    }

    private JdbcConnectionPool newPool(long botID) {
        H2Config.H2 h2 = H2Config.getInstance().getH2();
        String jdbc = h2.getJdbc() + "/" + botID;
        JdbcConnectionPool pool = JdbcConnectionPool.create(jdbc, h2.getUser(), h2.getPassword());
        Pools.put(botID, pool);
        log.info("JDBC 连接池创建成功；JDBC_URL:{}", jdbc);
        return pool;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = Pool.getConnection();
        connection = new H2ConnectionProxyFactory(connection).getProxyInstance();
        log.info("Get JDBC Connection From ConnectionPool: {}", connection.getCatalog());
        return connection;
    }

    public Connection getConnection(long botID) throws SQLException {
        JdbcConnectionPool pool = Pools.get(botID);
        if (pool == null) pool = newPool(botID);
        Connection connection = pool.getConnection();
        connection = new H2ConnectionProxyFactory(connection).getProxyInstance();
        log.info("Get JDBC Connection From ConnectionPool: {}", connection.getCatalog());
        return connection;
    }


}
