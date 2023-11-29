package github.zimoyin.mtool.config.global;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import github.zimoyin.mtool.dao.JsonSerializeUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandConfig {
    private static final Logger logger = LoggerFactory.getLogger(LoginConfig.class);
    private static CommandConfig config;
    private CommandConfigInfo commandConfigInfo;

    private CommandConfig() throws Exception {
        String read = JsonSerializeUtil.read("data/config/gloval/command.json");
        //反序列化
        commandConfigInfo = JSONObject.parseObject(read, CommandConfigInfo.class);
    }

    public synchronized static CommandConfig getInstance() {
        if (config == null) {
            try {
                config = new CommandConfig();
            } catch (Exception e) {
                logger.error("无法加载到命令配置文件", e);
            }
        }
        return config;
    }

    public CommandConfigInfo getCommandConfigInfo() {
        return commandConfigInfo;
    }

    @Data
    public static class CommandConfigInfo {
        //命令前缀
        private String commandPrefix;
        //是否需要at
        @JSONField(name = "@")
        private boolean isAT;
        //命令前缀与主语之间是否需要空格
        private boolean space;
    }
}
