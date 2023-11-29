package github.zimoyin.cli.command;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class CommandManager extends HashMap<Class<?>, CommandObject> {
    private static final HashMap<String, CommandObject> index = new HashMap<String, CommandObject>();
    private volatile static CommandManager INSTANCE;

    private CommandManager() {
    }

    public static CommandManager getInstance() {
        if (INSTANCE == null) synchronized (CommandManager.class) {
            if (INSTANCE == null) INSTANCE = new CommandManager();
        }
        return INSTANCE;
    }

    public static CommandManager initialize(Class<?>... classes) {
        //加载命令
        try {
            new CommandLoader(classes);
        } catch (Exception e) {
            log.error("无法加载所有命令", e);
        }
        //加载子命令

        return CommandManager.getInstance();
    }

    @Override
    public CommandObject put(Class<?> key, CommandObject value) {
        if (super.containsKey(key)) log.warn("重复添加命令:{}", key);
        put(value);
        return super.put(key, value);
    }

    private void put(CommandObject value) {
        put(value.getName(), value);
        if (value.getAliases() != null) for (String alias : value.getAliases()) {
            put(alias, value);
        }
    }

    private void put(String key, CommandObject value) {
        if (index.containsKey(key)) log.error("重复命令名称:{}", key);
        index.put(key, value);
    }

    public CommandObject putSub(Class<?> key, CommandObject value) {
        if (super.containsKey(key)) log.warn("重复添加的子命令:{}", key);
        if (!value.isSubcommand())
            log.error("该命令不是一个子命令:{}", key, new IllegalArgumentException("该命令不是一个子命令无法被添加"));
        if (value.getParent() == null) log.error("该命令的父命令的引用为null", new NullPointerException("NULL"));
        if (value.getParent() == null || !value.isSubcommand()) return null;
        return super.put(key, value);
    }

    public CommandObject get(String name) {
        return index.get(name);
    }

    public CommandObject get(Class<?> key) {
        return super.get(key);
    }
}
