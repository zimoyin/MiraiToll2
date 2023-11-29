package github.zimoyin.mtool.plug.loader;


import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.annotation.Controller;
import github.zimoyin.mtool.annotation.Filter;
import github.zimoyin.mtool.annotation.SkipLoadClass;
import github.zimoyin.mtool.command.CommandLoadInit;
import github.zimoyin.mtool.command.filter.AbstractFilter;
import github.zimoyin.mtool.command.filter.GlobalFilterInitOrExecute;
import github.zimoyin.mtool.control.ControllerAbs;
import github.zimoyin.mtool.plug.start.PlugStart;
import github.zimoyin.mtool.plug.start.PlugStartSPI;
import github.zimoyin.mtool.util.NewThreadPoolUtils;
import github.zimoyin.mtool.util.StateClassVisitorUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Shell;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * 插件加载器
 */
@Slf4j
public final class PlugLoader {
    /**
     * 存储插件的所有主类
     */
    @Getter
    private static final HashMap<String, ArrayList<Class<?>>> PlugStartClasses = new HashMap<>();
    private volatile static PlugLoader INSTANCE;
    /**
     * 是否是单例类加载器
     * 单例加载器：允许加载外部依赖，允许插件间的联动
     * 多例加载器：插件隔离
     */
    @Setter
    @Getter
    private volatile boolean isSingleLoader = true;

    private PlugLoader() {
        init("./plug");
        //每  TimerTask 分钟就自动维护一次
        Timer timer = new Timer("Timer-PlugLoader");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                PlugLoader.PThread();
            }
        }, 60 * 1000, 10 * 60 * 1000);
    }

    public static PlugLoader getInstance() {
        if (INSTANCE == null) synchronized (PlugLoader.class) {
            if (INSTANCE == null) INSTANCE = new PlugLoader();
        }
        return INSTANCE;
    }

    /**
     * 返回插件以及插件所有的类。注意如果不是插件则不返回，插件-》 有插件主类的则为插件主类
     */
    public static HashMap<String, List<String>> getPlugClasses() {
        HashMap<String, List<String>> map = new HashMap<>();
        for (String key : PlugStartClasses.keySet()) {
            map.put(key, PlugJarLoader.getClassNameCaches().get(key));
        }
        return map;
    }

    /**
     * 检测插件线程,并发出警告
     */
    private static void PThread() {
        List<String> list = PlugLoader.getPlugClasses().values().stream().flatMap(List::stream).toList();
//        log.info("开始检测插件是否创建了线程；线程是不允许在插件创建的，如果有将发出警告!");
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            for (StackTraceElement stackTraceElement : thread.getStackTrace()) {
                if (list.contains(stackTraceElement.getClassName())) {
                    log.warn("[警告][出现禁止操作] Thread " + thread.getName() + " created by " + stackTraceElement.getClassName());
                }
            }
        }
    }

    /**
     * 初始化，扫描plug 文件夹下的插件
     */
    private void init(String path) {
        if (path == null) return;
        File file = new File(path);
        //创建文件夹
        file.mkdirs();
        //扫描文件夹下所有的 jar
        File[] files = file.listFiles(pathname -> {
            String name = pathname.getName();
            int index = name.lastIndexOf(".");
            if (index < 0 || index > name.length() - 1) return false;
            return name.substring(index).equalsIgnoreCase(".jar");
        });
        //递归扫描文件夹
        for (File listFile : Objects.requireNonNull(file.listFiles(File::isDirectory))) init(listFile.getPath());
        //添加jar到集合
        if (files != null) addJar(files);
    }

    /**
     * 添加插件到类加载器中
     *
     * @param file 插件文件
     */
    @Deprecated
    public boolean load(File file) {
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index < 0 || index > name.length() - 1) return false;
        boolean b = name.substring(index).equalsIgnoreCase(".jar");
        if (b) addJar(file);
        return b;
    }

    /**
     * 添加jar 到类加载器
     *
     * @param file jar路径
     */
    private void addJar(File... file) {
        for (File file0 : file) {
            PlugJarLoader.addClassRootPath(file0.getPath());
        }
    }

    /**
     * 开启插件
     */
    public void start() throws IOException {
        Class<?>[] classes = preStart().toArray(new Class[0]);
        //注册监听
        if (classes.length > 0) {
            //注册命令
            CommandLoadInit.init(classes);
            //注册控制器
            ControllerAbs.init(classes);
            //注册shell TODO
            //TODO: 类加载器需要加载在插件中注册shell 命令的类，因此我们需要使用 CLI 中的 new CommandLoader 进行命令注册，classes 则是符合要求类的实例（不光符合shell类的）
//            new CommandLoader(classes);
            //TODO 使用类加载器加载CLI 中的命令加载器
            try {
                Class<?> MainCLI = PlugJarLoader.forName("github.zimoyin.cli.MainCLI");
                //TODO 在插件引用了或者程序引用了该类，是否能够正常通过反射获取该类的实例
                MainCLI.getConstructor(Class[].class).newInstance((Object) classes);
                log.info("加载到 MainCLI");
            } catch (ClassNotFoundException ignored) {
                // ignore
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        //继承了PlugStart 的主类入口
        getPlugStartClasses().forEach((path, value) -> {
            for (Class<?> cls : value) {
                NewThreadPoolUtils.getInstance().execute(() -> {
                    String threadName = Thread.currentThread().getName();
                    Thread.currentThread().setName("plug " + new File(path).getName());
                    try {
                        cls.getMethod("start").invoke(cls.getDeclaredConstructor().newInstance());
                        log.info("启动插件启动类: {}", cls);
                    } catch (Exception e) {
                        log.error("启动插件主类失败: {}", cls, e);
                    }
                    Thread.currentThread().setName(threadName);
                });
            }
        });

        List<String> list = PlugLoader.getPlugClasses().values().stream().flatMap(List::stream).toList();
        //SPI 只有单例的时候才能使用
        if (isSingleLoader)
            PlugServiceLoader.load(PlugStartSPI.class, getLoader(null)).forEach((url, plugStartSPI) -> {
                try {
                    if (!list.contains(plugStartSPI.getClass().getTypeName())) {
                        plugStartSPI.start();
                        log.info("SPI启动插件启动类: {}", plugStartSPI.getClass());
                    } else {
                        log.warn("启动插件启动类要求重复启动已被禁止于SPI启动器: {}", plugStartSPI.getClass());
                    }
                } catch (Exception e) {
                    log.error("SPI启动插件({})主类失败: {}", url, plugStartSPI.getClass(), e);
                }
            });
    }

    /**
     * 扫描出插件的入口类
     *
     * @return 插件中的监听类
     */
    private ArrayList<Class<?>> preStart() {
        //类的信息，用于注册监听等
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        HashMap<String, List<String>> caches0 = PlugJarLoader.getClassNameCaches();
        caches0.forEach((path, caches) -> {
            log.info("类加载器({})加载插件:{}", caches0.hashCode(), path);
            for (String name : caches) {
                try {
                    StateClassVisitorUtil.ClassVisitor visitor = StateClassVisitorUtil.getVisitor(name, Collections.singletonList(path));
                    //类是否是继承了插件入口类
                    boolean isSuperPlugStart = visitor.getSuperClass().stream().anyMatch(s -> s.equals(PlugStart.class.getTypeName()));
                    //类是否是继承了控制器类
                    boolean isSuperController = visitor.getSuperClass().stream().anyMatch(s -> s.equals(PlugStart.class.getTypeName()));
                    //类上是否有Controller注解
                    boolean isController = visitor.getClassAnnotations().stream().anyMatch(s -> s.equals(Controller.class.getTypeName()));
                    //类上是否有命令注解
                    boolean isCommand = visitor.getClassAnnotations().stream().anyMatch(s -> s.equals(CommandClass.class.getTypeName()));
                    //类上是否有Filter注解
                    boolean isFilter = visitor.getClassAnnotations().stream().anyMatch(s -> s.equals(Filter.class.getTypeName()));
                    //类是否继承了AbstractFilter
                    boolean isSuperFilter = visitor.getSuperClass().stream().anyMatch(s -> s.equals(AbstractFilter.class.getTypeName()));
                    //类上是否有Shell注解 TODO 测试是否能够加载到shell
//                    boolean isShell = visitor.getClassAnnotations().stream().anyMatch(s -> s.equals(Shell.class.getTypeName()));
                    boolean isShell = visitor.getClassAnnotations().stream().anyMatch(s -> s.equals(CLIINFO.Shall));
                    //是否存在不可达注解
                    boolean isSkipLoadClass = visitor.getClassAnnotations().stream().anyMatch(s -> s.equals(SkipLoadClass.class.getTypeName()));
                    if (isSkipLoadClass) continue;

                    Class<?> cls = null;
                    //存储主类信息
                    if (isSuperPlugStart) {
                        cls = getLoader(path).loadClass(name);
                        ArrayList<Class<?>> value = PlugStartClasses.getOrDefault(path, new ArrayList<>());
                        value.add(cls);
                        PlugStartClasses.put(path, value);
                    }
                    //注册控制器
                    if (isSuperController || isController) {
                        if (cls == null) cls = getLoader(path).loadClass(name);
                        classes.add(cls);
                    }
                    //注册命令
                    if (isCommand) {
                        if (cls == null) cls = getLoader(path).loadClass(name);
                        classes.add(cls);
                    }
                    //注册shell
                    if (isShell) {
                        if (cls == null) cls = getLoader(path).loadClass(name);
                        classes.add(cls);
                    }
                    //注册过滤器
                    if (isFilter || isSuperFilter) {
                        if (cls == null) cls = getLoader(path).loadClass(name);
                        GlobalFilterInitOrExecute.getInstance().addGlobalFilter(cls);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("无法加载的插件主类: " + name, e);
                }
            }
        });

        return classes;
    }

    private PlugJarLoader getLoader(String path) {
        if (isSingleLoader) return PlugJarLoader.getInstance();
        else return PlugJarLoader.createInstance(path);
    }
}
