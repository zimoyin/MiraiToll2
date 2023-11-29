package github.zimoyin.mtool.command;

import github.zimoyin.mtool.config.global.CommandConfig;
import github.zimoyin.mtool.util.message.GroupFileSystem;
import github.zimoyin.mtool.util.message.MessageData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.IMirai;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.action.Nudge;
import net.mamoe.mirai.message.data.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 命令数据封装
 */
@Data
@Slf4j
public class CommandData {
    /**
     * 在特定时间内对目标群发言的频率
     */
    @Deprecated
    private static final HashMap<Long, Integer> sendCount = new HashMap<Long, Integer>();
    /**
     * 统计3s内的发送频率
     */
    @Deprecated
    private static final long sendTime = 3000L;
    /**
     * 统计发言时间
     */
    @Deprecated
    private static long Time = 0L;
    /**
     * 单位时间内限制发言次数
     */
    @Deprecated
    private static int count = 1;
    /**
     * 是否被AT
     */
    private boolean isAT;
    /**
     * 命令前缀
     */
    private String prefix;
    /**
     * 命令主语
     */
    private String header;
    /**
     * 命令参数（文本）
     */
    private String[] params;
    /**
     * 命令参数（图片）
     */
    private ArrayList<Image> images;


    /**
     * 命令参数（图片）
     */
    private Image image;

    /**
     * 命令原始文本
     */
    private String textMessage;

    /**
     * 命令信息链
     */

    private MessageChain messageChain;

    /**
     * 是否是命令语句
     */
    private boolean isCommand;

    /**
     * 信息事件源
     */
    private MessageEvent event;

    /**
     * 信息发送者ID
     */
    private long senderID;
    /**
     * 信息发送者名称
     */
    private String senderName;

    /**
     * 群对象
     */
    private Group group;

    /**
     * 朋友对象
     */
    private Friend friend;

    /**
     * 会话窗口ID
     */
    private long windowID;
    /**
     * 会话窗口名称
     */
    private String windowName;

    /**
     * 联系人,通常为信息发送者
     */
    private Contact contact;


    /**
     * 该方法初始化的类无法发送信息等主动方法
     */
    public CommandData(MessageChain messageChain) {
        init(messageChain);
    }


    public CommandData(MessageEvent event) {
        try {
            this.event = event;
            init(event.getMessage());
            initGroup();
            initSender();
            initFriend();
            contact = event.getSubject();
        } catch (Exception e) {
            log.error("无法初始一个 CommandData", e);
        }
    }

    private void init(MessageChain chain) {
        this.messageChain = chain;
        this.textMessage = MessageData.getTextMessage(chain);
        if (!CommandParsing.isCommandSubjectParsing(textMessage)) {
            this.isCommand = false;
            return;
        }
        this.isCommand = true;
        this.image = MessageData.getImage(chain);
        this.images = MessageData.getImages(chain);
        initParams();
    }

    private void initSender() {
        this.senderID = event.getSource().getFromId();
        this.senderName = event.getSender().getNick();
    }


    private void initGroup() {
        if (event.getSubject() instanceof Group) {
            Group subject = (Group) event.getSubject();
            this.group = subject;
            this.windowName = group.getName();
            this.windowID = event.getSource().getTargetId();
        }
    }

    private void initFriend() {
        if (event.getSubject() instanceof Friend) {
            this.friend = (Friend) event.getSubject();
            this.windowName = this.friend.getNick();
            this.windowID = this.friend.getId();
        }
    }


    public void initParams(String... command) {
        String[] strings = null;
        if (command == null || command.length == 0) strings = CommandParsing.commandParsing(textMessage);
        else strings = command;
        if (strings == null) log.error("命令主语以及参数皆为 null");
        assert strings != null;
        this.prefix = CommandConfig.getInstance().getCommandConfigInfo().getCommandPrefix();
        this.header = strings[0];
        this.params = new String[strings.length - 1];
        System.arraycopy(strings, 1, params, 0, strings.length - 1);
    }

    public boolean isNotEmptyParams() {
        return params != null && params.length > 0;
    }

    public boolean isEmptyParams() {
        return !isNotEmptyParams();
    }

    public Bot getBot() {
        return event.getBot();
    }

    public long getBotId() {
        return getBot().getId();
    }

    public MessageReceipt<Contact> sendMessage(String message) {
        return contact.sendMessage(message);
    }

    public MessageReceipt<Contact> sendMessage(String message, Object... params) {
        return contact.sendMessage(String.format(message, params));
    }

    public MessageReceipt<Contact> sendMessage(MessageChain chain) {
        return contact.sendMessage(chain);
    }

    public MessageReceipt<Contact> sendMessage(Number number) {
        return contact.sendMessage(String.valueOf(number));
    }

    public MessageReceipt<Contact> sendMessage(Message message) {
        return contact.sendMessage(message);
    }

    /**
     * 允许通过 函数式接口来构建一个信息并发送
     */
    public MessageReceipt<Contact> sendMessage(Consumer<MessageChainBuilder> consumer) {
        MessageChainBuilder messages = new MessageChainBuilder();
        consumer.accept(messages);
        return sendMessage(messages.build());
    }

    /**
     * 发送引用信息
     *
     * @param msg 信息
     */
    public MessageReceipt<Contact> sendQuoteMessage(MessageChain msg) {
        MessageChain chain = new MessageChainBuilder() // 引用收到的消息并回复 "Hi!", 也可以添加图片等更多元素.
                .append(new QuoteReply(event.getMessage()))
                .append(msg)
                .build();
        return sendMessage(chain);
    }

    /**
     * 发送引用信息
     *
     * @param msg 信息
     */
    public MessageReceipt<Contact> sendQuoteMessage(String msg) {
        MessageChain chain = new MessageChainBuilder() // 引用收到的消息并回复 "Hi!", 也可以添加图片等更多元素.
                .append(new QuoteReply(event.getMessage()))
                .append(msg)
                .build();
        return sendMessage(chain);
    }

    /**
     * 发送合并信息: 内部构建函数
     */
    public MessageReceipt<Contact> sendForwardMessage(Consumer<ForwardMessageData> consumer) {
        ForwardMessageData messageData = new ForwardMessageData(this);
        consumer.accept(messageData);
        return sendMessage(messageData.build());
    }

    /**
     * 发送合并信息: 内部构建函数
     */
    public MessageReceipt<Contact> sendForwardMessage(BiConsumer<ForwardMessageBuilder, Contact> consumer) {
        ForwardMessageBuilder messageData = new ForwardMessageBuilder(contact);
        consumer.accept(messageData, contact);
        return sendMessage(messageData.build());
    }

    /**
     * 发送合并信息: 发生已经构建完毕的学习
     */
    public MessageReceipt<Contact> sendForwardMessage(ForwardMessageData messageData) {
        return sendMessage(messageData.build());
    }

    /**
     * 发送合并信息: 发生已经构建完毕的学习
     */
    public MessageReceipt<Contact> sendForwardMessage(ForwardMessageBuilder messageData) {
        return sendMessage(messageData.build());
    }

    /**
     * 发送合并信息: 发生已经构建完毕的学习
     */
    public MessageReceipt<Contact> sendForwardMessage(ForwardMessage messageData) {
        return sendMessage(messageData);
    }

    /**
     * 获取群文件系统
     */
    public GroupFileSystem getFileSystem() {
        return new GroupFileSystem(group);
    }

    /**
     * 上传文件到文件系统的根路径
     *
     * @param file
     * @throws IOException
     */
    public void sendFileToRoot(File file) throws IOException {
        GroupFileSystem system = getFileSystem();
        system.uploadFileToRoot(file, null);
    }

    /**
     * 构建拍一拍
     *
     * @param id
     * @return
     */
    public Nudge nudge(long id) {
        if (friend != null) {
            return friend.nudge();
        }
        NormalMember normalMember = group.get(id);
        if (normalMember != null) {
            return normalMember.nudge();
        }
        if (id == getBotId()) return getBot().nudge();
        return null;
    }

    /**
     * 构建拍一拍
     */
    public Nudge nudge() {
        if (friend != null) {
            return friend.nudge();
        }
        NormalMember normalMember = group.get(getSenderID());
        if (normalMember != null) {
            return normalMember.nudge();
        }
        return getBot().nudge();
    }

    /**
     * 拍一拍
     *
     * @return 是否成功
     */
    public boolean sendNudgeMessage() {
        Nudge nudge = nudge();
        return nudge.sendTo(contact);
    }

    /**
     * 拍一拍
     *
     * @return 是否成功
     */
    public boolean sendNudgeMessage(long id) {
        Nudge nudge = nudge(id);
        if (friend != null) {
            return nudge.sendTo(friend);
        }
        NormalMember normalMember = group.get(id);
        if (normalMember != null) {
            return nudge.sendTo(normalMember);
        }
        if (id == getBotId()) return nudge.sendTo(getBot().getAsFriend());
        return false;
    }

    /**
     * 拍一拍
     *
     * @return 是否成功
     */
    public boolean sendNudgeMessage(Nudge nudge, Contact contact) {
        return nudge.sendTo(contact);
    }

    /**
     * 随机发送戳一戳消息（拍一拍为Nudge）
     */
    public MessageReceipt<Contact> sendPokeMessage() {
        PokeMessage[] values = PokeMessage.values;
        Random random = new Random();
        //随机产生0-(N-1)当中的一个随机整数
        int index = random.nextInt(values.length);
        return sendMessage(values[index]);
    }

    /**
     * 发送戳一戳消息（拍一拍为Nudge）
     *
     * @return
     */
    public MessageReceipt<Contact> sendPokeMessage(PokeMessage packet) {
        return sendMessage(packet);
    }

    /**
     * 撤回信息
     */
    public void recallMessage() {
        IMirai iMirai = Mirai.getInstance();
        iMirai.recallMessage(getBot(), MessageData.getMessageSource(getMessageChain()));
    }

    /**
     * 撤回信息
     */
    public void recallMessage(MessageChain message) {
        IMirai iMirai = Mirai.getInstance();
        iMirai.recallMessage(getBot(), MessageData.getMessageSource(message));
    }

    /**
     * 撤回信息
     */
    public void recallMessage(MessageSource messageSource) {
        IMirai iMirai = Mirai.getInstance();
        iMirai.recallMessage(getBot(), messageSource);
    }

    /**
     * 撤回信息
     */
    public void recallMessage(MessageReceipt<Contact> messageReceipt) {
        messageReceipt.recall();
    }

    /**
     * 撤回信息
     */
    public void recallMessage(MessageReceipt<Contact> messageReceipt, long time) {
        messageReceipt.recallIn(time);
    }


    public String getParam() {
        StringBuilder builder = new StringBuilder();
        for (String param : params) {
            builder.append(param).append(" ");
        }
        return builder.toString();
    }
}
