package github.zimoyin.mtool.config.global;

import com.alibaba.fastjson2.JSONObject;
import github.zimoyin.mtool.dao.JsonSerializeUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

@Data
public class LoginConfig {
    private static final Logger logger = LoggerFactory.getLogger(LoginConfig.class);
    private static LoginConfig loginConfig;
    private static boolean isInitialized;
    private LoginInfo loginInfo;

    private LoginConfig() throws Exception {
        if (!isInitialized) Config.init();
        String read = JsonSerializeUtil.read("./data/config/gloval/login.json");
        loginInfo = JSONObject.parseObject(read, LoginInfo.class);
    }

    public synchronized static LoginConfig getInstance() {
        if (loginConfig == null) {
            try {
                loginConfig = new LoginConfig();
            } catch (Exception e) {
                logger.error("无法加载到登录信息文件,请检查配置文件是否配置正确");
            }
            isInitialized = true;
        }
        return loginConfig;
    }

    @Data
    public static class LoginInfo {
        private String name;
        private HashMap<String, User> user;
    }

    @Data
    public static class User {
        private long id;
        private String password;
    }
}
