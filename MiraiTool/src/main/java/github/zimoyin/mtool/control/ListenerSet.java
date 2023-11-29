package github.zimoyin.mtool.control;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.Listener;

import java.util.HashSet;
import java.util.concurrent.CancellationException;

/**
 * 监听器集合，允许一个监听被多次创建并添加进集合，但是不推荐这么做
 * 监听器集合组织形式：(该集合仅仅作为一个监听器记录形式存在，不能对监听器实际集合产生影响)
 * 1. 监听器以类为一组的形式存在于监听器集合中的
 * 2. 具体监听(方法)是这个一组中的成员
 */
@Slf4j
public class ListenerSet extends HashSet<ListenerObj> {
    private static ListenerSet INSTANCE;

    /**
     * 单例模式
     */
    public static synchronized ListenerSet getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ListenerSet();
        }
        return INSTANCE;
    }

    @Override
    public boolean add(ListenerObj listener) {
        //禁止二次添加
        if (contains(listener)) {
            log.warn("重复添加的监听: {}", listener);
            return false;
        }
        //检测过往的监听方法是否注册了监听，不检测当前传入的监听方法
        listenerExists();
        return super.add(listener);
    }

    /**
     * 检测已存在于集合中的监听方法是否注册了监听
     */
    public void listenerExists() {
        //检测监听集合是否正常
        for (ListenerObj obj : ListenerSet.getInstance()) {
            for (ListenerObj.ListenerMethod method : obj.getMethods()) {
                Listener listener = method.getListener();
                if (listener == null) {
                    log.warn("检测到监听方法无注册监听,方法：{} ,监听:{}", method, listener);
                }
            }
        }
    }

    /**
     * 该方法运行添加监听到集合，但是注意要注册这个监听才行
     *
     * @param listener
     * @return
     */
    @Deprecated
    public boolean addListener(ListenerObj listener) {
        return super.add(listener);
    }


    @Override
    @Deprecated
    public void clear() {
        for (ListenerObj obj : this) {
            for (Listener listener : obj.getListeners()) {
                listener.complete();
//                listener.cancel(new CancellationException());
            }
        }
        super.clear();
    }

    /**
     * 停止所有监听
     */
    public void complete() {
        for (ListenerObj obj : this) {
            for (Listener listener : obj.getListeners()) {
                listener.complete();
            }
        }
    }

    /**
     * 取消所有监听
     *
     * @param e
     */
    public void cancel(CancellationException e) {
        for (ListenerObj obj : this) {
            for (Listener listener : obj.getListeners()) {
                listener.cancel(e);
            }
        }
    }

    public void start() {
        for (ListenerObj obj : this) {
            for (Listener listener : obj.getListeners()) {
                listener.start();
            }
        }
    }
}