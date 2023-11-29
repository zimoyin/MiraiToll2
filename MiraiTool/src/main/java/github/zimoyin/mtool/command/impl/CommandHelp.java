package github.zimoyin.mtool.command.impl;

import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.command.CommandObject;
import github.zimoyin.mtool.command.CommandSet;
import github.zimoyin.mtool.config.global.CommandConfig;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.PlainText;

import java.util.HashSet;

@CommandClass
public final class CommandHelp {
    private final CommandSet<String, CommandObject> commandSet = CommandSet.getInstance();

    @Command(value = "help", description = "命令帮助信息", help = "[参数]: 其他命令主语 （可为空）")
    public void onCommandHelp(final CommandData data) {
        String[] params = data.getParams();
        if (params.length == 0) createDescription(data);
        else createHelp(data);
    }

    private void createDescription(CommandData data) {
        ForwardMessageBuilder forward = new ForwardMessageBuilder(data.getContact());
        StringBuilder buffer = new StringBuilder();
        CommandConfig config = CommandConfig.getInstance();
        buffer.append("命令格式: ")
                .append(config.getCommandConfigInfo().isAT() ? "@机器人 " : "")
                .append("命令前缀")
                .append(config.getCommandConfigInfo().isSpace() ? "空格" : "")
                .append("命令主语 参数")
                .append("\n");
        buffer.append("示例:  ").append(config.getCommandConfigInfo().getCommandPrefix()).append("help  ").append("help").append("\n");
        buffer.append("==============================").append("\n");
        //命令个数大于 10 个就启用转发数据类型
        int count = 0;
        int find = 10;
        HashSet<CommandObject> valuesSet = new HashSet<CommandObject>();
        for (CommandObject value : commandSet.values()) {
            String description = value.getHelp().getDescription();
            if (valuesSet.contains(value)) continue;
            valuesSet.add(value);
            if (description.isEmpty()) continue;//如果不存在描述就跳过这个命令
            buffer.append(config.getCommandConfigInfo().getCommandPrefix()).append(value.getName()).append(":  ").append(description).append("\n");
            count++;
            if (count % find == 0) {
                forward.add(123, "帮助" + (count / 10), new PlainText(buffer.toString()));
                buffer = new StringBuilder();
            }
        }
        buffer.append("==============================").append("\n");
        buffer.append("20230222");
        forward.add(123, "帮助" + (count % 10), new PlainText(buffer.toString()));
        if (count < find) data.sendMessage(buffer.toString());
        else data.sendMessage(forward.build());
    }

    private void createHelp(CommandData data) {
        StringBuilder buffer = new StringBuilder();
        for (String param : data.getParams()) {
            CommandObject commandObject = commandSet.get(param);
            if (commandObject == null) continue;
            String help = commandObject.getHelp().getHelp();
            if (help.isEmpty()) continue;
            buffer.append(param).append(": ").append("\n\t").append(help).append("\n");
            buffer.append("==============================").append("\n");
        }
        if (buffer.toString().trim().isEmpty()) {
            data.sendMessage("该命令主语没有任何的 help");
            return;
        }
        buffer.append("20221105");
        data.sendMessage(buffer.toString());
    }
}
