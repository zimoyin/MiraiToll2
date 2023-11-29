package github.zimoyin.mtool.command;

import lombok.Getter;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter
public class ForwardMessageData {
    private final ForwardMessageBuilder Forward;
    private final CommandData Data;

    public ForwardMessageData(final CommandData data) {
        Forward = new ForwardMessageBuilder(data.getContact());
        this.Data = data;
    }

    public ForwardMessageData append(String msg) {
        append(Data.getBotId(), "bot", msg);
        return this;
    }

    public ForwardMessageData append(long id, String name, String msg) {
        Forward.add(id, name, getPlainText(msg));
        return this;
    }


    public ForwardMessageData append(Message msg) {
        append(Data.getBotId(), "bot", msg);
        return this;
    }

    public ForwardMessageData append(long id, String name, Message msg) {
        Forward.add(id, name, msg);
        return this;
    }

    public ForwardMessageData append(MessageChain chain) {
        append(Data.getBotId(), "bot", chain);
        return this;
    }

    public ForwardMessageData append(long id, String name, MessageChain chain) {
        Forward.add(id, name, chain);
        return this;
    }

    public ForwardMessageData append(Consumer<MessageChainBuilder> consumer) {
        append(Data.getBotId(), "bot", consumer);
        return this;
    }

    public ForwardMessageData append(long id, String name, Consumer<MessageChainBuilder> consumer) {
        MessageChainBuilder messages = new MessageChainBuilder();
        consumer.accept(messages);
        Forward.add(id, name, messages.build());
        return this;
    }

    /**
     * 显示方案
     * ### 移动端
     * 在移动客户端将会显示为卡片
     * <p>
     * - `<title>`: [DisplayStrategy.generateTitle]
     * - `<preview>`: [DisplayStrategy.generatePreview]
     * - `<summary>`: [DisplayStrategy.generateSummary]
     * <p>
     * ```
     * |-------------------------|
     * | <title>                 |
     * | <preview>               |
     * |-------------------------|
     * | <summary>               |
     * |-------------------------|
     * ```
     * <p>
     * 默认显示方案:
     * ```
     * |-------------------------|
     * | 群聊的聊天记录             |
     * | <消息 1>                 |
     * | <消息 2>                 |
     * | <消息 3>                 |
     * |-------------------------|
     * | 查看3条转发消息          |
     * |-------------------------|
     * ```
     * <p>
     * ### PC 端
     * 在部分 PC 端显示为类似移动端的卡片, 在其他 PC 端显示为以下格式
     * ```
     * 鸽子 A 2020/04/23 11:27:54
     * 咕
     * 鸽子 B 2020/04/23 11:27:55
     * 咕
     * 鸽子 C 1970/01/01 08:00:00
     * 咕咕咕
     * ```
     */
    public ForwardMessageData setDisplayStrategy(ForwardMessage.DisplayStrategy displayStrategy) {
        Forward.setDisplayStrategy(displayStrategy);
        return this;
    }

    public ForwardMessage build() {
        return Forward.build();
    }


    private PlainText getPlainText(String msg) {
        return new PlainText(msg);
    }

    public static class MessageNode implements ForwardMessage.INode {

        @NotNull
        @Override
        public MessageChain getMessageChain() {
            return null;
        }

        @Override
        public long getSenderId() {
            return 0;
        }

        @NotNull
        @Override
        public String getSenderName() {
            return null;
        }

        @Override
        public int getTime() {
            return 0;
        }
    }
}
