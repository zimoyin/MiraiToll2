package github.zimoyin.mtool.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class NewThreadPoolUtils {
    /**
     * 刚开始都是在创建新的线程，达到核心线程数量5个后，新的任务进来后不再创建新的线程，
     * 而是将任务加入工作队列，任务队列到达上线5个后，新的任务又会创建新的普通线程，
     * 直到达到线程池最大的线程数量10个，后面的任务则根据配置的饱和策略来处理。
     *
     * @Title ThreadPoolExecutor:创建自定义线程池，
     * @param 池中常驻的线程数为 1
     * @param 允许最大的线程数为  cpu核心数*2 + 所有的插件数，
     * @param 存活时间450ms,
     * @param 单位毫秒,
     * @param 双缓冲队列 最大的线程数*2，,
     * @param 拒绝策略CallerRunsPolicy 如果队列满了，就拿主线程执行
     */
    private static ThreadPoolExecutor tpe = null;
    private static NewThreadPoolUtils obj = null;
    private final int MinThreadCount = 10;//最小线程
    protected Logger logger = LoggerFactory.getLogger(NewThreadPoolUtils.class);
    private int AllBotGroups = 0; //所有机器人加的群数
    private int cpuCount;//cpu 核数
    private int MaxThreadCount = 120;//最大线程
    private int QueueCount = MinThreadCount / 2 + 1;//队列数

    //单例
    private NewThreadPoolUtils() {
        //AllBotGroups = Login.getBots().stream().map(Bot::getBot).map(Bot::getGroups).collect(Collectors.toList()).stream().map(ContactList::size).reduce(0, Integer::sum);
        cpuCount = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
//        MaxThreadCount = cpuCount * 2 + new File("./plug").list().length;
//        QueueCount = MaxThreadCount * 2;

        tpe = new ThreadPoolExecutor(MinThreadCount, MaxThreadCount,
                450,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(QueueCount),
                new MyTheadFactory("ThreadPool"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        logger.debug("线程池创建成功,最大线程数: " + MaxThreadCount);
    }

    public static NewThreadPoolUtils getInstance() {
        if (obj == null) {
            synchronized (NewThreadPoolUtils.class) {
                obj = new NewThreadPoolUtils();
            }
        }
        return obj;
    }

    public ThreadPoolExecutor getTpe() {
        return tpe;
    }

    public void execute(Runnable runnable) {
        tpe.execute(runnable);
    }


    static class MyTheadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        MyTheadFactory() {
            //默认namePrefix = default-name-pool
            this("default-name-pool");
        }

        MyTheadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            //此时namePrefix就是 name + 第几个用这个工厂创建线程池的
            this.namePrefix = name + "-p" +
                    poolNumber.getAndIncrement();
        }

        public Thread newThread(Runnable r) {
            //此时线程的名字 就是 namePrefix + -thread- + 这个线程池中第几个执行的线程
            Thread t = new Thread(group, r,
                    namePrefix + "-Thread-" + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
