package github.zimoyin.mtool.dao;

import lombok.Getter;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// elementType = "appender"
@Plugin(name = "BeforeAppender", category = "Core", elementType = "BeforeAppender", printObject = true)
public class BeforeAppender extends AbstractAppender {
    /**
     * 日志执行后执行的方法
     */
    @Getter
    private static final List<Consumer<LogEvent>> beforeLog = new ArrayList<>();

    protected BeforeAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }


    @PluginFactory
    public static BeforeAppender createAppender(@PluginAttribute("name") String name,
                                                @PluginElement("Filter") Filter filter,
                                                @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        return new BeforeAppender(name, filter, layout, ignoreExceptions);
    }

    /**
     * 日志执行后执行的方法
     */
    public static void addBeforeLogger(Consumer<LogEvent> consumer) {
        beforeLog.add(consumer);
    }

    @Override
    public void append(LogEvent event) {
        executeBeforeLog(event);
    }

    private void executeBeforeLog(final LogEvent event) {
        beforeLog.forEach(voidConsumer -> voidConsumer.accept(event));
    }
}
