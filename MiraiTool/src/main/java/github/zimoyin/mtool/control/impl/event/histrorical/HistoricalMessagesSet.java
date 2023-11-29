package github.zimoyin.mtool.control.impl.event.histrorical;


import github.zimoyin.mtool.util.message.MessageData;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 信息集合类
 * 维护用户发送的信息，用以撤回定位
 */
public class HistoricalMessagesSet {
    private static final long TimerTask = 10 * 60 * 1000;// 10 分钟
    private static HistoricalMessagesSet obj = null;
    //联系人id
//    ArrayList<HashMap<MessageSource,MessageChain>> a = new ArrayList<>();
    private final HashMap<Long, MessageInfoSet> messageMap = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(HistoricalMessagesSet.class);

    private HistoricalMessagesSet() {
        //单例
    }

    public static synchronized HistoricalMessagesSet getInstance() {
        if (obj == null) {
            obj = new HistoricalMessagesSet();
            //每  TimerTask 分钟就自动维护一次
            Timer timer = new Timer("Timer-MessagesSetAutoMaintain");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    obj.automaticMaintain();
                }
            }, 0, TimerTask);
        }
        return obj;
    }

    /**
     * 自动维护集合
     */
    private synchronized void automaticMaintain() {
        try {
            int count = 0;
            int count2 = 0;
            //删除commands注册的cls下所有命令
            for (Iterator<Map.Entry<Long, MessageInfoSet>> it = messageMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Long, MessageInfoSet> next = it.next();
                MessageInfoSet value = next.getValue();

                int i = value.getList().size();
                //删除事件大于5分钟的Info
                value.getList().removeIf(info -> ((System.currentTimeMillis() / 1000 - info.getTime()) > 5 * 60));
                count = count + (i - value.getList().size());
                count2 = count2 + value.getList().size();
            }
            logger.debug(String.format("MessagesSet自动维护完成，移除%s 个过时信息，剩余有效信息%s 个", count, count2));
        } catch (Exception e) {
            logger.error("维护信息表时出现异常", e);
        }

    }

    /**
     * 添加
     *
     * @param id
     * @param chain
     */
    public void set(long id, MessageChain chain) {
        MessageInfoSet messageM = messageMap.get(id);
        if (messageM == null) {
            messageMap.put(id, new MessageInfoSet(chain));
        } else {
            messageM.setMessageChain(chain);
        }
    }

    /**
     * 根据id来获取
     */
    public Info get(long id, int messageID) {
        MessageInfoSet messageInfoSet = messageMap.get(id);
        if (messageInfoSet == null) return null;
        return messageInfoSet.getInfo(messageID);
    }

    /**
     * 根据id来获取
     */
    public MessageChain getMessageChain(long id, int messageID) {
        MessageInfoSet messageInfoSet = messageMap.get(id);
        if (messageInfoSet == null) return null;
        return messageInfoSet.getInfo(messageID).getMessageChain();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class MessageInfoSet {
        private ArrayList<Info> list = new ArrayList<>();

        public MessageInfoSet(MessageChain chain) {
            setMessageChain(chain);
        }

        public MessageInfoSet() {
        }

        public void setMessageChain(MessageChain chain) {
            Info info = new Info(chain);
            list.add(info);
        }

        /**
         * 根据时间戳来获取
         *
         * @param time
         * @return
         */
        public Info getInfo(long time) {
            for (Info info : list) {
                if (info.getTime() == time) {
                    System.out.println(info);
                    return info;
                }
            }
            return null;
        }

        /**
         * 根据id来获取
         */
        public Info getInfo(int id) {
            for (Info info : list) {
                for (int messageId : info.getMessageIds()) {
                    if (id == messageId) {
                        return info;
                    }
                }
            }
            return null;
        }

        public ArrayList<Info> getList() {
            return list;
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class Info {
        private long contact;
        private long groupId;
        private long time;
        private MessageSource source;
        private MessageChain chain;
        /**
         * ### 多 ID 情况
         * 对于单条消息, [ids] 为单元素数组. 对于分片 (一种长消息处理机制) 消息, [ids] 将包含多元素.
         */
        private int[] messageIds;
        //在事件中和在引用中无法保证同一条消息的 [internalIds] 相同.
        private int[] messageInternalIds;

        public Info(MessageChain chain) {

            this.chain = chain;
            this.source = MessageData.getMessageSource(chain);
            this.time = this.source.getTime();
            this.contact = this.source.getFromId();
            this.messageIds = this.source.getIds();
            this.messageInternalIds = this.source.getInternalIds();
            groupId = source.getTargetId();
        }

        public long getContact() {
            return contact;
        }

        public long getGroupId() {
            return groupId;
        }

        public long getTime() {
            return time;
        }

        public MessageSource getMessageSource() {
            return source;
        }

        public MessageChain getMessageChain() {
            return chain;
        }

        public int[] getMessageIds() {
            return messageIds;
        }

        public int[] getMessageInternalIds() {
            return messageInternalIds;
        }

        @Override
        public String toString() {
            return "Info{" +
                    "contact=" + contact +
                    ", groupId=" + groupId +
                    ", time=" + time +
                    ", source=" + source +
                    ", chain=" + chain +
                    ", messageIds=" + Arrays.toString(messageIds) +
                    ", messageInternalIds=" + Arrays.toString(messageInternalIds) +
                    '}';
        }
    }
}
