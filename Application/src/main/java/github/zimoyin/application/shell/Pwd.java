package github.zimoyin.application.shell;

import github.zimoyin.cli.annotation.Shell;
import github.zimoyin.cli.command.IShell;
import lombok.Data;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * /botID/windowID
 * 会话路径
 */
@Data
@Shell(value = "pwd", description = "当前路径")
public class Pwd implements IShell {
    private String pwd;
    //    private String location;
    private String name;
    private long WindowsID;
    private String WindowsName;
    private boolean isGroup;
    private Bot bot;
    private long botID;
    private boolean isRoot;
    private int pathCount;

    public Pwd() {
    }

    public Pwd(String name) {
        pwd = name;
        if (name == null || name.isEmpty()) return;
        Path path = Paths.get(name);
        pathCount = path.getNameCount();
        if (pathCount >= 3) throw new IllegalArgumentException("Pwd 命令无法解析的路径: " + pwd);
        if (pathCount == 0) {
            isRoot = true;
        }
        try {
            if (pathCount >= 1) {
                botID = Long.parseLong(path.getName(0).toString());
                bot = Bot.getInstance(botID);
            }
            if (pathCount >= 2) {
                WindowsID = Long.parseLong(path.getName(1).toString());
                Friend friend = bot.getFriend(WindowsID);
                Group group = bot.getGroup(WindowsID);
                if (group != null) {
                    isGroup = true;
                    WindowsName = Objects.requireNonNull(bot.getGroup(WindowsID)).getName();
                }
                if (friend != null) {
                    isGroup = false;
                    WindowsName = Objects.requireNonNull(bot.getFriend(WindowsID)).getNick();
                }
                if (group == null && friend == null)
                    throw new IllegalArgumentException("该参数不是个正确的会话窗口ID: " + WindowsID);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Pwd 命令无法解析的路径: " + pwd, e);
        }
    }

    @Override
    public void execute() {
        System.out.println(pwd());
    }

    public String pwd() {
        Object obj = ShellParametersCentre.getInstance().get("pwd");
        if (obj == null) {
            ShellParametersCentre.getInstance().put("pwd", "/");
            return "/";
        }
        pwd = obj.toString();
        return pwd;
    }

    @Override
    public String toString() {
        return pwd;
    }
}
