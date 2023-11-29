package github.zimoyin.mtool.util.message;


import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.PermissionDeniedException;
import net.mamoe.mirai.contact.file.RemoteFiles;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.utils.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 格式化可发送的文件 工具类
 *
 * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
 */
@Deprecated
public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);//日志

    @Deprecated
    public static void send(long groupID, FileInfo info) {
        Group group = null;
        //随机使用一个有这个群的机器人上传文件
//        for (Bot bot : Login.getBots()) {
        for (Bot bot : Bot.getInstances()) {
            Group botGroup = bot.getGroup(groupID);
            if (botGroup != null) {
                group = bot.getGroup(groupID);
            }
        }
        send(group, info);
    }

    public static void send(Bot bot, long groupID, FileInfo info) {
        Group group = null;
        Group botGroup = bot.getGroup(groupID);
        if (botGroup != null) {
            group = bot.getGroup(groupID);
            send(group, info);
        }
    }


    public static void send(GroupMessageEvent event, FileInfo info) {
        Group group = event.getGroup();
        send(group, info);
    }


    public static void send(Group group, FileInfo info) {
        ExternalResource externalResource = null;
        try {
            externalResource = ExternalResource.create(info.getInputStream());
            RemoteFiles files = group.getFiles();
            if (info.getCallback() != null && info.getListener() != null)
                files.uploadNewFile(info.getQQServerFilePath(), externalResource, info.getCallback(), info.getListener());
            else if (info.getListener() != null)
                files.uploadNewFile(info.getQQServerFilePath(), externalResource, info.getCallback(), info.getListener());
            else if (info.getCallback() != null)
                files.uploadNewFile(info.getQQServerFilePath(), externalResource, info.getCallback());
            else files.uploadNewFile(info.getQQServerFilePath(), externalResource);
            ;
        } catch (Exception e) {
            logger.error("上传文件时发生了异常", e);
        } finally {
            if (info.getInputStream() != null) {
                try {
                    info.release();
                } catch (IOException e) {
                    logger.error("关闭文件输入流失败");
                }
            }
            if (externalResource != null) {
                try {
                    externalResource.close();
                } catch (IOException e) {
                    logger.error("关闭文件输入流失败");
                }
            }
        }
    }
}
