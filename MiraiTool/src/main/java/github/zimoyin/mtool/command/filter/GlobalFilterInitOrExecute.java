package github.zimoyin.mtool.command.filter;

import github.zimoyin.mtool.annotation.Filter;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.util.FindClassCache;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * 全局过滤器加载执行器
 */
@Slf4j
public class GlobalFilterInitOrExecute {
    private volatile static GlobalFilterInitOrExecute INSTANCE;
    @Getter
    private final List<Class<?>> GlobalFilterList;

    /**
     * 初始化全局过滤器
     * 如果类实现了 AbstractFilter 为局部过滤器,如果类上还有@Filter 注解则为全局过滤器
     */
    private GlobalFilterInitOrExecute() {
        GlobalFilterList = FindClassCache.getInstance().getFilterResultsToClass().stream()
                .filter(cla -> cla.isAnnotationPresent(Filter.class))//类是过滤器
                .filter(GlobalFilterInitOrExecute::isAssignableFrom)//类为AbstractFilter的直接子类
                .collect(Collectors.toList());
        GlobalFilterList.forEach(aClass -> log.debug("[系统日志]加载全局过滤器类: {}", aClass));
        //打印局部过滤器列表
        FindClassCache.getInstance().getFilterResultsToClass().stream()
                .filter((Predicate<Class<?>>) AbstractFilter.class::isAssignableFrom)
                .filter(cla -> !cla.isAnnotationPresent(Filter.class))//类不是全局过滤器
                .forEach((Consumer<Class<?>>) aClass -> log.debug("[系统日志]检测到局部过滤器类: {}", aClass));
    }


    public static GlobalFilterInitOrExecute getInstance() {
        if (INSTANCE == null) synchronized (GlobalFilterInitOrExecute.class) {
            if (INSTANCE == null) INSTANCE = new GlobalFilterInitOrExecute();
        }
        return INSTANCE;
    }

    private static boolean isAssignableFrom(Class<?> cls) {
        if (!AbstractFilter.class.isAssignableFrom(cls))
            log.warn("{} 未能继承 github.zimoyin.mtool.command.filter.AbstractFilter 类,这将导致过滤器无法被执行", cls);
        else return true;
        return false;
    }

    /**
     * 执行所有的全局过滤器
     *
     * @return 是否放行
     */
    public boolean execute(CommandData data) {
        for (Class<?> cls : GlobalFilterList) {
            try {
                boolean res = (boolean) cls.getMethod("filter", CommandData.class).invoke(cls.newInstance(), data);
                log.debug("全局过滤器 {} 放行: {}", cls, res);
                if (!res) return false;
            } catch (Exception e) {
                log.warn("全局过滤器执行失败，请检查过滤器是否存在一个默认的空构造函数，如没有请创建");
                log.error("无法执行的全局过滤器 {}", cls, e);
            }
        }
        return true;
    }

    /**
     * 添加全局过滤器
     *
     * @param cls 过滤器类
     */
    public boolean addGlobalFilter(Class<?> cls) {
        //是否是局部过滤器
        if (isAssignableFrom(cls)) {
            //是否是全局过滤器
            if (cls.isAnnotationPresent(Filter.class)) {
                log.debug("[系统日志]加载全局过滤器类: {}", cls);
                return this.GlobalFilterList.add(cls);
            } else {
                log.debug("[系统日志]检测到局部过滤器类: {}", cls);
            }
        }
        return false;
    }
}
