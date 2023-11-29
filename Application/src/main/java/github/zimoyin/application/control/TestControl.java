package github.zimoyin.application.control;

import github.zimoyin.mtool.annotation.Controller;
import github.zimoyin.mtool.annotation.ControllerFilter;
import github.zimoyin.mtool.annotation.EventType;
import github.zimoyin.mtool.event.FileMessageEvent;
import github.zimoyin.mtool.event.HistoricalMessageEvent;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessagePreSendEvent;
import net.mamoe.mirai.message.data.MessageChain;

@Slf4j
@Controller
public class TestControl {
    @EventType(HistoricalMessageEvent.class)
    public void onHistoricalMessage(HistoricalMessageEvent event) {
//        System.out.println(event.getAuthor());
    }

    @EventType(FileMessageEvent.class)
    public void onHistoricalMessage(FileMessageEvent event) {
//        System.out.println(event.getSenderName());
    }

    @EventType(MessagePreSendEvent.class)
    public void onMessagePreSendEvent(MessagePreSendEvent event) {
//        MessageChain plus = event.getMessage().plus("");
//        plus.clear();
//        plus.plus("123");
//        event.setMessage(plus);
    }


    @ControllerFilter(GroupMessageEvent.class)
    public boolean a(GroupMessageEvent event){
//        log.warn("过滤器执行....");
        return true;
    }



}
