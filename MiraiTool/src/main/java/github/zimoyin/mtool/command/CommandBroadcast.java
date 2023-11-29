package github.zimoyin.mtool.command;

import github.zimoyin.mtool.annotation.Controller;
import github.zimoyin.mtool.annotation.EventType;
import github.zimoyin.mtool.annotation.ThreadSpace;
import github.zimoyin.mtool.command.filter.CommandFilter;
import github.zimoyin.mtool.config.global.CommandConfig;
import github.zimoyin.mtool.control.ListenerObj;
import github.zimoyin.mtool.control.ListenerSet;
import github.zimoyin.mtool.util.NewThreadPoolUtils;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.event.events.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 命令广播：监听信息事件发生，之后交给 CommandExecute 执行
 */
@Controller
public class CommandBroadcast {
    private static CommandBroadcast instance;
    private final CommandSet<String, CommandObject> set = CommandSet.getInstance();
    private final Logger logger = LoggerFactory.getLogger(CommandBroadcast.class);
    @Setter
    @Getter
    private boolean broadcast = true;
    @Setter
    @Getter
    private boolean broadcastCommand = true;

    /**
     * 从监听器中获取这个类的唯一实例
     */
    public static CommandBroadcast getInstance() {
        if (instance != null) return instance;
        instance = (CommandBroadcast) ListenerSet.getInstance().stream()
                .filter(listenerObj -> listenerObj.getCls().equals(CommandBroadcast.class))
                .map(ListenerObj::getObj)
                .findFirst()
                .orElse(null);
        if (instance == null)
            throw new NullPointerException("CommandBroadcast 为null ，该类未被监听器扫描并建立实例对象");
        return instance;
    }

    /**
     * 当发生 MessageEvent 事件时则对该事件进行广播
     *
     * @param event
     */
    @EventType
    public void MessageEventBroadcast(MessageEvent event) {
        try {
            if (broadcast) broadcast(event);
        } catch (Exception e) {
            logger.error("无法对命令进行广播", e);
        }
    }

    private void broadcast(MessageEvent event) {
        CommandConfig.CommandConfigInfo info = CommandConfig.getInstance().getCommandConfigInfo();
        //命令信息
        CommandData commandData = new CommandData(event);
        if (!commandData.isCommand()) return;//如果该事件没有包含命令主语则退出方法体
        //命令对象: 命令主语和参数之间有空格
        CommandObject commandObject = CommandSet.getInstance().get(commandData);
        if (info.getCommandPrefix().isEmpty() && commandObject == null) {
            return;
        }
        if (commandObject == null && info.isSpace()) {
            logger.warn("无法找到命令: {}", commandData.getHeader());
            return;
        }
        if (commandObject == null) {
            List<String> commandList = CommandSet.getInstance().keySet().stream().filter(name -> commandData.getHeader().indexOf(name) == 0).collect(Collectors.toList());
            if (commandList.size() > 1) logger.warn("匹配到多个符合该{} 命令的命令对象", commandData.getHeader());
            if (commandList.size() == 0) logger.warn("无法找到命令的执行方法: {}", commandData.getHeader());
            if (commandList.size() == 0) return;
            String header = commandList.get(0);
            String text = commandData.getTextMessage().replaceFirst(info.getCommandPrefix() + header, info.getCommandPrefix() + header + " ");
            commandData.setHeader(header);
            commandData.initParams(CommandParsing.commandParsing(text));
            commandObject = CommandSet.getInstance().get(commandData);
        }
        if (commandObject == null) {
            logger.warn("无法找到命令: {}", commandData.getHeader());
            return;
        }
        //执行过滤器
        boolean filter = new CommandFilter(commandData).execute();
        if (!filter) {
            logger.info("拦截命令：{}", commandData.getTextMessage());
            return;
        }
        logger.debug("解析命令：{}", commandData.getTextMessage().replace("\n", "\\n"));

        //如果有这注解就在线程内运行
        ThreadSpace annotation1 = commandObject.getCommandClass().getAnnotation(ThreadSpace.class);
        ThreadSpace annotation = commandObject.getMethod().getAnnotation(ThreadSpace.class);
        //是否允许运行命令
        if (!broadcastCommand) {
            logger.debug("命令广播已停止工作,已拦截此命令: " + commandData.getHeader());
            return;
        }
        //执行
        if (annotation == null && annotation1 == null) execute(commandData);
        else NewThreadPoolUtils.getInstance().execute(() -> execute(commandData));
    }

    private void execute(CommandData commandData) {
        new CommandExecute(commandData).execute();
    }
}
