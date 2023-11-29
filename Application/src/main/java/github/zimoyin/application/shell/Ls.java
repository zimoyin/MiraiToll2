package github.zimoyin.application.shell;

import github.zimoyin.cli.annotation.Shell;
import github.zimoyin.cli.command.IShell;
import github.zimoyin.mtool.command.CommandObject;
import github.zimoyin.mtool.command.CommandSet;
import github.zimoyin.mtool.control.ListenerObj;
import github.zimoyin.mtool.control.ListenerSet;
import github.zimoyin.mtool.util.message.GroupFile;
import github.zimoyin.mtool.util.message.GroupFileSystem;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;

import java.util.Objects;
import java.util.function.BiConsumer;

@Shell(value = "ls", description = "当前路径下的内容")
public class Ls implements IShell {
    @Shell.Parameter(value = "-a", description = "输出指定列表的内容", help = "参数：listeners(所有监听),commands(所有命令), a(所有联系人),files(群中所有文件信息),file(所有文件名称)")
    private String name = "-lsa";

    @Override
    public void execute() {
        try {
            execute0();
        } catch (Exception e) {
            System.err.println("执行命令失败: " + e.getClass().getSimpleName() + "  " + e.getMessage());
        }
    }

    private void execute0() {
        ShellParametersCentre centre = ShellParametersCentre.getInstance();
//        centre.put("pwd", "/3619430796/713584481/");
        StringBuffer buffer = new StringBuffer();
        Pwd pwd = new Pwd(centre.getOrDefault("pwd", "/").toString());
        //关于监听的逻辑
        if (name.equalsIgnoreCase("listeners")) {
            for (ListenerObj listenerObj : ListenerSet.getInstance()) {
                System.out.println(listenerObj.getCls().getTypeName());
                for (ListenerObj.ListenerMethod listenerMethod : listenerObj.getListenerMethods()) {
                    System.out.println("\t" + listenerMethod.getMethod().getName() + ": " + listenerMethod.getEventClas().getSimpleName());
                }
            }
            return;
        }
        //关于监听的逻辑
        if (name.equalsIgnoreCase("commands")) {
            CommandSet.getInstance().forEach(new BiConsumer<String, CommandObject>() {
                @Override
                public void accept(String s, CommandObject commandObject) {
                    System.out.println(s + ": \t\t\t\t" + commandObject.getCommandClass().getTypeName());
                }
            });
            return;
        }
        //关于联系人的逻辑
        contacts(buffer, pwd);
    }

    private void contacts(StringBuffer buffer, Pwd pwd) {
        //如果PWD在根则默认所有会话列表
        if (pwd.isRoot()) {
            for (Bot instance : Bot.getInstances()) {
                if (instance == null) continue;
                buffer.append(instance.getNick()).append("[").append(instance.getId()).append("]").append("\n");
                if (name != null && name.equalsIgnoreCase("a")) {
                    getWindows(buffer, instance);
                }
            }
            System.out.println(buffer);
            return;
        }
        //如果路径只有一个,打印该BOT的所有联系人
        if (pwd.getPathCount() == 1) {
            Bot bot = pwd.getBot();
            buffer.append(bot.getNick()).append("[").append(bot.getId()).append("]").append("\n");
            getWindows(buffer, bot);
            System.out.println(buffer);
            return;
        }
        //路径有两个,如果是群则打印所有群友，如果是个人则打印个人信息
        if (pwd.getPathCount() == 2) {
            Bot bot = pwd.getBot();
            buffer.append("BOT: ").append(bot.getNick()).append("[").append(bot.getId()).append("]").append("\n");
            if (pwd.isGroup()) {
                Group group = bot.getGroup(pwd.getWindowsID());
                if (group == null) throw new NullPointerException("无法获取到群组: " + pwd.getWindowsID());
                buffer.append(group.getName()).append("[").append(group.getId()).append("]").append("\n");
                if (name != null && name.equalsIgnoreCase("a")) for (NormalMember member : group.getMembers()) {
                    buffer.append(member.getNick()).append("[").append(member.getId()).append("]").append("\n");
                }
            } else {
                Friend friend = bot.getFriend(pwd.getWindowsID());
                if (friend == null) throw new NullPointerException("无法获取到好友: " + pwd.getWindowsID());
                buffer.append(friend.getNick()).append("[").append(friend.getId()).append("]").append("\n");
            }
            System.out.println(buffer);
        }

        if (name != null && (name.equalsIgnoreCase("files") || name.equalsIgnoreCase("file"))) {
            Bot bot = pwd.getBot();
            GroupFileSystem system = new GroupFileSystem(Objects.requireNonNull(bot.getGroup(pwd.getWindowsID())));
            for (GroupFile file : system.list()) {
                String url = null;
                if (name.equalsIgnoreCase("files")) url = file.getURL();
                System.out.println(file.getPath() + "\t\t\t\t\t[" + url + "]");
                if (file.isDirectory()) {
                    for (GroupFile groupFile : file.list()) {
                        if (name.equalsIgnoreCase("files")) url = groupFile.getURL();
                        System.out.println("\t" + groupFile.getPath() + "\t\t\t\t\t[" + url + "]");
                    }
                }
            }
        }
    }

    private void getWindows(StringBuffer buffer, Bot bot) {
        buffer.append("\t").append("Groups: ").append(bot.getGroups().size()).append("\n");
        for (Group group : bot.getGroups()) {
            buffer.append("\t\t").append(group.getName()).append("[").append(group.getId()).append("]").append("\n");
        }
        buffer.append("\t").append("Friends: ").append(bot.getFriends().size()).append("\n");
        for (Friend friend : bot.getFriends()) {
            buffer.append("\t\t").append(friend.getNick()).append("[").append(friend.getId()).append("]").append("\n");
        }
    }
}
