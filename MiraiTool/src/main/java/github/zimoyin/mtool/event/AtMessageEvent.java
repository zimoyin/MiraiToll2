package github.zimoyin.mtool.event;

import lombok.Getter;
import lombok.ToString;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.OnlineMessageSource;

@ToString
@Getter
public class AtMessageEvent extends AbstractPrivateEvent {
    protected final MessageChain Message;
    protected final User sender;
    protected final String senderName;
    protected final OnlineMessageSource.Incoming source;
    protected final Contact subject;
    protected final int time;

    public AtMessageEvent(Bot bot, MessageEvent event) {
        super(bot);
        this.Message = event.getMessage();
        this.sender = event.getSender();
        this.senderName = event.getSenderName();
        this.source = event.getSource();
        this.subject = event.getSubject();
        this.time = event.getTime();
    }
}
