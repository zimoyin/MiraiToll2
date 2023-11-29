package github.zimoyin.mtool.util;

import github.zimoyin.mtool.annotation.*;
import github.zimoyin.mtool.control.ControllerAbs;
import lombok.Getter;
import lombok.Setter;
import org.h2.tools.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通过反射来查找包下的所有的类
 */
public class FindClassCache {
    private static final Logger logger = LoggerFactory.getLogger(FindClassCache.class);
    @Getter
    private static final List<String> Annotations = new ArrayList<>();
    @Getter
    private static final List<String> SuperClasses = new ArrayList<>();
    /**
     * 黑名单
     */
    private static final HashSet<String> blacklist = new HashSet<String>();
    private static List<String> results;
    private static List<? extends Class<?>> resultsClasses;
    private volatile static FindClassCache INSTANCE;

    static {
//        Annotations.add(Shell.class.getTypeName());
        //TODO: 注解 github.zimoyin.cli.annotation.Shell
        Annotations.add("github.zimoyin.cli.annotation.Shell");
        Annotations.add(Controller.class.getTypeName());
        Annotations.add(Filter.class.getTypeName());
        Annotations.add(Server.class.getTypeName());
        Annotations.add(ServerMain.class.getTypeName());
        Annotations.add(CommandClass.class.getTypeName());
        SuperClasses.add(ControllerAbs.class.getTypeName());
    }

    @Setter
    @Getter
    private String PackagePath = "github.zimoyin";

    private FindClassCache() {
    }

    public static FindClassCache getInstance() {
        if (INSTANCE == null) synchronized (FindClassCache.class) {
            if (INSTANCE == null) INSTANCE = new FindClassCache();
        }
        return INSTANCE;
    }

    /**
     * 判断类是否符合框架的需要
     */
    private static boolean filter(String cls) {
        try {
            StateClassVisitorUtil.ClassVisitor visitor = StateClassVisitorUtil.getVisitor(cls);
            List<String> annotations = visitor.getClassAnnotations();
            List<String> superClasses = visitor.getSuperClass();
            boolean hasAnts = annotations.stream().anyMatch(Annotations::contains);
            boolean hasSupClass = superClasses.stream().anyMatch(SuperClasses::contains);
            return hasAnts || hasSupClass;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回扫描的默认位置的结果集
     */
    public List<String> getResults() {
        if (results == null) {
            results = new ArrayList<String>();
            results = FindClass.getClazzNameForURL(PackagePath, true);
            results = results.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        return results;
    }

    /**
     * 返回扫描的默认位置的结果集（过滤板）
     */
    public List<? extends Class<?>> getFilterResultsToClass() {
        if (resultsClasses == null) {
            resultsClasses = getResults().stream()
                    .filter(this::isBlacklist)
                    //只加载指定的类
                    .filter(FindClassCache::filter)
                    .map(this::toClass)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            logger.info("查找到的类结果过滤后可用类的个数为: {}", resultsClasses.size());
        }
        return resultsClasses;
    }

    /**
     * 返回扫描的默认位置的结果集
     */
    public List<? extends Class<?>> getResultsToClass() {
        if (resultsClasses == null) {
            resultsClasses = getResults().stream()
                    .filter(this::isBlacklist)

                    .map(this::toClass)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return resultsClasses;
    }

    /**
     * 将类路径转为类实例
     */
    private Class<?> toClass(String cls) {
        try {
            return Class.forName(cls);
        } catch (ClassNotFoundException e) {
            logger.warn("无法将此类路径加载成 Class 实例: {}", cls, e);
        }
        return null;
    }

    /**
     * 初始化加载类的黑名单
     */
    private boolean isBlacklist(String s) {
        return !blacklist.contains(s);
    }

    /**
     * 添加黑名单，不支持正则
     *
     * @param name 不进行加载的类，通常用于防止加载引入了外部依赖的类
     */
    public void setBlacklist(String name) {
        blacklist.add(name);
    }
}
