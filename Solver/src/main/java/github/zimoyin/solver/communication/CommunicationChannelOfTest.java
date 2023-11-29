package github.zimoyin.solver.communication;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

public class CommunicationChannelOfTest implements CommunicationChannel<String> {
    private volatile static CommunicationChannelOfTest INSTANCE;
    private final BlockingQueue<String> queue = new LinkedTransferQueue<>();

    private CommunicationChannelOfTest() {
    }

    public static CommunicationChannelOfTest getInstance() {
        if (INSTANCE == null) synchronized (CommunicationChannelOfTest.class) {
            if (INSTANCE == null) INSTANCE = new CommunicationChannelOfTest();
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
