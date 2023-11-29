package github.zimoyin.mtool.control;

import net.mamoe.mirai.event.Listener;

import java.util.HashMap;

/**
 * 临时监听集合
 */
//key: 实现内部类对象，监听对象
public class TempListenerSet extends HashMap<EventTask, Listener> {
    private static TempListenerSet INSTANCE;

    public static synchronized TempListenerSet getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TempListenerSet();
        }
        return INSTANCE;
    }
}
