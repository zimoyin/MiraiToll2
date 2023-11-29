package github.zimoyin.application.command;

import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.command.CommandData;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.SimpleServiceMessage;

/**
 * 卡片
 */
@CommandClass
public class CommandAppCord {
    /**
     * 小程序.
     * <p>
     * 大部分 JSON 消息为此类型, 另外一部分为 [ServiceMessage]
     * content 一般是 json
     */
    @Command(value = "ser-app")
    public void onCommandAppCord(final CommandData data) {
        LightApp lightApp = new LightApp(data.getParam());
        data.sendMessage(lightApp);
    }

    /**
     * 服务消息, 可以是 JSON 消息或 XML 消息.
     * <p>
     * JSON 消息更多情况下通过 [LightApp] 发送.
     * serviceId 目前未知, XML 一般为 60, JSON 一般为 1
     * content 消息内容. 可为 JSON 文本或 XML 文本
     */
    @Command(value = "ser-xml")
    public void onCommandAppCord2(final CommandData data) {
        SimpleServiceMessage lightApp = new SimpleServiceMessage(60, data.getParam());
        data.sendMessage(lightApp);
    }

    @Command(value = "ser-json")
    public void onCommandAppCord3(final CommandData data) {
        SimpleServiceMessage lightApp = new SimpleServiceMessage(1, data.getParam());
        data.sendMessage(lightApp);
    }
}
