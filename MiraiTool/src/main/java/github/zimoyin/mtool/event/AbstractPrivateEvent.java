package github.zimoyin.mtool.event;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.event.EventKt;

/**
 * 自定义事件
 */
@Slf4j
public abstract class AbstractPrivateEvent extends AbstractEvent {
    private Bot bot;

    public AbstractPrivateEvent(Bot bot) {
        this.bot = bot;
    }

    public static void Broadcast(AbstractEvent event) {
        EventKt.broadcast(event);
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public void broadcast() {
        EventKt.broadcast(this);
    }
}
