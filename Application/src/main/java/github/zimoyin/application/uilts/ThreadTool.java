package github.zimoyin.application.uilts;

import lombok.Data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class ThreadTool {
    private static ThreadTool INSTANCE;
    private ExecutorService executorService = Executors.newFixedThreadPool(32);

    private ThreadTool() {
    }

    public synchronized static ThreadTool getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new ThreadTool();
        }
        return INSTANCE;
    }
}
