package github.zimoyin.mtool.config.global;


import com.alibaba.fastjson2.JSONObject;
import github.zimoyin.mtool.dao.JsonSerializeUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class H2Config {
    private volatile static H2Config INSTANCE;
    private volatile static H2 h2;

    private H2Config() throws Exception {
        String read = JsonSerializeUtil.read("data/config/gloval/h2.json");
        h2 = JSONObject.parseObject(read, H2.class);
    }

    public static H2Config getInstance() {
        if (INSTANCE == null) synchronized (H2Config.class) {
            if (INSTANCE == null) {
                try {
                    INSTANCE = new H2Config();
                } catch (Exception e) {
                    log.error("无法加载到数据库信息文件", e);
                }
            }
        }
        return INSTANCE;
    }

    public H2 getH2() {
        return h2;
    }

    @Data
    public static class H2 {
        private String user;
        private String password;
        private String jdbc;
        private String driverClass;

        public String getGlobal_jdbc() {
            return jdbc + "/gloval";
        }
    }
}
