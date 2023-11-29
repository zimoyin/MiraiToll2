package github.zimoyin.mtool.control;

import github.zimoyin.mtool.annotation.ThreadSpace;
import github.zimoyin.mtool.util.NewThreadPoolUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;

import java.util.function.Consumer;

@Data
@Slf4j
public class ListeningRegistration {
    /**
     * 监听集合对象
     */
    private ListenerSet listeners = ListenerSet.getInstance();

    /**
     * 创建监听
     *
     * @param cls 带有处理方法的类（会通过cls创建对象）
     */
    public ListeningRegistration(Class<?> cls) {
        registration(new ListenerObj(cls));
    }

    /**
     * 创建监听
     *
     * @param cls 带有处理方法的类（会通过cls创建对象）
     */
    public static void registration(Class<?> cls) {
        new ListeningRegistration(cls);
    }

    /**
     * 注册监听: 对监听器中的方法进行注册监听
     * 注意此方法可以可以被静态调用，同时参数不会被此方法检测是否有效，所以请谨慎调用。
     * 此方法可以将一个非 Controller 的类 下的监听方法进行注册，如果你选择这样为你的方法进行这种注册方式那么请对他负责
     *
     * @param listenerMethod 监听
     */
    public static void newListener0(ListenerObj.ListenerMethod listenerMethod) throws NullPointerException {
        //创建监听
        //执行处理方法
        listenerMethod.setExecute(true);
        Listener<? extends Event> listener = GlobalEventChannel.INSTANCE.subscribeAlways(listenerMethod.getEventClas(), (Consumer<Event>) event -> {
            ThreadSpace annotation = listenerMethod.getCls().getAnnotation(ThreadSpace.class);
            ThreadSpace annotation2 = listenerMethod.getMethod().getAnnotation(ThreadSpace.class);
            boolean invokes = ControllerFilterMap.getInstance().invokes(event);
            if (annotation == null && annotation2 == null) {
                if (invokes) listenerMethod.invoke(event);
            } else {
                if (invokes) NewThreadPoolUtils.getInstance().execute(() -> listenerMethod.invoke(event));
            }
        });
        listenerMethod.setListener(listener);
    }

    /**
     * 创建临时监听对象
     *
     * @param eventClass 事件类型
     * @param runnable   任务类： 任务类的泛型请指定为 eventClass 的子类、父类、或者本身，但请勿指定其兄弟类。这里推荐使用其本身或者其子类
     * @Title 创建临时监听
     * 注意： 临时监听为程序控制监听用于临时接收信息，该信息需要临时监听有条件的过滤。如果临时监听没有对信息进行筛选则会导致程序异常，信息筛选应该存在临时监听处理逻辑里面
     * @例子 <p>
     * ListeningRegistration.newTempListener0(GroupMessageEvent.class, new EventTask<GroupMessageEvent>() {
     * public boolean run(GroupMessageEvent event) {
     * System.out.println(4);
     * return true; //返回true就结束监听
     * }
     * });
     * </p>
     */
    public static void newTempListener(Class<? extends Event> eventClass, EventTask<? extends Event> runnable) {
        TempListenerSet tempListenerSet = TempListenerSet.getInstance();
        runnable.init();
        //创建监听：允许监听自定义事件
        Listener<? extends Event> tempListener = GlobalEventChannel.INSTANCE.subscribeAlways(eventClass, event -> {
            //多线程执行处理方法，防止处理方法中有阻塞操作
            NewThreadPoolUtils.getInstance().execute(() -> {
                //执行任务类中的方法，并接受返回值
                boolean run = false;
                try {
                    if (!runnable.isTimeoutDead()) run = runnable.run(event);
                } catch (Exception e) {
                    log.error("临时监听执行失败(来自监听实现的错误)：TempListener[{}]", runnable, e);
                }
                //返回值为 true 时关闭临时监听,或者监听时间大于了监听存活时间时关闭监听
                if (run || runnable.isTimeoutDead()) {
                    //执行死亡方法
                    runnable.dead();
                    if (run) runnable.closeDead();
                    if (runnable.isTimeoutDead()) runnable.timeoutDead();
                    //从临时监听集合中获取到监听并注销他
                    //监听的key就是一个任务类的实例对象，他在内存中是唯一的
                    tempListenerSet.get(runnable).complete();
                    try {
                        //移除临时监听
                        tempListenerSet.remove(runnable);
                    } catch (Exception e) {
                        log.error("TempListener[" + runnable + "]" + " 临时监听从注册列表移除失败", e);
                    }
                    log.debug("临时监听{}关闭: TempListener[{}]({})", (runnable.isTimeoutDead()) ? "超时" : "主动", runnable, runnable.getDescription());
                }
            });
        });
        //添加监听到集合
        tempListenerSet.put(runnable, tempListener);
    }

    /**
     * 注册监听
     */
    private void registration(ListenerObj listener) {
        //禁止监听器为null 或 监听器不是一个有效的监听器就禁止注册
        if (listener == null || !listener.isListening()) return;
        //如果监听没有成功的添加进集合就禁止进行注册
        if (!listeners.add(listener)) return;
        log.debug(" [系统日志]加载控制器类: " + listener.getCls());
        //注册该(监听)类下所有的监听方法
        for (ListenerObj.ListenerMethod listenerMethod : listener.getMethods()) {
            ListeningRegistration.newListener0(listenerMethod);
        }
    }

}

