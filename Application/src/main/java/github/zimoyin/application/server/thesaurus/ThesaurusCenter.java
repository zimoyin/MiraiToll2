package github.zimoyin.application.server.thesaurus;

import github.zimoyin.application.dao.thesaurus.ThesaurusDao;
import github.zimoyin.mtool.dao.H2ConnectionFactory;
import github.zimoyin.mtool.dao.ResultMapObject;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class ThesaurusCenter implements Thesaurus {
    private volatile static ThesaurusCenter INSTANCE;
    private final HashMap<String, ArrayList<String>> cache = new HashMap<String, ArrayList<String>>();
    private final H2ConnectionFactory db = H2ConnectionFactory.INSTANCE;

    private ThesaurusCenter() {
        int count = 0;
        long length = 0;
        Connection connection = null;
        try {
            connection = db.getConnection();
//            Statement statement = db.getStatement(connection);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from CHAT");
            if (resultSet == null) {
                System.out.println(888);
            }
            List<ThesaurusDao> parseObject = new ResultMapObject<ThesaurusDao>().parseObject(resultSet, ThesaurusDao.class);
            ArrayList<String> list = new ArrayList<>();
            String key = null;
            for (ThesaurusDao dao : parseObject) {
                list.add(dao.getText_value());
                length += dao.getText_value().length();
                key = dao.getText_key();
            }
            if (key != null) length += key.length();
            count += list.size();
            cache.put(key, list);
        } catch (Exception e) {
            log.warn("查询数据库数据失败", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("无法被关闭的流", e);
                }
            }
        }
        log.info("加载词库数据:{}条", count);
        log.info("加载词库数据:{}字符", length);
        if (count >= 10000) {
            log.warn("词库数据较多+");
        }
    }

    public static ThesaurusCenter getInstance() {
        if (INSTANCE == null) synchronized (ThesaurusCenter.class) {
            if (INSTANCE == null) INSTANCE = new ThesaurusCenter();
        }
        return INSTANCE;
    }


    @Override
    public void addEntries(String key, String value) {
        ArrayList<String> list = cache.getOrDefault(key, new ArrayList<String>());
        list.add(value);
        cache.put(key, list);
        Connection connection = null;
        try {
            connection = db.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("insert into CHAT (TEXT_KEY, TEXT_VALUE) values (?,?)");
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, value);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            log.warn("词库向数据库存入数据失败", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("无法被关闭的流", e);
                }
            }
        }

    }

    @Override
    public boolean removeEntries(String key, String value) {
        return cache.get(key).remove(value);
    }

    @Override
    public ArrayList<String> removeEntries(String key) {
        return cache.remove(key);
    }

    @Override
    public ArrayList<String> getEntries(String key) {
        ArrayList<String> list = cache.get(key);
        if (list == null || list.size() == 0) {
            Connection connection = null;
            try {
                connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement("select * from CHAT where TEXT_KEY=?");
                statement.setString(1, key);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet == null) {
                    System.out.println(888);
                }
                List<ThesaurusDao> parseObject = new ResultMapObject<ThesaurusDao>().parseObject(resultSet, ThesaurusDao.class);
                list = new ArrayList<String>();
                for (ThesaurusDao dao : parseObject) {
                    list.add(dao.getText_value());
                }
                System.out.println(list.size());
                cache.put(key, list);
                return list;
            } catch (Exception e) {
                log.warn("查询数据库数据失败", e);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        log.error("无法被关闭的流", e);
                    }
                }
                if (list == null) list = new ArrayList<String>();
            }
        }
        return list;
    }

    @Override
    public void addCache(HashMap<String, ArrayList<String>> cache) {
        this.cache.putAll(cache);
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }
}
