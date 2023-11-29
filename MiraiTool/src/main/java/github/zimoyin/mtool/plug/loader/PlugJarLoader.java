package github.zimoyin.mtool.plug.loader;

import github.zimoyin.mtool.annotation.SkipLoadClass;
import github.zimoyin.mtool.exception.AccessUnreachableClassException;
import github.zimoyin.mtool.util.ClassReaderUtil;
import github.zimoyin.mtool.util.ClassReferenceAnalysisUtil;
import github.zimoyin.mtool.util.FindClass;
import github.zimoyin.mtool.util.StateClassVisitorUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 类加载器： 该类加载器会去扫描Jar所有class，此方法会消耗大量时间与空间
 * 如果使用 getInstance 创建实例，那么加载所有的类都是出自同一个类加载器
 * 如果使用 createInstance 创建实例，那么加载所有的类都是出自不同的类加载器
 * PlugJarLoader.forName() 注意是基于单例的
 * 注意所有的静态方法都是来自 getInstance 的支持
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public final class PlugJarLoader extends SecureClassLoader {

    /**
     * class 所在的根路径可以是一个文件夹也可以是一个 jar  如: D://hello.jar
     * 如果创建了一个非单例加载器，此属性将对非单例加载器不可见
     */
    @Getter
    private static final ArrayList<String> ClassRootPaths = new ArrayList<>();
    /**
     * jar 中的 class 信息缓存，此缓存信息只是用于辅助加载jar中的class
     * 该列表缓存了所有的 class 信息，包括被注以了不可达注解的class
     * 该属性将被用于检测类是否在外部jar(插件)中,如果不在就检测类在jvm系统中是否存在，如果不存在则抛出异常，而不是抛出Error
     *
     * @see PlugJarLoader#loadClass(String)  类加载前，检测是否存在该class
     */
    @Getter
//    private static final ArrayList<String> ClassNameCaches = new ArrayList<>();
    private static final HashMap<String, List<String>> ClassNameCaches = new HashMap<>();
    /**
     * 是否开启双亲委派，默认开启双亲委派
     */
    @Setter
    @Getter
    private static boolean isParentalDelegation = true;
    /**
     * 是否开启类可达扫描，默认开启
     */
    @Setter
    @Getter
    private static boolean isClassReachableScanEnabled = true;
    /**
     * 是否开启类引用可达扫描，默认开启
     */
    @Setter
    @Getter
    private static boolean isClassReferenceReachableScanEnabled = true;
    private volatile static PlugJarLoader INSTANCE;
    /**
     * 创建 PlugJarLoader 实例后赋值，同时不使用 ClassRootPaths
     * 如果创建的一个非单例类加载器则此属性必须复制
     */
    @Getter
    private String path;

    private PlugJarLoader(String jarPath) {
        if (jarPath != null && !jarPath.isEmpty()) addClassRootPath(jarPath);
    }

    public static PlugJarLoader getInstance(String path) {
        if (INSTANCE == null) synchronized (PlugJarLoader.class) {
            if (INSTANCE == null) INSTANCE = new PlugJarLoader(path);
        }
        //如果是单例加载器，并且该路径没有被添加过
        if (path != null && !getClassRootPaths().contains(path)) addClassRootPath(path);
        return INSTANCE;
    }

    public static PlugJarLoader getInstance() {
        return getInstance(null);
    }

    /**
     * 创建一个类对象
     *
     * @param jarPath class 所在的根路径可以是一个文件夹也可以是一个 jar
     */
    public static PlugJarLoader createInstance(String jarPath) {
        PlugJarLoader loader = new PlugJarLoader(jarPath);
        loader.setPath(jarPath);
        return loader;
    }

    /**
     * 加载类
     *
     * @param name 类名称
     */
    public static Class<?> forName(String name) throws ClassNotFoundException {
        return getInstance().loadClass(name);
    }

    /**
     * 添加类的加载路径
     *
     * @param path 类的加载路径，文件夹或jar。<br>
     *             文件夹: 加载该文件夹下的class<br>
     *             jar：加载jar中的class<br>
     */
    public static void addClassRootPath(String path) {
        //判断path是否合法，并缓存 class 的名称
        File file = new File(path);
        if (!file.exists()) throw new NullPointerException("该路径不合法: " + path);
        //添加路径
        getClassRootPaths().add(path);
        //判断是jar 还是文件夹，如果是jar 则直接访问class，如果是文件夹则直接访问class，如果文件夹内有jar 则递归添加
        if (file.isFile()) {
            if (!path.endsWith(".jar")) throw new NullPointerException("该路径不合法: " + path);
            try {
//                ClassNameCaches.addAll(FindClass.getJarClassName(path));
                List<String> list = FindClass.getJarClassName(path);
                ArrayList<String> paths = getClassRootPaths();
                //测试功能：类可达分析
                if (isClassReachableScanEnabled())
                    list.stream().filter(s -> !ClassReferenceAnalysisUtil.ClassesAccessibilityAnalysis(s, paths))
                            .forEach(s -> log.warn("[测试功能][警告] 类不可达,这可能导致异常发生: {}", s));
                if (isClassReferenceReachableScanEnabled())
                    list.stream().filter(s -> !ClassReferenceAnalysisUtil.ClsRefAccAnalysis(s, paths))
                            .forEach(s -> log.warn("[测试功能][警告] 类中存在不可达引用,这可能导致异常发生: {}", s));
                if (isClassReferenceReachableScanEnabled())
                    list.stream().filter(s -> !ClassReferenceAnalysisUtil.ClsRefAccAnalysis(s, paths))
                            .filter(s -> {
                                try {
                                    return PlugJarLoader.getInstance().isSkipLoad(s);
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .forEach(s -> log.warn("[测试功能][警告] 类中存在不可达引用且没有不可达注解,这可能导致异常发生: {}", s));


                //该插件是否被重复加载了
                //如果被重复加载则输出此插件的警告
                //如果没有被重复添加则输出重复的类信息警告
                if (ClassNameCaches.containsKey(path)) {
                    log.warn("该插件({})曾经被加载,你现在正在更新类加载器 ClassNameCaches 列表中关于此插件的信息,如果你是单例加载器请留意此情况，如果你是多例加载器那么你无需在意", path);
                } else {
                    //该类是否被重复加载了
                    list.stream()
                            .filter(s -> getClassNameCachesAll().contains(s))
                            .forEach(name -> log.warn("加载插件({})时，出现了类{},该类已经被其他插件所加载", path, name));

                }
                ClassNameCaches.put(path, list);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("类加载器对文件夹支持不成熟,请指定jar的详细路径");
            //文件夹
            Arrays.stream(Objects.requireNonNull(file.list()))
                    .filter(s -> s.toLowerCase().endsWith(".class"))
//                    .forEach(ClassNameCaches::add);
                    .forEach(s -> {
                        List<String> list = ClassNameCaches.getOrDefault(path, new ArrayList<>());
                        list.add(s);
                        ClassNameCaches.put(path, list);
                    });
            //递归jar
            Arrays.stream(Objects.requireNonNull(file.listFiles()))
                    .filter(file1 -> file1.getName().endsWith(".jar"))
                    .forEach(file2 -> addClassRootPath(file2.getPath()));

        }

    }

    /**
     * 判断类是否存在标准库与应用库，以及jar包中
     *
     * @param name
     * @return
     */
    public static boolean isClassExists(String name) {
        //判断类是否在插件中
        if (getClassNameCachesAll().stream().anyMatch(s -> s.equals(name))) return true;
        //判断类是否在标准库中
        try {
            Class<?> clazz = Class.forName(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断类是否在JDK标准库中
     *
     * @param name
     * @return
     */
    @Deprecated
    public static boolean isJdkClass(String name) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(name);

        if (clazz.isPrimitive()) { // 如果是基本类型，则不在JDK标准库中
            System.out.println("不在JDK标准库中");
            return false;
        } else {
            ClassLoader classLoader = clazz.getClassLoader();
            if (classLoader == null) { // 如果类加载器为null，则表示在JDK标准库中
                System.out.println("在JDK标准库中");
                return true;
            } else {
                String className = clazz.getName().replace('.', '/') + ".class";
                if (classLoader.getResource(className) != null) { // 如果在类加载路径中找到该类文件，则表示在JDK标准库中
                    System.out.println("在JDK标准库中");
                    return true;
                } else {
                    System.out.println("不在JDK标准库中");
                    return false;
                }
            }
        }
    }

    /**
     * 如果实例中没有设置路径则使用全局路径
     * 没有设置路径代表这是个单例类加载器
     *
     * @param loader 类加载器
     */
    public static ArrayList<String> getClassRootPaths(PlugJarLoader loader) {
        if (loader == null) return ClassRootPaths;

        ArrayList<String> list = new ArrayList<>();
        list.add(loader.getPath());

        if (loader.getPath() == null)
            return ClassRootPaths;
        else return list;
    }

    /**
     * 获取所有的缓存class 名称
     *
     * @return
     */
    public static ArrayList<String> getClassNameCachesAll() {
        ArrayList<String> list = new ArrayList<>();
        ClassNameCaches.values().forEach(list::addAll);
        return list;
    }

    /**
     * 扫描路径下所有的class进内存进行处理
     *
     * @param predicate 处理逻辑
     */
    @Deprecated
    public ArrayList<Class<?>> searchClass(Predicate<Class<?>> predicate) throws IOException, ClassNotFoundException {
        ArrayList<Class<?>> list = new ArrayList<>();
        for (String path : getClassRootPaths(this)) list.addAll(searchClass(path, predicate));
        return list;
    }

    /**
     * 扫描路径下所有的class进内存进行处理
     *
     * @param path      路径
     * @param predicate 处理逻辑
     */
    @Deprecated
    public ArrayList<Class<?>> searchClass(String path, Predicate<Class<?>> predicate) throws IOException, ClassNotFoundException {
        ArrayList<Class<?>> list = new ArrayList<>();
        if (new File(path).isDirectory()) return list;
        List<String> jarClassName = FindClass.getJarClassName(path);
        for (String name : jarClassName) {
//            if (!name.equalsIgnoreCase("org.apache.log4j.Appender")) continue;
            try {
                Class<?> cls = loadClass(name);
                if (predicate.test(cls)) list.add(cls);
            } catch (AccessUnreachableClassException ignored) {
                log.debug(ignored.getMessage());
            }
        }
        return list;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
//        log.debug("请求加载类: " + name);
        //通过字节码技术来查看类上是否存在不可达注解，如果存在则返回null
        try {
            if (isSkipLoad(name))
                throw new AccessUnreachableClassException("[警告]应用请求了被添加了不可达注解的类，这是被禁止访问的类: " + name);
        } catch (Exception e) {
            throw new AccessUnreachableClassException("[警告]应用请求了不可达的类: " + name, e);
        }
        //判断是否在库中是否存在该类
        if (!isClassExists(name))
            throw new AccessUnreachableClassException("[警告][类可达检测]应用请求了不可达的类，该类无法被 Plug与JVM中类加载器加载: " + name);
        return super.loadClass(name);
    }

    /**
     * 加载类
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        //通过字节码技术来查看类上是否存在不可达注解，如果存在则返回null
        try {
            if (isSkipLoad(name))
                throw new ClassNotFoundException("[严重]应用疑似越过检测进行请求了被添加了不可达注解的类，这是被禁止访问的类: " + name);
        } catch (Exception e) {
            throw new ClassNotFoundException("[严重]应用疑似越过检测进行请求了不可达的类: " + name, e);
        }
//        log.debug("加载类: " + name);
        //如果为 true 则表示不打破不双亲委派
        if (isParentalDelegation) {
            return super.loadClass(name, resolve);
        }
        Class<?> c = null;
        synchronized (getClassLoadingLock(name)) {
            //先去MyJarLoader缓存查找
            c = findLoadedClass(name);

            //缓存没有就去加载
            if (c == null) {
                //先去我的类加载器所负责的范围加载
                try {
                    c = this.findClass(name);
                } catch (Exception ignored) {
                    //如果我的类加载器无法加载到类，就接着执行，让父类去加载
                    //如果因为我的加载器产生了异常则抛弃掉
                }

                try {
                    //如果我的类加载器没有加载到就让父加载器去加载
                    if (c == null) c = super.loadClass(name, resolve);
                } catch (Exception e) {
                    throw new AccessUnreachableClassException("[严重][ERROR!] 应用疑似越过检测进行请求了不可达的类: " + name, e);
                }
            }

            if (c == null) throw new NoClassDefFoundError("未定义的类: " + name);

        }
        return c;
    }

    /**
     * 在 jarPath 的jar 中 加载类
     *
     * @param name 类的全限定名
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        //2. 获取jar文件里面的class文件的字节数组
        byte[] b = null;
        try {
            b = getClassByte(name);
        } catch (IOException e) {
            throw new ClassNotFoundException("[严重]错误的调用! 由于插件中此类未能添加'不可达类注解(@SkipLoadClass)'导致此类被加载; 未能找到类: " + name, e);
        }

        //3. defineClass()是在jvm内存中定义一个class
        return this.defineClass(name, b, 0, b.length);
    }

    /**
     * 读取class文件
     */
    private byte[] getClassByte(String name) throws IOException {
        return ClassReaderUtil.readClassBytes(name, getClassRootPaths(this).toArray(new String[0]), false);
    }

    /**
     * 是否跳过加载
     *
     * @param name 跳过加载的类
     */
    private boolean isSkipLoad(String name) throws ClassNotFoundException {
        //判断类是否在jar中: 如果不在则禁止跳过，否则无法加载jdk中的class
        //class 没有在jar 中，禁止跳过
//        if (ClassNameCaches.stream().noneMatch(s -> s.equalsIgnoreCase(name))) return false;
        if (getClassNameCachesAll().stream().noneMatch(s -> s.equalsIgnoreCase(name))) return false;
        //在jar中
        //读取并分析class
        try {
            //如果该jar中存在 SkipLoadClass 注解则返回true 允许跳过加载该类
            List<String> list = StateClassVisitorUtil.getClassAnnotations(name, getClassRootPaths(this));
            for (String clz : list) if (clz.equals(SkipLoadClass.class.getTypeName())) return true;
        } catch (Exception e) {
            throw new ClassNotFoundException("无法加载类上的注解: " + name, e);
        }
        return false;
    }

    /**
     * 设置类加载器的加载路径，通常是个 JAR
     * 该方法是针对多例类加载器使用的
     *
     * @param path jar 路径
     */
    private void setPath(String path) {
        this.path = path;
    }

    /**
     * 该加载器是否是单例(共享)加载器
     */
    public boolean isSingleLoader() {
        return getPath() == null;
    }


    /**
     * 获取jar中的resources 目录下的文件
     */
    public URL readJarResources(String jarFilePath, String path) throws IOException {
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(path)) {
                    StringBuilder urlStr = new StringBuilder();
                    urlStr.append("jar:file:/");
                    urlStr.append(new File(jarFilePath).getCanonicalPath().replace("\\", "/"));
                    urlStr.append("!");
                    if (entryName.charAt(0) != '/') urlStr.append("/");
                    urlStr.append(entryName);
                    return new URL(urlStr.toString());
                }
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String path) throws IOException {
        List<URL> urls = new ArrayList<>();
        //调用父加载器加载资源
        Enumeration<URL> resources = super.getResources(path);
        while (resources.hasMoreElements()) urls.add(resources.nextElement());
        //通过读取JAR文件 来获取 jar中的resources 目录下的文件
        if (this.path != null && new File(this.path).isFile()) urls.add(readJarResources(this.path, path));
        //扫描路径下所有的Jar文件，通过读取JAR文件 来获取 jar中的resources 目录下的文件
        for (String rootPath : getClassRootPaths()) {
            //然后上面扫描的是这个文件则跳过
            if (this.path != null && this.path.equalsIgnoreCase(rootPath)) continue;
            if (new File(rootPath).isFile()) urls.add(readJarResources(rootPath, path));
        }
        //去除null 、 去重
        urls = urls.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        return Collections.enumeration(urls);
    }


    @Nullable
    @Override
    @Deprecated
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Nullable
    @Override
    @Deprecated
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }
}
