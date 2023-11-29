package github.zimoyin.mtool.control.impl.event.message;

import github.zimoyin.mtool.annotation.Controller;
import github.zimoyin.mtool.annotation.EventType;
import github.zimoyin.mtool.event.AtMessageEvent;
import github.zimoyin.mtool.util.message.MessageData;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;

@Controller
public class AtMessage {
    @EventType
    public void onMessage(MessageEvent event) {
        At at = MessageData.getAt(event);
        if (at == null) return;
        new AtMessageEvent(event.getBot(), event).broadcast();
    }
}
