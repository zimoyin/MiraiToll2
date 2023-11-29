package github.zimoyin.mtool.control;

import github.zimoyin.mtool.annotation.EventType;
import net.mamoe.mirai.event.Event;

/**
 * 监听处理类
 *
 * @param <T> 泛型为事件，但是注意该事件必须是注册时的事件或者子事件
 */
public interface EventTask<T extends Event> {

    long lifeLength = 5 * 60 * 1000;//临时变量存活的时间 5min
    long createTime = System.currentTimeMillis();//获取到创建时间
//    long timeOfDeath = createTime + lifeLength;//变量预计在什么时间死亡

    /**
     * 临时事件执行方法
     *
     * @param event
     * @return 返回true就结束这个事件
     */
    @EventType
    public boolean run(T event);

    /**
     * 临时事件执行方法
     *
     * @param obj event
     * @return 返回true就结束这个事件
     */
    public default boolean run(Object obj) {
        if (obj == null) throw new NullPointerException("事件为空");
        if (!Event.class.isAssignableFrom(obj.getClass())) throw new IllegalArgumentException("参数应为 Event 实例");
        return run((T) obj);
    }

    /**
     * 获取死亡时间，可以通过重写此方法以此来设定死亡时间
     *
     * @return 预计死亡时间(ms)
     */
    public default long getTimeOfDeath() {
        return createTime + getLifeLength();
    }

    /**
     * 获取存活时间，可以重写此方法以此自定义世间，单位毫秒
     */
    public default long getLifeLength() {
        return lifeLength;
    }

    /**
     * 初始化时执行
     */
    public default void init() {

    }


    /**
     * 临时监听结束后执行的钩子函数
     */
    public default void dead() {

    }

    /**
     * 超时死亡时执行的构子函数
     */
    public default void timeoutDead() {
    }

    /**
     * 主动关闭时执行的构子函数
     */
    public default void closeDead() {
    }

    /**
     * 是否已经超时死亡。可以通过重写此方法来自定义死亡条件，或者禁止超时死亡
     *
     * @return 默认实现为超时后死亡
     */
    public default boolean isTimeoutDead() {
        return System.currentTimeMillis() >= this.getTimeOfDeath();
    }

    public default String getDescription() {
        return "监听预期生命长度(ms): " +
                getLifeLength() + ";  " +
                "监听创建时间(ms): " +
                createTime + ";  " +
                "监听预计死亡时间(ms): " +
                getTimeOfDeath() + "; " +
                "当前时间(ms): " +
                System.currentTimeMillis();

    }
}
