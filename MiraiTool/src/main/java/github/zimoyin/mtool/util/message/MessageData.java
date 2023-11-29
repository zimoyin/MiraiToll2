package github.zimoyin.mtool.util.message;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.ArrayList;


/**
 * 从MessageChain或Event中分离出想要的数据类型 和 一些URL等属性
 */
public class MessageData {
    /**
     * 获取文本内容
     */
    public static long getGroupID(MessageEvent event) {
        return event.getSubject().getId();
    }

    /**
     * 获取文本内容
     */
    public static long getUserID(MessageEvent event) {
        return event.getSource().getFromId();
    }

    /**
     * 获取文本内容
     */
    public static String getUserName(MessageEvent event) {
        return event.getSender().getNick();
    }


    /**
     * 获取文本内容
     *
     * @param chain
     * @return
     */
    public static PlainText getPlainText(MessageChain chain) {
        return (PlainText) chain.stream().filter(PlainText.class::isInstance).findFirst().orElse(null);
    }


    /**
     * 获取文本内容
     *
     * @param event
     * @return
     */
    public static PlainText getPlainText(MessageEvent event) {
        MessageChain chain = event.getMessage();
        return (PlainText) chain.stream().filter(PlainText.class::isInstance).findFirst().orElse(null);
    }

    /**
     * ServiceMessage 服务消息 (XML, JSON)
     * 获取（不稳定）服务消息
     */
    public static SimpleServiceMessage getSimpleServiceMessage(MessageEvent event) {
        MessageChain chain = event.getMessage();
        return (SimpleServiceMessage) chain.stream().filter(SimpleServiceMessage.class::isInstance).findFirst().orElse(null);
    }

    /**
     * ServiceMessage 服务消息 (XML, JSON)
     * 获取（不稳定）服务消息
     */
    public static ServiceMessage getServiceMessage(MessageEvent event) {
        MessageChain chain = event.getMessage();
        return (ServiceMessage) chain.stream().filter(ServiceMessage.class::isInstance).findFirst().orElse(null);
    }

    /**
     * LightApp 小程序 (JSON)
     */
    public static LightApp getLightApp(MessageEvent event) {
        MessageChain chain = event.getMessage();
        return (LightApp) chain.stream().filter(LightApp.class::isInstance).findFirst().orElse(null);
    }

    /**
     * 获取MessageSource
     *
     * @param chain
     * @return
     */
    public static MessageSource getMessageSource(MessageChain chain) {
        return (MessageSource) chain.stream().filter(MessageSource.class::isInstance).findFirst().orElse(null);
    }


    /**
     * 获取MessageSource
     *
     * @param event
     * @return
     */
    public static MessageSource getMessageSource(MessageEvent event) {
        MessageChain chain = event.getMessage();
        return (MessageSource) chain.stream().filter(MessageSource.class::isInstance).findFirst().orElse(null);
    }

    /**
     * 获取At
     *
     * @return
     */
    public static At getAt(MessageEvent event) {
        MessageChain chain = event.getMessage();
        return (At) chain.stream().filter(At.class::isInstance).findFirst().orElse(null);
    }

    /**
     * 获取At
     *
     * @param chain
     * @return
     */
    public static At getAt(MessageChain chain) {
        return (At) chain.stream().filter(At.class::isInstance).findFirst().orElse(null);
    }

    /**
     * 获取文件
     *
     * @return
     */
    public static FileMessage getFileMessage(MessageEvent event) {
        MessageChain chain = event.getMessage();
        return (FileMessage) chain.stream().filter(FileMessage.class::isInstance).findFirst().orElse(null);
    }

    /**
     * 获取文件 URL
     *
     * @return
     */
    @Deprecated
    public static String getFileMessageURL(GroupMessageEvent event) {
        return getFileMessage(event).toRemoteFile(event.getGroup()).getDownloadInfo().getUrl();
    }

    /**
     * 获取文件 URL
     *
     * @return
     */
    public static String getToFileMessageURL(GroupMessageEvent event) {
        return getFileMessage(event).toAbsoluteFile(event.getGroup()).getUrl();
    }

    /**
     * 获取文件
     *
     * @param chain
     * @return
     */
    public static FileMessage getFileMessage(MessageChain chain) {
        return (FileMessage) chain.stream().filter(FileMessage.class::isInstance).findFirst().orElse(null);
    }


    /**
     * 获取信息链中所有的图片
     *
     * @param chain
     * @return
     */
    public static ArrayList<Image> getImages(MessageChain chain) {
        ArrayList<Image> images = new ArrayList<Image>();
        for (SingleMessage singleMessage : chain) {
            if (singleMessage instanceof Image) images.add((Image) singleMessage);
        }
        return images;
    }


    /**
     * 获取图片
     *
     * @param chain
     * @return
     */
    public static Image getImage(MessageChain chain) {
        return (Image) chain.stream().filter(Image.class::isInstance).findFirst().orElse(null);
    }

    /**
     * 获取图片
     *
     * @return
     */
    public static Image getImage(MessageEvent event) {
        MessageChain chain = event.getMessage();
        return (Image) chain.stream().filter(Image.class::isInstance).findFirst().orElse(null);
    }


    /**
     * 获取图片 URL
     *
     * @param chain
     * @return
     */
    public static String getImageURL(MessageChain chain) {
        return ImageUtils.getURL(getImage(chain));
    }

    /**
     * 获取图片 URL
     *
     * @return
     */
    public static String getImageURL(MessageEvent event) {
        return ImageUtils.getURL(getImage(event));
    }

    /**
     * 获取图片 URL
     *
     * @return
     */
    public static String getImageURL(Image event) {
        return ImageUtils.getURL(event);
    }


    /**
     * 获取所有的文本信息
     *
     * @return
     */
    public static String getTextMessage(MessageChain chain) {
        StringBuffer buffer = new StringBuffer();
        //解析信息链
        for (SingleMessage singleMessage : chain) {
            if (singleMessage instanceof PlainText) buffer.append(singleMessage.contentToString());
        }
        return buffer.toString();
    }


    /**
     * 获取所有的文本信息
     *
     * @return
     */
    public static String getTextMessage(MessageEvent event) {
        MessageChain chain = event.getMessage();
        StringBuffer buffer = new StringBuffer();
        //解析信息链
        for (SingleMessage singleMessage : chain) {
            if (singleMessage instanceof PlainText) buffer.append(singleMessage.contentToString());
        }
        return buffer.toString();
    }

}
