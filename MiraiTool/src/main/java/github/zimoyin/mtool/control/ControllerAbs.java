package github.zimoyin.mtool.control;

import github.zimoyin.mtool.annotation.ControllerFilter;
import github.zimoyin.mtool.util.FindClassCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * 控制器注册类
 * 注册控制器方法：
 * 1. 继承本类
 * 2. 使用注释：@Controller
 */
public abstract class ControllerAbs {
    private static final Logger logger = LoggerFactory.getLogger(ControllerAbs.class);

    /**
     * 推荐使用注解来声明控制器类（其他方法为  ListeningRegistration.registration(cls); ）
     * 查找本应用的类，并实例化,如果类是ControllerAbs的子类那么实例化的时候会执行构造方法创建监听
     * 注意只有ControllerAbs直系子类才能被加载
     */
    public static void init(Class<?>... classes) {
        //如果传入参数为null，则程序获取类
        if (classes == null || classes.length == 0)
            classes = FindClassCache.getInstance().getFilterResultsToClass().toArray(new Class<?>[0]);
        for (Class<?> cls : classes) {
            //如果是Controller的子类说明是处理器类
            if (cls != null && cls.getSuperclass() != null && ControllerAbs.class.isAssignableFrom(cls.getSuperclass())) {
                //注册事件处理器
                ListeningRegistration.registration(cls);
                //注册事件过滤器
                initControllerFilter(cls);
            }
            //被@Controller修饰就说明是处理器类
            else if (cls != null && cls.isAnnotationPresent(github.zimoyin.mtool.annotation.Controller.class)) {
                ListeningRegistration.registration(cls);
                //注册事件过滤器
                initControllerFilter(cls);
            }
        }
    }

    /**
     * 注册事件过滤器
     */
    public static void initControllerFilter(Class<?> cls) {
        List<Method> list = Arrays.stream(cls.getMethods()).filter(method -> method.isAnnotationPresent(ControllerFilter.class)).collect(Collectors.toList());
        for (Method method : list) {
            ControllerFilterMap instance = ControllerFilterMap.getInstance();
            ControllerFilterMap.ListenerMethod listenerMethod = new ControllerFilterMap.ListenerMethod(cls, method);
            instance.add(listenerMethod);
        }
    }
}
