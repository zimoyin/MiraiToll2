package github.zimoyin.mtool.event;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import net.mamoe.mirai.contact.AvatarSpec;
import net.mamoe.mirai.contact.UserOrBot;
import net.mamoe.mirai.event.events.MessageRecallEvent;
import net.mamoe.mirai.message.data.MessageChain;

@Getter
@ToString
public class HistoricalMessageEvent extends AbstractPrivateEvent {
    /**
     * 信息 为 null 则是无法获取到事件
     */
    private final MessageChain Chain;
    /**
     * 时间（s）
     */
    private final long Time;
    /**
     * 撤回事件
     * 请从这里获取事件发源地（哪个群发生的事件）,注意对于朋友类没有撤回群的信息
     */
    private final MessageRecallEvent MessageRecallEvent;
    /**
     * 日志
     */
    private final String Logstr;
    /**
     * 撤回者
     */
    private final Author author;

    public HistoricalMessageEvent(MessageChain chain,
                                  long time,
                                  net.mamoe.mirai.event.events.MessageRecallEvent messageRecallEvent,
                                  String logstr,
                                  Author author) {
        super(messageRecallEvent.getBot());
        Chain = chain;
        Time = time;
        MessageRecallEvent = messageRecallEvent;
        Logstr = logstr;
        this.author = author;
    }

    @Data
    public static class Author {
        private UserOrBot userOrBot;
        /**
         * 群名称
         */
        private String windowName;
        /**
         * 群id
         */
        private long windowID;
        /**
         * 撤回者名称
         */
        private String name;
        /**
         * 撤回者ID
         */
        private long id;
        private String headUrl;

        public Author(UserOrBot userOrBot,
                      String windowName,
                      long windowID,
                      String name,
                      long id,
                      String headUrl) {
            this.userOrBot = userOrBot;
            this.windowName = windowName;
            this.windowID = windowID;
            this.name = name;
            this.id = id;
            this.headUrl = headUrl;
        }

        public String getHeadUrl(AvatarSpec avatarSpec) {
            return userOrBot.getAvatarUrl(avatarSpec);
        }
    }
}
