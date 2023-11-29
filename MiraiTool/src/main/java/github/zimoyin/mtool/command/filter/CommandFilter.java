package github.zimoyin.mtool.command.filter;


import github.zimoyin.mtool.annotation.Filter;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.command.CommandObject;
import github.zimoyin.mtool.command.CommandSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 执行命令过滤器
 */
public class CommandFilter {
    private final CommandData data;
    private final CommandSet<String, CommandObject> commands = CommandSet.getInstance();
    private final Logger logger = LoggerFactory.getLogger(CommandFilter.class);
    private Filter annotation;


    public CommandFilter(CommandData data) {
        this.data = data;
        init();
    }

    private void init() {
        //命令对象
        CommandObject commandObject = commands.get(data);
        if (commandObject == null) return;
        annotation = commandObject.getMethod().getAnnotation(Filter.class);
    }


    public boolean execute() {
        if (annotation == null) return true;//没有过滤器注解就默认放行
        boolean result = true;
        Class<? extends AbstractFilter>[] filterCls = annotation.filterCls();
        for (Class<? extends AbstractFilter> cls : filterCls) {
            if (cls.equals(AbstractFilter.class)) continue;
            try {
                boolean filter = (boolean) cls.getMethod("filter", CommandData.class).invoke(cls.newInstance(), data);
                result = result && filter;
                logger.debug("局部过滤器:{}  放行: {}", cls, result);
                if (!result) return false;
            } catch (Exception e) {
                logger.error("局部过滤器执行失败", e);
            }
        }

        //当局部过滤器放行后 执行全局过滤器
        if (result) result = GlobalFilterInitOrExecute.getInstance().execute(data);
        return result;
    }
}
