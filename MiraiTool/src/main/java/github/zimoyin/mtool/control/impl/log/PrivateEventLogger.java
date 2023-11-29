package github.zimoyin.mtool.control.impl.log;

import github.zimoyin.mtool.annotation.Controller;
import github.zimoyin.mtool.dao.MiraiLog4j;
import github.zimoyin.mtool.event.AbstractPrivateEvent;
import net.mamoe.mirai.Bot;

@Controller
public class PrivateEventLogger {
    private MiraiLog4j log = new MiraiLog4j();

    /**
     * 监听自定义事件
     *
     * @param event
     */
//    @EventType
    public void onPrivateEvent(AbstractPrivateEvent event) {
        Bot bot = event.getBot();
        if (bot == null) log = new MiraiLog4j();
        else if (log == null) log = (MiraiLog4j) bot.getLogger();
        log.verbose("Private Event : " + event);
    }
}
