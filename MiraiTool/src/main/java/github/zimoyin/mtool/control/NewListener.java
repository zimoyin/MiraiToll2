package github.zimoyin.mtool.control;

import github.zimoyin.mtool.annotation.EventType;
import github.zimoyin.mtool.util.NewThreadPoolUtils;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 创建监听,为命令类或菜单类下的所有的命令都创建一个监听
 */
@Deprecated
public class NewListener {
    //禁止二次创建类对象
    private static HashSet<Class> clss = new HashSet<>();
    //全局监听事件集合  主键是类对象，不保存临时事件监听
    private static HashMap<Class, HashMap<Method, Listener>> listeners = new HashMap<>();
    //全局监听事件集合  主键是类名称，方便插件获取,与listeners功能一样，就个别细节不同
    private static HashMap<String, HashMap<Method, Listener>> listeners2 = new HashMap<>();
    //全局事件对象集合，不保存临时事件监听
    private static HashMap<Class, Object> objs = new HashMap<>();
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    //有处理方法的类对象
    private Object obj = null;
    //时间用于创建控制器对象
    private long thisObjTime = System.currentTimeMillis(); //有处理方法的类对象
    //本次监听事件
    private Listener listener0 = null;
    //临时事件监听对象
    private HashMap<EventTask, Listener> tempListeners = new HashMap<>();

    public NewListener() {
    }

    /**
     * 创建监听
     *
     * @param cls 带有处理方法的类（会通过cls创建对象）
     */
    public NewListener(Class<?> cls) {
        if (clss.contains(cls)) return;//如果类被创建过就禁止二次创建监听
        clss.add(cls);//添加创建记录
        //创建这个类的对象
        try {
            obj = cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(cls + " 实例创建失败,无法对其进行监听方法注册", e);
            return;
        }

        logger.debug(" [系统日志]加载控制器类: " + cls);
        //注册这个类中所有的事件处理方法，让事件监听到信息都调用对应的方法
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            //获取方法上的注解
            EventType annotation = method.getAnnotation(EventType.class);
            if (annotation == null) continue;
            //获取事件类型(获取注解的值)
            Class<? extends Event> value = annotation.value();
            //如果注解的事件类型的值不再是默认就说明，注解的值被修改了，那就是false，这样注册监听的话用的是注解的事件类型而不是方法中定义的事件类型
            if (value.equals(Event.class)) {
                //获取方法参数(事件)的class
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    value = (Class<? extends Event>) parameterTypes[0];
                } else {
                    //如果方法的参数个数不是一个那么就出问题了
                    logger.warn(cls + " 类下 " + method + " 方法的参数个数不等于一，不符合注解描述，以对方法停止事件监听创建");
                    return;
                }
            }
            //创建监听
            newListener0(cls, value, method, -1, false);
        }
    }

    /**
     * 创建监听，如果希望对象唯一那么建议使用它
     *
     * @param cls 处理器类
     * @param obj 处理类的实例对象（newListener不会通过 cls 创建对象，需要从外部接收对象）
     */
    @Deprecated
    public NewListener(Class<?> cls, Object obj) {
        if (clss.contains(cls)) return;//如果类被创建过就禁止二次创建监听
        clss.add(cls);//添加创建记录
        //创建这个类的对象
        this.obj = obj;

        logger.debug(" [系统日志]加载控制器类: " + cls);
        //注册这个类中所有的事件处理方法，让事件监听到信息都调用对应的方法
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            //获取方法上的注解
            EventType annotation = method.getAnnotation(EventType.class);
            if (annotation == null) continue;
            //获取事件类型(获取注解的值)
            Class<? extends Event> value = annotation.value();
            //如果注解的事件类型的值不再是默认就说明，注解的值被修改了，那就是false，这样注册监听的话用的是注解的事件类型而不是方法中定义的事件类型
            if (value.equals(Event.class)) {
                //获取方法参数(事件)的class
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    value = (Class<? extends Event>) parameterTypes[0];
                } else {
                    //如果方法的参数个数不是一个那么就出问题了
                    logger.warn(cls + " 类下 " + method + " 方法的参数个数不等于一，不符合注解描述，以对方法停止事件监听创建");
                    return;
                }
            }
            //创建监听
            newListener0(cls, value, method, -1, false);
        }
    }


    /**
     * 创建监听
     *
     * @param cls   带有处理方法的类
     * @param time  创建对象的间隔事件，单位毫秒(ms)。为0时则每次都创建
     * @param reObj 为true时，每隔一段时间就新创建一次控制器的对象
     */
    @Deprecated
    public NewListener(Class<?> cls, long time, boolean reObj) {
        if (clss.contains(cls)) return;//如果类被创建过就禁止二次创建监听
        clss.add(cls);//添加创建记录
        //创建这个类的对象
        try {
            obj = cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(cls + " 实例创建失败,无法对其进行监听方法注册", e);
            return;
        }


        logger.debug(" [系统日志]加载控制器类: " + cls);

        //注册这个类中所有的事件处理方法，让事件监听到信息都调用对应的方法
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            //获取方法上的注解
            EventType annotation = method.getAnnotation(EventType.class);
            if (annotation == null) continue;
            //获取事件类型(获取注解的值)
            Class<? extends Event> value = annotation.value();
            //如果注解的事件类型的值不再是默认就说明，注解的值被修改了，那就是false，这样注册监听的话用的是注解的事件类型而不是方法中定义的事件类型
            if (value.equals(Event.class)) {
                //获取方法参数(事件)的class
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    value = (Class<? extends Event>) parameterTypes[0];
                } else {
                    //如果方法的参数个数不是一个那么就出问题了
                    logger.warn(cls + " 类下 " + method + " 方法的参数个数不等于一，不符合注解描述，以对方法停止事件监听创建");
                    return;
                }
            }
            //创建监听
            newListener0(cls, value, method, time, reObj);
        }
    }

    /**
     * 返回所有的监听器对象(如果要获取插件的监听对象建议去看PlugRUN)  控制器类：处理方法们
     * 主键是类对象
     *
     * @return
     */
    public static HashMap<Class, HashMap<Method, Listener>> getListeners() {
        return listeners;
    }

    /**
     * 删除一个类下的所有监听
     * 通过 listeners 集合停止并删除监听，listeners2 同步删除已经停止的监听
     *
     * @param cls 监听类
     */
    public static void remove(Class<?> cls) {
        //停止监听
        HashMap<Method, Listener> methodListenerHashMap = listeners.get(cls);
        for (Method method : methodListenerHashMap.keySet()) {
            methodListenerHashMap.get(method).complete();
        }
        //删除注册
        listeners.remove(cls);
        listeners2.remove(cls.getName());
    }

    /**
     * 删除一个类下的所有监听
     * 如果类是插件类加载器，加载的可能回导致 listeners 删除失败
     * 通过 listeners2 集合停止并删除监听，listeners 同步删除已经停止的监听
     *
     * @param clsName 监听类名称
     */
    public static void remove(String clsName) throws ClassNotFoundException {
        //停止监听
        HashMap<Method, Listener> methodListenerHashMap = listeners2.get(clsName);
        for (Method method : methodListenerHashMap.keySet()) {
            methodListenerHashMap.get(method).complete();
        }

        //删除注册
        listeners2.remove(clsName);
        listeners.remove(Class.forName(clsName));
    }


    /**
     * 返回所有的控制器对象
     *
     * @return
     */
    public static HashMap<Class, Object> getObjs() {
        return objs;
    }

    /**
     * 返回所有的监听器对象(如果要获取插件的监听对象建议去看PlugRUN)  控制器类：处理方法们
     * 主键是类名称
     *
     * @return
     */
    public static HashMap<String, HashMap<Method, Listener>> getListeners2() {
        return listeners2;
    }

    /**
     * 创建监听
     *
     * @param cls        控制器类
     * @param eventClass 事件类型
     * @param method     处理方法
     * @param time       创建对象的间隔事件，单位毫秒(ms)
     * @param reObj      为true时，每隔一段时间就新创建一次控制器的对象
     */
    private void newListener0(Class<?> cls, Class<? extends Event> eventClass, Method method, long time, boolean reObj) {
//        logger.removeAppender(StringFormat.formatLeftS(method.toString(), 95) + "创建监听事件类型：\t" + eventClass);


        //安全检查
        if (method.getParameterTypes().length <= 0) return;
        if (!method.getParameterTypes()[0].equals(eventClass)) {
            logger.warn(cls + " 类下 " + method.getName() + " 方法的参数类型'" + method.getParameterTypes()[0] + "' 与@EventType所描述事件类型 '" + eventClass + "' 不一致，如果他们没有父子关系可能导致事件无法正确的被处理");
        }

        //创建监听
        listener0 = GlobalEventChannel.INSTANCE.subscribeAlways(eventClass, event -> {
            try {
                //是否新创建控制器对象
                if (reObj && (System.currentTimeMillis() - thisObjTime) >= time) {
                    thisObjTime = System.currentTimeMillis();
                    obj = cls.newInstance();
                    NewListener.objs.put(cls, obj);
                }
            } catch (Exception e) {
                logger.error(method + " 中无法创建对象", e);
            }


            //执行处理方法,这里吧线程池给撤了，不知道会不会出现什么问题
            try {
                //执行处理方法
                method.invoke(obj, event);
            } catch (Exception e) {
                logger.error(method + " (监听处理)方法执行失败", e);
            }

        });


        //添加监听器到集合
        setListeners(cls, method, listener0);

        //全局控制器类对象
        NewListener.objs.put(cls, obj);
    }

    /**
     * 创建临时监听对象
     *
     * @param eventClass 事件类型
     * @param runnable   任务类
     * @Title 创建临时监听
     * @例子 <p>
     * NewListener newListener = new NewListener();
     * newListener.newTempListener0(GroupMessageEvent.class, new EventTask<GroupMessageEvent>() {
     * public boolean run(GroupMessageEvent event) {
     * System.out.println(4);
     * return true; //返回true就结束监听
     * }
     * });
     * </p>
     */
    public void newTempListener(Class<? extends Event> eventClass, EventTask<? extends MessageEvent> runnable) {

        //用一个线程创建监听，防止主线程被阻塞后监听也被阻塞
//        NewThreadPoolUtils.getInstance().execute(() -> {
        //创建监听
        Listener listener = GlobalEventChannel.INSTANCE.subscribeAlways(eventClass, event -> {

            //多线程执行处理方法，防止处理方法中有阻塞操作
            NewThreadPoolUtils.getInstance().execute(() -> {
                //执行任务类中的方法，并接受返回值
                boolean run = runnable.run(event);
                //返回值为 true 时关闭临时监听
                if (run || System.currentTimeMillis() >= runnable.getTimeOfDeath()) {
                    //从临时监听集合中获取到监听并注销他
                    //监听的key就是一个任务类的实例对象，他在内存中是唯一的
                    tempListeners.get(runnable).complete();
                    try {
                        //移除临时监听
                        remove(runnable.getClass());
                    } catch (Exception e) {
                        logger.error("TempListener[" + runnable + "]" + " 临时监听关闭失败", e);
                    }
                    logger.info(runnable + "  临时监听关闭: 是否是因为超时关闭 " + (System.currentTimeMillis() >= runnable.getTimeOfDeath()));
                }
            });
        });

        //保存监听到临时集合
        tempListeners.put(runnable, listener);

        //添加监听器到集合
        try {
            setListeners(runnable.getClass(), runnable.getClass().getMethod("run", Event.class), listener);
            logger.info(runnable + "临时监听被创建");
        } catch (NoSuchMethodException e) {
            logger.error("添加监听失败", e);
        }

        //全局控制器类对象
        NewListener.objs.put(runnable.getClass(), obj);
//        });
    }

    /**
     * 添加监听器到集合
     *
     * @param cls
     * @param method
     * @param listener
     */
    private void setListeners(Class<?> cls, Method method, Listener listener) {
        //更新全局变量
        //(Listeners<Class,HashMap<Method, Listener>> 监听器)全局监听事件集合  主键是类对象
        //获取到存放这个类下的map表，并添加监听到表中
        HashMap<Method, Listener> listeners = NewListener.listeners.get(cls);
        if (listeners == null) {
            listeners = new HashMap<>();
            listeners.put(method, listener);
        } else {
            listeners.put(method, listener);
        }
        //更新
        //将map表放回去（虽然可能不用放也许）
        NewListener.listeners.put(cls, listeners);//注册进类事件列表
        NewListener.listeners2.put(cls.getName(), listeners);//注册进类事件列表
    }

    /**
     * 返回最后一个创建的监听器
     *
     * @return
     */
    public Listener getLastListener0() {
        return listener0;
    }
}
