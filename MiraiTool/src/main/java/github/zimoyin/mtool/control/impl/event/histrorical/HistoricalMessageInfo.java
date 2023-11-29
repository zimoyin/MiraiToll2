package github.zimoyin.mtool.control.impl.event.histrorical;

import github.zimoyin.mtool.util.message.MessageData;
import net.mamoe.mirai.message.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Queue;


public class HistoricalMessageInfo {
    private final Logger logger = LoggerFactory.getLogger(HistoricalMessageInfo.class);

    public String getChain(MessageChain chain, boolean isRecord) {
        //【维护撤回信息集合】是否记录此次的信息
        if (isRecord) {
            //一个会话一个表
            MessageSource messageSource = MessageData.getMessageSource(chain);
            try {
                HistoricalMessagesSet.getInstance().set(messageSource.getTargetId(), chain);
            } catch (NullPointerException e) {
                //机器人自己发送的信息不能获取到发送目的地
                HistoricalMessagesSet.getInstance().set(messageSource.getBotId(), chain);
            }

//        MessagesSet.getInstance().set(messageSource.getFromId(),chain);//一个人一个表
        }

        //以下为学习时留下来的dome代码，由于官方代码无一些期盼的表示形式因此。。。
        StringBuffer buffer = new StringBuffer();
        //解析信息链
        for (SingleMessage singleMessage : chain) {
            if (singleMessage instanceof PlainText) buffer.append(singleMessage.contentToString());
            else if (singleMessage instanceof Queue) buffer.append("[引用]: ").append(singleMessage);
            else if (singleMessage instanceof QuoteReply) {//回复
                MessageSource source = ((QuoteReply) singleMessage).getSource();
                buffer.append(" [回复(" + source.getFromId() + ")'").append(getChain(source.getOriginalMessage(), false) + "']： ");
            } else if (singleMessage instanceof MarketFace || singleMessage instanceof VipFace)
                buffer.append("[表情]").append(singleMessage);
            else if (singleMessage instanceof Face) buffer.append("原生表情" + singleMessage.contentToString());
            else if (singleMessage instanceof Dice) buffer.append("魔法表情: " + singleMessage.toString());
            else if (singleMessage instanceof Image)
                buffer.append("[图片]: ").append(((Image) singleMessage).getImageId());
            else if (singleMessage instanceof Voice) buffer.append("[语音]: ").append(((Voice) singleMessage).getUrl());
            else if (singleMessage instanceof Audio)
                buffer.append("[语音]: ").append(Arrays.toString(((Audio) singleMessage).getExtraData()));
            else if (singleMessage instanceof FlashImage)
                buffer.append("[闪照]: ").append(((FlashImage) singleMessage).getImage().getImageId());
            else if (singleMessage instanceof ShowImageFlag) buffer.append("[秀图]: ").append(singleMessage);
            else if (singleMessage instanceof PokeMessage) buffer.append("[戳一戳]: ").append(singleMessage.toString());
            else if (singleMessage instanceof MusicShare) buffer.append("音乐分享： " + singleMessage.contentToString());
            else if (singleMessage instanceof FileMessage)
                buffer.append("[文件]" + ((FileMessage) singleMessage).getName());
            else if (singleMessage instanceof LightApp) buffer.append("小程序: " + singleMessage.contentToString());
            /**
             * 此处应该展开
             */
            else if (singleMessage instanceof ForwardMessage) buffer.append("[合并信息]: ").append(singleMessage);
            else if (singleMessage instanceof ServiceMessage)
                buffer.append("[服务信息]: " + singleMessage);//SimpleServiceMessage 通常构建[LightApp] 和 [ServiceMessage]
            else if (singleMessage instanceof At) buffer.append(singleMessage.contentToString());//@ 信息
            else if (singleMessage instanceof AtAll) buffer.append("@全体成员");
            ;//双分号闭合
        }
        //如果解析的信息是个空串就说明信息没解析出来
        if (buffer.toString().equals("")) {
            buffer.append("无法解析的消息: \n");
            buffer.append(chain);
            logger.info(buffer.toString());
        }
//        //直接返回信息链的toString
//        return chain.contentToString();
        return buffer.toString();
    }


    /**
     * 解析信息链
     *
     * @param chain
     * @return
     */
    public String getChain(MessageChain chain) {
        return getChain(chain, true);
    }
}
