package github.zimoyin.mtool.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.AbstractEvent;

/**
 * 自定义事件
 */
public abstract class AbstractPrivateEvent extends AbstractEvent {
    private Bot bot;

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }
}
