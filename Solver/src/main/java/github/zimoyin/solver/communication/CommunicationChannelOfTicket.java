package github.zimoyin.solver.communication;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

public class CommunicationChannelOfTicket implements CommunicationChannel<String> {
    private volatile static CommunicationChannelOfTicket INSTANCE;
    private final BlockingQueue<String> queue = new LinkedTransferQueue<>();

    private CommunicationChannelOfTicket() {
    }

    public static CommunicationChannelOfTicket getInstance() {
        if (INSTANCE == null) synchronized (CommunicationChannelOfTicket.class) {
            if (INSTANCE == null) INSTANCE = new CommunicationChannelOfTicket();
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

    public int size() {
        return queue.size();
    }
}
