package github.zimoyin.mtool.event;

import lombok.Getter;
import lombok.ToString;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.network.WrongPasswordException;

@Getter
@ToString
public class BotExceptionEvent extends AbstractPrivateEvent {
    private Exception exception;

    public BotExceptionEvent(Bot bot, Exception exception) {
        super(bot);
    }

    @Getter
    @ToString
    public static class BotLoginExceptionEvent extends BotExceptionEvent {
        public BotLoginExceptionEvent(Bot bot, WrongPasswordException exception) {
            super(bot, exception);
        }
    }
}
