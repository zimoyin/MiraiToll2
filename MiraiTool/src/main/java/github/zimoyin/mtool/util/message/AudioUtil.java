package github.zimoyin.mtool.util.message;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.OfflineAudio;
import net.mamoe.mirai.utils.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;


/**
 * 语音工具：用来格式化语音和发送语音
 */
public class AudioUtil {
    private static Logger logger = LoggerFactory.getLogger(AudioUtil.class);

    public static OfflineAudio getAudio(InputStream inputStream, Group group) {
        ExternalResource externalResource = null;
        try {

            long s = System.currentTimeMillis();
            externalResource = ExternalResource.create(inputStream);
            OfflineAudio offlineAudio = group.uploadAudio(externalResource);
            long e = System.currentTimeMillis();
            logger.debug(" :[系统日志]" + offlineAudio.getFileMd5() + "[MD5] <--获取格式化语音流用时: " + ((e - s) / 1000) + " s");

            //结构问题导致，jar包下语音文件无法上传至服务器
            return offlineAudio;

        } catch (IOException e) {
            logger.error("获取语音时发生了异常");
        } finally {
            if (externalResource != null) {
                try {
                    externalResource.close();
                } catch (IOException e) {
                    logger.error("无法关闭语音资源流");
                }
            }
        }
        return null;
    }


    /**
     * 格式化语音流，使语音符合QQ语音规范
     *
     * @param inputStream 语音流
     * @param event       信息事件
     * @return
     */
    public static OfflineAudio getAudio(InputStream inputStream, MessageEvent event) {
        OfflineAudio offlineAudio = null;
        //判断具体事件类型
        if (event instanceof GroupMessageEvent) {//群
            Group group = ((GroupMessageEvent) event).getGroup();//获取联系人
            offlineAudio = getAudio(inputStream, group);
        }
        if (event instanceof GroupTempMessageEvent) {//临时会话
            Group group = ((GroupTempMessageEvent) event).getGroup();
            offlineAudio = getAudio(inputStream, group);
        }
        if (event instanceof FriendMessageEvent) {//好友
            Friend friend = ((FriendMessageEvent) event).getFriend();
            offlineAudio = getAudio(inputStream, friend);
        }
        return offlineAudio;
    }

    public static OfflineAudio getAudio(InputStream inputStream, Friend friend) {
        ExternalResource externalResource = null;
        try {

            long s = System.currentTimeMillis();
            externalResource = ExternalResource.create(inputStream);
            OfflineAudio offlineAudio = friend.uploadAudio(externalResource);
            long e = System.currentTimeMillis();
            logger.debug(" :[系统日志]" + offlineAudio.getFileMd5() + "[MD5] <--获取格式化语音流用时: " + ((e - s) / 1000) + " s");

            return offlineAudio;
        } catch (IOException e) {
            logger.error("获取语音时发生了异常");
        } finally {
            if (externalResource != null) {
                try {
                    externalResource.close();
                } catch (IOException e) {
                    logger.error("无法关闭语音资源流");
                }
            }
        }
        return null;
    }

    public static void sendAudio(MessageEvent event, InputStream inputStream) {
        //发送语音
        sendAudio(event, getAudio(inputStream, event));
    }

    public static void sendAudio(MessageEvent event, OfflineAudio audio) {
        //发送语音
        //判断具体事件类型
        if (event instanceof GroupMessageEvent) {//群
            Group group = ((GroupMessageEvent) event).getGroup();//获取联系人
            group.sendMessage(audio);//发送消息
        }
        if (event instanceof GroupTempMessageEvent) {//临时会话
            Group group = ((GroupTempMessageEvent) event).getGroup();
            group.sendMessage(audio);
        }
        if (event instanceof FriendMessageEvent) {//好友
            Friend friend = ((FriendMessageEvent) event).getFriend();
            friend.sendMessage(audio);
        }
    }


}
