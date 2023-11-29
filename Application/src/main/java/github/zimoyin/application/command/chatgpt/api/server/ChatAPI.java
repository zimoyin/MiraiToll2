package github.zimoyin.application.command.chatgpt.api.server;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import github.zimoyin.application.command.chatgpt.api.cofig.ChatGPTConfig;
import github.zimoyin.application.command.chatgpt.api.cofig.ChatGPTConfig3;
import github.zimoyin.mtool.util.net.httpclient.HttpClientResult;
import github.zimoyin.mtool.util.net.httpclient.HttpClientUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class ChatAPI {
    private volatile static ChatAPI INSTANCE;
    @Getter
    private final ChatGPTConfig config = new ChatGPTConfig();
    private final ChatGPTConfig3 config3 = new ChatGPTConfig3();
    private final String URL = "https://api.openai.com/v1/completions";
    private final String URL_2 = "https://api.openai.com/v1/chat/completions";
    private final String URL_3 = "https://api.openai.com/dashboard/billing/credit_grants";
    private final HashMap<String, ArrayList<String>> caches = new HashMap<String, ArrayList<String>>();
    @Getter
    private final HashMap<String, Integer> cachesCount = new HashMap<>();

    private ChatAPI() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                log.trace("保存配置文件");
                try {
                    config.save();
                } catch (IOException e) {
                    log.warn("无法保存GPT配置文件", e);
                }
            }
        }, 20 * 1000, 20 * 1000);
    }

    public static ChatAPI getInstance() {
        if (INSTANCE == null) synchronized (ChatAPI.class) {
            if (INSTANCE == null) INSTANCE = new ChatAPI();
        }
        return INSTANCE;
    }

    private HashMap<String, String> buildHeaders() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("Content-Type", "application/json;charset=utf-8");
        hashMap.put("Authorization", "Bearer " + config.getKey());
        return hashMap;
    }

    /**
     * @param name 会话ID
     * @return true 则是符合要求
     */
    public boolean preChat(String name) {
        ArrayList<String> list = caches.get(name);
        if (list == null) return true;
        if (list.size() >= 3) list.clear();
        return cachesCount.get(name) < 30;
    }

    public String chat(String text, String name) throws IOException {
        //计数
        cachesCount.put(name, cachesCount.getOrDefault(name, 1));
        //配置
        UUID uuid = UUID.randomUUID();
        ChatGPTConfig copyConfig = copyChatGPTConfig();
        copyConfig.setPrompt(text);
        copyConfig.setUser(name);
        log.debug("GPT({}) [{}]-> {}", name, uuid, copyConfig.toJson());
        copyConfig.setPrompt(getList(text, name).toString());
        //参数判断
        if (copyConfig.isStream()) {
            throw new IllegalArgumentException("当前API不能接收 Stream 为true的参数");
        }
        //构建参数体
        StringEntity stringEntity = new StringEntity(copyConfig.toJson().toString(), StandardCharsets.UTF_8);
        stringEntity.setContentType("application/json;charset=utf-8");
        stringEntity.setContentEncoding("UTF-8");
//        System.exit(0);
        //响应
        String content;
        try (HttpClientResult httpClientResult = HttpClientUtils.doPost(URL, buildHeaders(), null, stringEntity)) {
            content = httpClientResult.getContent().trim();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.debug("GPT({}) [{}]<- {}", name, uuid, content);
        return content;
    }

    public String chat2(String text, Role roleType) throws IOException {
        String role = roleType.name();
        //计数
        cachesCount.put(role, cachesCount.getOrDefault(role, 1));
        //配置
        UUID uuid = UUID.randomUUID();
        ChatGPTConfig3 copyConfig = copyChatGPTConfig3();
        copyConfig.setUser(role);
        log.debug("GPT({}) [{}]-> {}", role, uuid, copyConfig.toJson());
        JSONArray array = new JSONArray();
        for (String valText : getList(text, role)) {
            JSONObject object = new JSONObject();
            object.put("role", role);
            object.put("content", valText);
            array.add(object);
        }
        copyConfig.setMessages(array);
        //参数判断
        if (copyConfig.isStream()) {
            throw new IllegalArgumentException("当前API不能接收 Stream 为true的参数");
        }
        //构建参数体
        StringEntity stringEntity = new StringEntity(copyConfig.toJson().toString(), StandardCharsets.UTF_8);
        stringEntity.setContentType("application/json;charset=utf-8");
        stringEntity.setContentEncoding("UTF-8");
//        System.exit(0);
        //响应
        String content;
        try (HttpClientResult httpClientResult = HttpClientUtils.doPost(URL_2, buildHeaders(), null, stringEntity)) {
            content = httpClientResult.getContent().trim();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.debug("GPT({}) [{}]<- {}", role, uuid, content);
        return content;
    }

    public ChatGPTConfig getChatGPTConfig() {
        return config;
    }

    public ChatGPTConfig copyChatGPTConfig() {
        return config.clone();
    }

    public ChatGPTConfig3 copyChatGPTConfig3() {
        return config3.clone();
    }

    /**
     * 重置缓存
     *
     * @return 缓存内容
     */
    public ArrayList<String> reset(String name) {
        return caches.remove(name);
    }

    private ArrayList<String> getList(String text, String name) {
        ArrayList<String> list = caches.getOrDefault(name, new ArrayList<String>());
        list.add(text);
        caches.put(name, list);
        return list;
    }

    public JSONObject info() throws IOException {
        HttpClientResult result = HttpClientUtils.doGet(URL_3, buildHeaders(), null);
        return JSONObject.parseObject(result.getContent());
    }

    /**
     * 通常，对话首先使用系统消息进行格式化，然后是交替的用户和助理消息。
     * 系统消息有助于设置助手的行为。在上面的例子中，助手被指示“你是一个有用的助手”。
     * 用户消息有助于指导助手。它们可以由应用程序的最终用户生成，或由开发人员设置为指令。
     * 助手消息帮助存储先前的响应。它们也可以由开发人员编写，以帮助提供所需行为的示例。
     */
    public enum Role {
        system, assistant, user
    }
}
