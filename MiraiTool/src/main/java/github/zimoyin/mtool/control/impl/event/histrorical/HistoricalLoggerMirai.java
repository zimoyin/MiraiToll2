package github.zimoyin.mtool.control.impl.event.histrorical;

import github.zimoyin.mtool.annotation.Controller;
import github.zimoyin.mtool.annotation.EventType;
import github.zimoyin.mtool.event.HistoricalMessageEvent;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.UserOrBot;
import net.mamoe.mirai.event.EventKt;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.MessageChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Controller
public class HistoricalLoggerMirai {
    private static final Logger logger = LoggerFactory.getLogger(HistoricalLoggerMirai.class);

    /*************************************************我接收消息的日志*************************************************************/
    /**
     * 处理一个群中发送的信息
     */
    @EventType(GroupMessageEvent.class)
    public void Controller(GroupMessageEvent event) {
//        handle((GroupMessageEvent) e);
        StringBuffer buffer = new StringBuffer();
        //获取那个机器人接收到的内容
        long botID = event.getBot().getId();
        //获取哪个群发送的内容
//        String groupName = event.getSubject().getName();
//        String groupName = event.getGroup().getName();
        String groupName = event.getSource().getGroup().getName();
        long groupID = event.getSource().getGroup().getId();
        //获取哪个人发送的消息
        String name = event.getSenderName();
        long id = event.getSender().getId();
        //获取消息链
        MessageChain chain = event.getMessage(); // 可获取到消息内容等, 详细查阅 `GroupMessageEvent`
        //获取当然时间
        long time = new Date().getTime();

        buffer.append("[" + botID + "]").append("[群消息]").append("[" + groupName + "(" + groupID + ")]").append("[" + name + "(" + id + ")] ").append(":");

        String chain1 = new HistoricalMessageInfo().getChain(chain);
//        logger.debug("  :" + buffer.append(chain1));
    }


    /**
     * 处理朋友发来的信息 FriendMessageEvent
     */
    @EventType(FriendMessageEvent.class)
    public void handle(FriendMessageEvent event) {
        long botID = event.getBot().getId();//那个机器人接收的的

        long id = event.getSender().getId();//朋友ID
        String senderName = event.getSenderName();//会话名称(朋友名称)
        String chain = new HistoricalMessageInfo().getChain(event.getMessage());//发送的信息,放到log类里解析成日志形式

        //构建日志
        StringBuffer buffer = new StringBuffer();
        buffer.append(": [" + botID + "]").append("[好友消息]").append("[" + senderName + "(" + id + ")]  :").append(chain);
        //输出日志
//        logger.debug(buffer.toString());
    }

    /**
     * 陌生人消息接收
     *
     * @param event
     */
    @EventType(StrangerMessageEvent.class)
    public void handle(StrangerMessageEvent event) {
        StringBuffer buffer = new StringBuffer();
        //获取那个机器人接收到的内容
        long botID = event.getBot().getId();
        //获取对面是谁
        String name = event.getSenderName();
        long id = event.getSender().getId();
        //获取消息链
        MessageChain chain = event.getMessage(); // 可获取到消息内容等, 详细查阅 `GroupMessageEvent`
        //获取当然时间
        long time = new Date().getTime();

        buffer.append("[" + botID + "]").append("[陌生人消息]").append("[" + name + "(" + id + ")] ").append(":");

        String chain1 = new HistoricalMessageInfo().getChain(chain);
//        logger.debug("  :" + buffer.append(chain1));
    }

    /**
     * 临时群会话消息接收
     *
     * @param event
     */
    @EventType(GroupTempMessageEvent.class)
    public void handle(GroupTempMessageEvent event) {
        StringBuffer buffer = new StringBuffer();
        //获取那个机器人接收到的内容
        long botID = event.getBot().getId();
        //获取哪个群发送的内容
        String groupName = event.getSource().getGroup().getName();
        long groupID = event.getSource().getGroup().getId();
        //获取哪个人发送的消息
        String name = event.getSenderName();
        long id = event.getSender().getId();
        //获取消息链
        MessageChain chain = event.getMessage(); // 可获取到消息内容等, 详细查阅 `GroupMessageEvent`
        //获取当然时间
        long time = new Date().getTime();

        buffer.append("[" + botID + "]").append("[临时群会话消息]").append("[" + groupName + "(" + groupID + ")]").append("[" + name + "(" + id + ")] ").append(":");

        String chain1 = new HistoricalMessageInfo().getChain(chain);
//        logger.debug("  :" + buffer.append(chain1));
    }

    /*************************************************我发送消息的日志*************************************************************
     /**
     * 处理被其他设备发送的信息
     */
    @EventType(MessageSyncEvent.class)
    public void handle(MessageSyncEvent event) {
        long botID = event.getBot().getId();//那个机器人发送的
        long id = event.getSubject().getId();
        String senderName = event.getSenderName();
        String chain = new HistoricalMessageInfo().getChain(event.getMessage());//发送的信息

        try {
            senderName = event.getBot().getGroup(id).getName();
        } catch (Exception e) {
            //吞异常，不处理非群异常
        }

        //构建日志
        StringBuffer buffer = new StringBuffer();
        buffer.append("  :[" + botID + "]").append("[其他设备][发送消息][" + senderName + "(" + id + ")]  <- " + event.getBot().getNick() + ":").append(chain);
        //输出日志
//        logger.debug(buffer.toString());
    }

    /**
     * 处理QQ bot发送的信息 MessagePostSendEvent：发送消息后
     */
    @EventType(MessagePostSendEvent.class)
    public void handle(MessagePostSendEvent event) {
        long botID = event.getBot().getId();//那个机器人发送的
        long id = event.getTarget().getId();//发给谁了

        String chain = new HistoricalMessageInfo().getChain(event.getMessage(), false);//发送的信息

        //构建日志
        StringBuffer buffer = new StringBuffer();
        buffer.append("  :[" + botID + "]").append("[Bot][发送消息][" + id + "]  <-  QQ Bot:").append(chain);
        //输出日志
//        logger.debug(buffer.toString());
    }

/***************************************************************  信息撤回 *************************************************************************************/
    /**
     * 撤回信息: 群撤回
     */
    @EventType(MessageRecallEvent.GroupRecall.class)
    public void messageRecall(MessageRecallEvent.GroupRecall event) {

        //撤回人
        UserOrBot author = event.getAuthor();
        // 消息 ids.
        int[] messageIds = event.getMessageIds();

        //获取信息
        HistoricalMessagesSet.Info info = HistoricalMessagesSet.getInstance().get(event.getGroup().getId(), messageIds[0]);
        String log = null;
        MessageChain messageChain = null;
        if (info != null) {
            log = new HistoricalMessageInfo().getChain(info.getMessageChain(), false);
            messageChain = info.getMessageChain();
        }
        //构建发源信息
        HistoricalMessageEvent.Author sp = new HistoricalMessageEvent.Author(author, event.getGroup().getName(), event.getGroup().getId(), author.getNick(), author.getId(), author.getAvatarUrl());
        //广播事件
        HistoricalMessageEvent messageEvent = new HistoricalMessageEvent(messageChain, event.getMessageTime(), event, log, sp);
        EventKt.broadcast(messageEvent);
    }


    /**
     * 撤回信息: 好友撤回
     */
    @EventType(MessageRecallEvent.FriendRecall.class)
    public void messageRecallFriendRecall(MessageRecallEvent.FriendRecall event) {
        //撤回人
        UserOrBot author = event.getAuthor();
        String name = author.getNick();
        long id = author.getId();//发送者id

        //消息 ids.
        int[] messageIds = event.getMessageIds();

        //获取信息
        HistoricalMessagesSet.Info info = HistoricalMessagesSet.getInstance().get(event.getBot().getId(), messageIds[0]);
        String log = new HistoricalMessageInfo().getChain(info.getMessageChain(), false);
        //构建发源信息
        HistoricalMessageEvent.Author sp = new HistoricalMessageEvent.Author(author, name, id, name, id, author.getAvatarUrl());
        //广播事件
        HistoricalMessageEvent messageEvent = new HistoricalMessageEvent(info.getMessageChain(), event.getMessageTime(), event, log, sp);
        EventKt.broadcast(messageEvent);
    }


/***************************************************************  新成员加入群与入群申请 *************************************************************************************/

    /**
     * 成员被邀请加入群
     *
     * @param event
     */
    @EventType(MemberJoinEvent.Invite.class)
    public void invite(MemberJoinEvent.Invite event) {
        long groupID = event.getGroup().getId();
        String groupName = event.getGroup().getName();
        //被邀请人
        long id = event.getUser().getId();
        String name = event.getUser().getNick();
        //邀请人
        long inviteID = event.getInvitor().getId();
        String inviteName = event.getInvitor().getNick();

        //拼接信息
        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + event.getBot().getId() + "]").append("[邀请入群]").append("[" + groupName + "(" + groupID + ")]").append("[邀请人][" + inviteName + "(" + inviteID + ")] ").append(":").append("[" + name + "(" + id + ")] ").append("被邀请进群");


//        logger.debug("  :" + buffer);
    }

    /**
     * 加入群
     *
     * @param event
     */
    @EventType(MemberJoinEvent.Active.class)
    public void active(MemberJoinEvent.Active event) {
        long groupID = event.getGroup().getId();
        String groupName = event.getGroup().getName();
        //加群人
        long id = event.getUser().getId();
        String name = event.getUser().getNick();


        //拼接信息
        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + event.getBot().getId() + "]").append("[加入群]").append("[" + groupName + "(" + groupID + ")]").append(":").append("[" + name + "(" + id + ")] ").append("加入进群聊");


//        logger.debug("  :" + buffer);
    }

    /**
     * 一个账号请求加入群
     */
    @EventType(MemberJoinRequestEvent.class)
    public void mjoin(MemberJoinRequestEvent event) {
        StringBuffer buffer = new StringBuffer();
        //群信息
        long groupID = event.getGroup().getId();
        String groupName = event.getGroup().getName();
        //加群信息
        String message = event.getMessage();
        //加群人信息
        long id = event.getFromId();
        String name = event.getFromNick();

        //拼接信息
        buffer.append("[" + event.getBot().getId() + "]").append("[入群申请]").append("[" + groupName + "(" + groupID + ")]");

        //邀请人
        NormalMember invitor = event.getInvitor();
        if (invitor != null) {
            long inviteID = event.getInvitor().getId();
            String inviteName = event.getInvitor().getNick();
            //拼接信息
            buffer.append("[邀请人][" + inviteName + "(" + inviteID + ")]");
        }

        //拼接信息
        buffer.append("申请: ").append("[" + name + "(" + id + ")]  ").append(message);


//        logger.debug("  :" + buffer);
    }

/***************************************************************  退群 *************************************************************************************/

    /**
     * 被踢出群
     */
    @EventType(MemberLeaveEvent.Kick.class)
    public void kick(MemberLeaveEvent.Kick event) {


        long groupID = event.getGroup().getId();
        String groupName = event.getGroup().getName();
        //被踢群人
        long id = event.getUser().getId();
        String name = event.getUser().getNick();
        //执行人群人
        NormalMember operator = event.getOperator();


        //拼接信息
        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + event.getBot().getId() + "]").append("[踢人]").append("[" + groupName + "(" + groupID + ")]").append("[执行人][" + operator.getNick() + "(" + operator.getId() + ")] ").append(":").append("[" + name + "(" + id + ")] 被踢出群");


//        logger.debug("  :" + buffer);
    }


    /**
     * 主动离开群
     */
    @EventType(MemberLeaveEvent.Quit.class)
    public void kick(MemberLeaveEvent.Quit event) {
        long groupID = event.getGroup().getId();
        String groupName = event.getGroup().getName();
        //退群人
        long id = event.getUser().getId();
        String name = event.getUser().getNick();


        //拼接信息
        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + event.getBot().getId() + "]").append("[离开了群]").append("[" + groupName + "(" + groupID + ")]").append(":").append("[" + name + "(" + id + ")] ");


//        logger.debug("  :" + buffer);
    }


/***************************************************************  机器人被添加好友和好友申请 或 被邀请入群 *************************************************************************************/
    /**
     * 机器人被邀请入群
     */
    @Deprecated
    @EventType(BotInvitedJoinGroupRequestEvent.class)
    public void botInvited(BotInvitedJoinGroupRequestEvent event) {
        //邀请人信息
        long id = event.getInvitorId();
        String name = event.getInvitorNick();

        //群信息
        String groupName = event.getGroupName();
        long groupId = event.getGroupId();

        //拼接信息
        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + event.getBot().getId() + "]").append("[机器人被邀请入群]").append("[" + groupName + "(" + groupId + ")]").append("[邀请人] : [" + name + "(" + id + ")] ");


//        logger.debug("  :" + buffer);
    }

    /**
     * 机器人加入群
     */
    @EventType(BotJoinGroupEvent.class)
    public void botJoinGroup(BotJoinGroupEvent event) {

        //群信息
        String groupName = event.getGroup().getName();
        long groupId = event.getGroupId();

        //拼接信息
        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + event.getBot().getId() + "]").append("[机器人加群]").append(" ： ").append("[" + groupName + "(" + groupId + ")]");


//        logger.debug("  :" + buffer);
    }

    /**
     * 添加了一个好友
     */
    @EventType(FriendAddEvent.class)
    public void friendAdd(FriendAddEvent event) {
        //好友信息
        long id = event.getFriend().getId();
        String name = event.getFriend().getNick();


        //拼接信息
        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + event.getBot().getId() + "]").append(" [机器人添加了位好友]").append(" : ").append("[" + name + "(" + id + ")] ");


//        logger.debug("  :" + buffer);
    }

    /**
     * 请求添加机器人为好友
     */
    @EventType(NewFriendRequestEvent.class)
    public void newFriendRequest(NewFriendRequestEvent event) {
        //好友信息
        long fromId = event.getFromId();
        String fromNick = event.getFromNick();

        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + event.getBot().getId() + "]");


        //来自哪个群
        Group group = event.getFromGroup();
        if (group != null) {
            String groupName = group.getName();
            long groupId = group.getId();
            buffer.append("[来自群][" + fromNick + "(" + fromId + ")] ");
        }


        //请求信息
        String message = event.getMessage();


        //拼接信息
        buffer.append("[" + fromNick + "(" + fromId + ")] ").append("[申请成为好友]").append(": ").append(message);

//        logger.debug("  :" + buffer);
    }


    /***************************************************************  群公告 *************************************************************************************/
    @Deprecated
    @EventType(GroupEntranceAnnouncementChangeEvent.class)
    public void groupEntranceAnnouncementChange(GroupEntranceAnnouncementChangeEvent event) {

        long id = event.getGroup().getId();
        String name = event.getGroup().getName();

        String aNew = event.getNew();
        String origin = event.getOrigin();
        NormalMember operator = event.getOperator();

        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + event.getBot().getId() + "]").append("[群公告]").append("[" + name + "(" + id + ")] ").append("[修改人][" + operator.getNick() + "(" + operator.getId() + ")] ").append(" : ").append(origin).append(aNew);


//        logger.debug("  :" + buffer);
    }


    @EventType(BotOfflineEvent.class)
    public void BotOfflineEvent(BotOfflineEvent event) {
//        logger.warn("  :" + event.getBot() + "  掉线");
//        logger.warn("  :" + "重新连接： " + event.getReconnect());
    }

    @EventType(BotReloginEvent.class)
    public void BotReloginEvent(BotReloginEvent event) {

//        logger.warn("  :" + event.getBot() + "  尝试自动登录.....");
//        logger.warn("  : 原因 :" + event.getCause());
    }

    @EventType(BotOnlineEvent.class)
    public void BotOnlineEvent(BotOnlineEvent event) {
//        logger.warn("  :" + event.getBot() + "  登录成功");
    }
}