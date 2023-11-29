package github.zimoyin.application.control;

import github.zimoyin.mtool.annotation.Controller;
import github.zimoyin.mtool.annotation.EventType;
import github.zimoyin.mtool.util.message.MessageData;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.ServiceMessage;
import net.mamoe.mirai.message.data.SimpleServiceMessage;

@Slf4j
@Controller
public class AppControl {
    @EventType
    public void onEvent(MessageEvent event) {
        SimpleServiceMessage message = MessageData.getSimpleServiceMessage(event);
        if (message == null) return;
        log.warn("监听到一条 SimpleServiceMessage 服务信息(主要字段 id，content): " + message);
    }

    @EventType
    public void onEvent2(MessageEvent event) {
        LightApp message = MessageData.getLightApp(event);
        if (message == null) return;
        log.warn("监听到一条 LightApp 服务信息(主要字段 id，content): " + message.getContent());
    }

    @EventType
    public void onEvent3(MessageEvent event) {
        ServiceMessage message = MessageData.getServiceMessage(event);
        if (message == null) return;
        log.warn("监听到一条 ServiceMessage 服务信息(主要字段 id，content): " + message);
    }
}
