package github.zimoyin.cli.shell;

import github.zimoyin.cli.annotation.Shell;
import github.zimoyin.cli.command.CommandManager;
import github.zimoyin.cli.command.CommandObject;
import github.zimoyin.cli.command.IShell;

import java.util.List;

//@Shell(value = "help",description = "帮助",help = "help 其他命令主语(只能是主语，子命令无法使用)")
@Shell(value = "help", description = "帮助")
public class CommandHelp implements IShell {
    @Shell.Parameter(value = "-name", description = "其他命令主语", help = "查看其他命令的用法")
    private String command = null;

    @Override
    public void execute() {
        CommandManager manager = CommandManager.getInstance();
        if (command == null) {
            StringBuffer buffer = new StringBuffer("有关某个命令的详细信息，请键入 HELP 命令名\n");
            manager.forEach((aClass, commandObject) -> {
                String description = commandObject.getDescription();
                String name = commandObject.getName();
                buffer.append(name).append(createSpace(24 - name.length())).append(description).append("\n");
            });
            System.out.println(buffer);
            return;
        }

        CommandObject commandObject = manager.get(command.trim());
        if (commandObject == null) {
            System.err.println("未知命令: " + command);
            return;
        }
        String commandHelp = commandObject.getHelp();
        StringBuffer buffer = new StringBuffer();
        //自构建帮助
        if (commandHelp == null || commandHelp.isEmpty()) {
//            buffer.append(commandObject.getName()).append("/t");
            buffer = buildHelpString(commandObject, buffer, 1);
            System.out.println(buffer);

        }
        //使用命令本身提供的帮助
        else {
            System.out.println(commandHelp);
        }
    }


    private StringBuffer buildHelpString(CommandObject commandObject, StringBuffer buffer, int tab) {
        if (commandObject == null) return buffer;
        List<CommandObject> childCommand = commandObject.getChildCommand();
        buffer.append(commandObject.getName()).append(createTable(tab)).append(commandObject.getDescription()).append("\n");

        //参数
        List<CommandObject.Parameter> parameters = commandObject.getParameters();
        for (CommandObject.Parameter parameter : parameters) {
            buffer.append(createTable(tab + 1)).append(parameter.getName()).append("  ").append(parameter.getDescription()).append("\n");
            if (parameter.getHelp() != null && !parameter.getHelp().isEmpty()) {
                buffer.append(createTable(tab + 2)).append(parameter.getHelp()).append("\n");
            }
            buffer.append("\n");
        }
        //子命令
        for (CommandObject child : childCommand) {
            buffer.append("关于子命令的参数").append("\n");
            buffer.append(createTable(tab + 1));
            buildHelpString(child, buffer, tab);
        }
        return buffer;
    }

    private String createSpace(int count) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < count; i++) {
            buffer.append(" ");
        }
        return buffer.toString();
    }

    private String createTable(int tab) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < tab; i++) {
            buffer.append("\t");
        }
        return buffer.toString();
    }
}
