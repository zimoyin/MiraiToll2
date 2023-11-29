package github.zimoyin.application.command.chatgpt.api;

import com.alibaba.fastjson2.JSONObject;
import github.zimoyin.application.command.chatgpt.api.server.ChatAPI;
import github.zimoyin.application.server.thesaurus.ThesaurusCenter;
import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.annotation.Filter;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.command.CommandObject;
import github.zimoyin.mtool.command.filter.impl.Level;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@CommandClass
public class CommandChatAPI {

    private long start = System.currentTimeMillis();

    //    @Command(value = "chat", description = "关于ChatGPT的指令")
    public String commandChatHelp() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("chat 指令").append("\n");
        buffer.append("==================").append("\n");
        buffer.append("gpt 参数：访问GPT").append("\n");
        buffer.append("rgpt : 重置会话缓存").append("\n");
        buffer.append("rest : 重置GPT缓存列表").append("\n");
        buffer.append("ginfo : GPT信息").append("\n");
        buffer.append("==================").append("\n");
        return buffer.toString();
    }

    @Command("gpt")
    public String commandChat(CommandData data, CommandObject commandObject) {
        if (System.currentTimeMillis() - start > 24 * 60 * 60 * 1000) {
            start = System.currentTimeMillis();
            ChatAPI.getInstance().getCachesCount().clear();
            log.info("ChatGPT： 正在重置缓存数值列表");
        }
        String param = data.getParam();
        //随机返回一个数据库里面都词条
        ThesaurusCenter instance = ThesaurusCenter.getInstance();
        String sv = instance.getEntries(param.trim()).stream().findFirst().orElse(null);
        if (sv != null) return sv;
        //参数校验
        if (param.isEmpty()) {
            return "参数不合法，请保持参数的长度在 1-300 之间";
        }
        if (!ChatAPI.getInstance().preChat(data.getWindowID() + "-" + data.getSenderID())) {
            return "访问失败,请重置会话缓存后再试";
        }
        String text = "访问失败,无法连接至 ChatGPT 服务器";
        try {
            String chat = ChatAPI.getInstance().chat(param, data.getWindowID() + "-" + data.getSenderID());
            text = JSONObject.parseObject(chat).getJSONArray("choices").getJSONObject(0).get("text").toString().trim();
            instance.addEntries(param.trim(), text.trim());
            return text;
        } catch (IOException e) {
            log.error("与Chat API 交流时产生异常", e);
            return "抱歉无法访问到ChatGPT API";
        }
    }

    @Command("gpt3")
    public String commandChat2(CommandData data, CommandObject commandObject) {
        if (System.currentTimeMillis() - start > 24 * 60 * 60 * 1000) {
            start = System.currentTimeMillis();
            ChatAPI.getInstance().getCachesCount().clear();
            log.info("ChatGPT： 正在重置缓存数值列表");
        }
        String param = data.getParam();
        //随机返回一个数据库里面都词条
        ThesaurusCenter instance = ThesaurusCenter.getInstance();
        String sv = instance.getEntries(param.trim()).stream().findFirst().orElse(null);
        if (sv != null) {
            if (sv.length() >= 300 || sv.contains("\n")) {
                data.sendForwardMessage(forwardMessageData -> {
                    for (String lng : sv.split("\n"))
                        forwardMessageData.append(lng);
                });
            } else {
                return sv;
            }
        }
        //参数校验
        if (param.isEmpty()) {
            return "参数不合法，请保持参数的长度在 1-300 之间";
        }
        if (!ChatAPI.getInstance().preChat(data.getWindowID() + "-" + data.getSenderID())) {
            return "访问失败,请重置会话缓存后再试";
        }
        String text = "访问失败,无法连接至 ChatGPT 服务器";
        try {
            String chat = ChatAPI.getInstance().chat2(param, ChatAPI.Role.user);
            text = JSONObject.parseObject(chat).getJSONArray("choices").getJSONObject(0).getJSONObject("message").get("content").toString().trim();
            instance.addEntries(param.trim(), text.trim());
            if (text.length() >= 300 || text.contains("\n")) {
                String finalText = text;
                data.sendForwardMessage(forwardMessageData -> {
                    for (String lng : finalText.split("\n"))
                        forwardMessageData.append(lng);
                });
            } else {
                return text;
            }
        } catch (IOException e) {
            log.error("与Chat API 交流时产生异常", e);
            return "抱歉无法访问到ChatGPT API";
        }
        return null;
    }

    @Command("rgpt")
    public String commandChatReset(CommandData data) {
        ChatAPI.getInstance().reset(data.getWindowID() + "-" + data.getSenderID());
        return "重置完毕";
    }

    @Command("rest")
    @Filter(value = Level.Root)
    public String commandChatResetRoot(CommandData data) {
        ChatAPI.getInstance().getCachesCount().clear();
        log.info("ChatGPT： 正在重置缓存数值列表");
        return "重置完毕";
    }


    @Deprecated
    @Command("ginfo")
    @Filter(value = Level.Root)
    public String commandChatInfo(CommandData data) throws IOException {
        JSONObject info = ChatAPI.getInstance().info();
        System.out.println(info);
        StringBuffer buffer = new StringBuffer();
        buffer.append("GPT INFO").append("\n");
        buffer.append("==================").append("\n");
        buffer.append("总额度:").append(info.getString("total_granted")).append("\n");
        buffer.append("使用的额度:").append(info.getString("total_used")).append("\n");
        buffer.append("==================").append("\n");
        return buffer.toString();
    }
}
