package github.zimoyin.mtool.event;

import lombok.Getter;
import lombok.ToString;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.OnlineMessageSource;
import net.mamoe.mirai.message.data.SingleMessage;

import java.util.List;

@ToString
@Getter
public class FileMessageEvent extends AbstractPrivateEvent {
    protected final MessageChain sourceMessage;
    protected final List<SingleMessage> fileMessage;
    protected final User sender;
    protected final String senderName;
    protected final OnlineMessageSource.Incoming source;
    protected final Contact subject;
    protected final int time;

    public FileMessageEvent(Bot bot, MessageChain sourceMessage, List<SingleMessage> fileMessage, User sender, String senderName, OnlineMessageSource.Incoming source, Contact subject, int time) {
        super(bot);
        this.sourceMessage = sourceMessage;
        this.fileMessage = fileMessage;
        this.sender = sender;
        this.senderName = senderName;
        this.source = source;
        this.subject = subject;
        this.time = time;
    }

    public static class ImageMessageEvent extends FileMessageEvent {

        public ImageMessageEvent(Bot bot, MessageChain sourceMessage, List<SingleMessage> fileMessage, User sender, String senderName, OnlineMessageSource.Incoming source, Contact subject, int time) {
            super(bot, sourceMessage, fileMessage, sender, senderName, source, subject, time);
        }
    }


    public static class FileUploadMessageEvent extends FileMessageEvent {

        public FileUploadMessageEvent(Bot bot, MessageChain sourceMessage, List<SingleMessage> fileMessage, User sender, String senderName, OnlineMessageSource.Incoming source, Contact subject, int time) {
            super(bot, sourceMessage, fileMessage, sender, senderName, source, subject, time);
        }
    }

    public static class AudioMessageEvent extends FileMessageEvent {

        public AudioMessageEvent(Bot bot, MessageChain sourceMessage, List<SingleMessage> fileMessage, User sender, String senderName, OnlineMessageSource.Incoming source, Contact subject, int time) {
            super(bot, sourceMessage, fileMessage, sender, senderName, source, subject, time);
        }
    }
}
