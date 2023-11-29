package github.zimoyin.mtool.command.filter.impl;

import github.zimoyin.mtool.annotation.Filter;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.command.CommandObject;
import github.zimoyin.mtool.command.CommandSet;
import github.zimoyin.mtool.command.filter.AbstractFilter;
import github.zimoyin.mtool.command.filter.CommandFilter;
import github.zimoyin.mtool.command.filter.FilterTable;
import net.mamoe.mirai.event.events.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命令等级过滤器
 */
@Filter
public class LevelFilter extends AbstractFilter {
    private final CommandSet<String, CommandObject> commands = CommandSet.getInstance();
    private final Logger logger = LoggerFactory.getLogger(CommandFilter.class);
    private Filter annotation;

    @Override
    public boolean filter(CommandData data) {
        CommandObject commandObject = commands.get(data);
        if (commandObject == null) return true;
        annotation = commandObject.getMethod().getAnnotation(Filter.class);
        return LevelFilter(data.getEvent());
    }

    /**
     * 用户等级过滤器
     *
     * @param event
     * @return
     */
    private boolean LevelFilter(MessageEvent event) {
        Level level = annotation.value();
        if (level == null) return true;//如果没有设置权限过滤器就直接放行
        long userID = event.getSource().getFromId();
        long groupID = event.getSource().getTargetId();
        //权限从最高开始查，依次向下
        //查询用户在此群的权限
        Level level0 = FilterTable.getInstance().getLevel(groupID, userID);
        if (level0 == null) return false;
        logger.debug("全局权限过滤器：在 {} 群 的 {} 权限是 {}({})，命令所需的权限为：{}({}),放行: {}", groupID, userID, level0, level0.getLevel(), level, level.getLevel(), level.getLevel() >= level0.getLevel());
        //当用户权限的数值大于命令权限的数值则禁止执行
        return level.getLevel() >= level0.getLevel();
    }
}
