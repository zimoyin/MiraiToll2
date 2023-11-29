package github.zimoyin.mtool.dao;

import github.zimoyin.mtool.util.StackTraceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase;
import org.apache.logging.log4j.core.LogEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.function.Consumer;


/**
 * Mirai日志到 log4j 日志的转接口
 * 这是不推荐使用的方法，目前在研究如何使用官方提供的日志接口
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MiraiLog4j extends MiraiLoggerPlatformBase {
    /**
     * 开启后会打印调用日志的类与方法，方便定位非 Mirai 使用被类打印日志的方法。
     * 对于只有 Mirai 使用了本类则不建议 开启，会导致日志冗余。并且对解决问题没有实质效果
     */
    private static final boolean DebugLog = false;
    /**
     * 动态 Logger 根据 不同调用类创建不同的 Logger实例
     */
    private static final boolean Dynamiclogger = true;
    private static final HashMap<String, Logger> Loggers = new HashMap<String, Logger>();
    private static Logger logger = LoggerFactory.getLogger("MiraiLog4j");

    public MiraiLog4j() {
//        Slf4jOutputStream.PrinterToLogger();
    }

    /**
     * 日志执行后执行的方法
     */
    public static void addAfterLogger(Consumer<LogEvent> consumer) {
        AfterLogAppender.addAfterLogger(consumer);
    }


    @Override
    protected void debug0(@Nullable String message) {
        debug0(message, null);
    }

    @Override
    protected void verbose0(@Nullable String message) {
        verbose0(message, null);
    }

    /**
     * 日志的标记. 在 Mirai 中, identity 可为
     * - "Bot"
     * - "BotNetworkHandler"
     * 等.
     * <p>
     * 它只用于帮助调试或统计. 十分建议清晰定义 identity
     */
    @Nullable
    @Override
    public String getIdentity() {
        return "MiraiLog4j";
    }

    @Override
    protected void debug0(@Nullable String s, @Nullable Throwable throwable) {
//        logger.debug(s);
        getLogger().debug(constructString(s));
    }

    @Override
    protected void error0(@Nullable String s, @Nullable Throwable throwable) {
//        logger.error(s);
        getLogger().error(constructString(s));
    }

    @Override
    protected void info0(@Nullable String s, @Nullable Throwable throwable) {
        //过滤日志信息
//        switch (s) {
//            case "Loaded account secrets from local cache.":
//                return;
//            case "Saved account secrets to local cache for fast login.":
//                return;
//            case "Login successful.":
//                return;
//            case "Bot login successful.":
//                return;
//        }
//        logger.info(s);
        getLogger().info(constructString(s));
    }

    /**
     * 记录一个 `verbose` 级别的日志.
     * 无关紧要的, 经常大量输出的日志应使用它.
     * 这里会让 debug 进行输出
     */
    @Override
    protected void verbose0(@Nullable String s, @Nullable Throwable throwable) {
//        logger.debug(s);
        getLogger().debug(constructString(s));
    }

    @Override
    protected void warning0(@Nullable String s, @Nullable Throwable throwable) {
//        logger.warn(s);
        getLogger().warn(constructString(s));
    }

    private Logger getLogger() {
        if (!Dynamiclogger) return logger;
        String className = getFormatString(getClassName(), getIdentity());

        Logger logger0 = Loggers.get(className);
        if (logger0 == null) {
            logger0 = LoggerFactory.getLogger(className);
            Loggers.put(className, logger0);
        }
        return logger0;
    }

    private String constructString(String str) {
        if (!DebugLog) return str;
        return "=> [" + getStr() + "] ---" + str;
    }

    /**
     * 格式化字符串，让字符串长度为110
     *
     * @param str  字符串左侧，与字符串右侧 之间填充空格以此达到 110 长度
     * @param str2 字符串右侧
     * @return
     */
    private String getFormatString(String str, String str2) {
        int length = getClassName().length() + "=>".length() + getIdentity().length();
        StringBuilder builder = new StringBuilder();
        builder.append(str);
//        for (int i = 1; i <= 108 - length; i++) {
//            builder.append(" ");
//        }
        builder.append(" =>");
        builder.append(str2);
        return builder.toString();
    }

    /**
     * 获取调用栈帧全路径
     *
     * @return
     */
    private String getStr() {
        try {
            StackTraceInfo traceInfo = new StackTraceInfo();
            return traceInfo.getClassName() + "." + traceInfo.getMethodName() + "():" + traceInfo.getLineNumber();
        } catch (Exception e) {
            logger.error("[严重] 无法获取到调用日志的方法栈帧");
            return null;
        }
    }

    /**
     * 获取调用栈帧 类名称
     *
     * @return
     */
    private String getClassName() {
        try {
            StackTraceInfo traceInfo = new StackTraceInfo();
            return traceInfo.getClassName() + ":" + traceInfo.getLineNumber();
        } catch (Exception e) {
            logger.error("[严重] 无法获取到调用日志的方法栈帧", e);
            return null;
        }
    }
}
