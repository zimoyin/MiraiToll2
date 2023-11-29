package github.zimoyin.solver.communication;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

public class CommunicationChannelOfURL implements CommunicationChannel<String> {
    private volatile static CommunicationChannelOfURL INSTANCE;
    //一个由链表结构组成的无界阻塞队列
    private final BlockingQueue<String> queue = new LinkedTransferQueue<>();

    private CommunicationChannelOfURL() {
    }

    public static CommunicationChannelOfURL getInstance() {
        if (INSTANCE == null) synchronized (CommunicationChannelOfURL.class) {
            if (INSTANCE == null) INSTANCE = new CommunicationChannelOfURL();
        }
        return INSTANCE;
    }

    @Override
    public String getValue() {
        try {
            return queue.poll(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean setValue(String value) {
        return queue.add(value);
    }
}
