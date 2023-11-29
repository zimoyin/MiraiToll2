package github.zimoyin.application.command;

import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.config.global.CommandConfig;
import net.mamoe.mirai.event.events.MessageEvent;

@CommandClass
public class CommandAIHelp {


    @Command(value = "AI", alias = {"ai"}, description = "AI工具（无参数【注意AI效率极低】）")
    public void ai(MessageEvent event) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("命令格式：命令前缀 命令主语\r\n" +
                "命令前缀：" + CommandConfig.getInstance().getCommandConfigInfo().getCommandPrefix() + "\r\n");
        buffer.append(1).append(".pic AI绘图").append("（参数【英文关键词】 ）").append("\r\n");
        buffer.append(2).append(".big 图片放大x4倍").append("（参数【图片】 ）").append("\r\n");
        buffer.append(2).append(".chat ChatGPT").append("\r\n");


        event.getSubject().sendMessage(buffer.toString());
    }
}
