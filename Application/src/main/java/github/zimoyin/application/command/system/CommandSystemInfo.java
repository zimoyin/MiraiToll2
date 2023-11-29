package github.zimoyin.application.command.system;

import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.annotation.Filter;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.command.filter.impl.Level;

import java.lang.management.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@CommandClass
public class CommandSystemInfo {

    private static NumberFormat fmtI = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.ENGLISH));
    private static NumberFormat fmtD = new DecimalFormat("###,##0.000", new DecimalFormatSymbols(Locale.ENGLISH));

    protected static String getKindName(String kind) {
        if ("NON_HEAP".equals(kind)) {
            return "NON_HEAP(非堆内存)";
        } else {
            return "HEAP(堆内存)";
        }
    }

    protected static String getPoolName(String poolName) {
        switch (poolName) {
            case "Code Cache":
                return poolName + "(代码缓存区)";
            case "Metaspace":
                return poolName + "(元空间)";
            case "Compressed Class Space":
                return poolName + "(类指针压缩空间)";
            case "PS Eden Space":
                return poolName + "(伊甸园区)";
            case "PS Survivor Space":
                return poolName + "(幸存者区)";
            case "PS Old Gen":
                return poolName + "(老年代)";
            default:
                return poolName;
        }
    }

    protected static String bytesToMB(long bytes) {
        return fmtI.format((long) (bytes / 1024 / 1024)) + " MB";
    }

    protected static String printSizeInKb(double size) {
        return fmtI.format((long) (size / 1024)) + " kbytes";
    }

    protected static String toDuration(double uptime) {
        uptime /= 1000;
        if (uptime < 60) {
            return fmtD.format(uptime) + " seconds";
        }
        uptime /= 60;
        if (uptime < 60) {
            long minutes = (long) uptime;
            String s = fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
            return s;
        }
        uptime /= 60;
        if (uptime < 24) {
            long hours = (long) uptime;
            long minutes = (long) ((uptime - hours) * 60);
            String s = fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
            if (minutes != 0) {
                s += " " + fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
            }
            return s;
        }
        uptime /= 24;
        long days = (long) uptime;
        long hours = (long) ((uptime - days) * 24);
        String s = fmtI.format(days) + (days > 1 ? " days" : " day");
        if (hours != 0) {
            s += " " + fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
        }
        return s;
    }

    @Command("system")
    @Filter(Level.Root)
    public String onInfo(CommandData data) {
        //运行时情况
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        //线程使用情况
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        //堆内存使用情况
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        //非堆内存使用情况
        MemoryUsage nonHeapMemoryUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        //类加载情况
        ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
        //内存池对象
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        //编译器和编译情况
        CompilationMXBean cm = ManagementFactory.getCompilationMXBean();
        //获取GC对象（不好用）
        List<GarbageCollectorMXBean> gcmList = ManagementFactory.getGarbageCollectorMXBeans();


        StringBuilder buffer = new StringBuilder();
        buffer.append("System Information").append("\n");
        buffer.append("================================").append("\n");
        buffer.append("OS Name : ").append(SystemUtil.getOsName()).append("\n");
        buffer.append("OS Version : ").append(SystemUtil.getOsVersion()).append("\n");
        buffer.append("OS Arch : ").append(SystemUtil.getOsArch()).append("\n");
        buffer.append("OS CPU Count : ").append(os.getAvailableProcessors()).append("\n");
        buffer.append("OS 负载平均值 : ").append(os.getSystemLoadAverage()).append("\n");
        buffer.append("--------------------------------").append("\n");
        buffer.append("User Name : ").append(SystemUtil.getUsername()).append("\n");
        buffer.append("User Home : ").append(SystemUtil.getUserHome()).append("\n");
        buffer.append("User Dir : ").append(SystemUtil.getUserDir()).append("\n");
        buffer.append("Line Seprator : ").append(SystemUtil.getLineSeprator().trim()).append("\n");
        buffer.append("--------------------------------").append("\n");
        buffer.append("JVM Name : ").append(System.getProperty("java.vm.name")).append("\n");
        buffer.append("JAVA Version : ").append(System.getProperty("java.version")).append("\n");
        buffer.append("JVM 的启动总时间 : ").append(toDuration(runtime.getStartTime())).append("\n");
        buffer.append("JVM 虚拟机的正常运行时间 : ").append(toDuration(runtime.getUptime())).append("\n");
        buffer.append("编译器名称 : ").append(cm.getName()).append("\n");
        buffer.append("编译器耗时 : ").append(cm.getTotalCompilationTime()).append("\n");
        buffer.append("是否支持即使编译器编译监控 : ").append(cm.isCompilationTimeMonitoringSupported()).append("\n");
        buffer.append("--------------------------------").append("\n");
        buffer.append("总线程数 : ").append(threads.getThreadCount()).append("\n");
        buffer.append("守护进程线程数 : ").append(threads.getDaemonThreadCount()).append("\n");
        buffer.append("峰值线程数 : ").append(threads.getPeakThreadCount()).append("\n");
        buffer.append("Java虚拟机启动后创建并启动的线程总数 : ").append(threads.getTotalStartedThreadCount()).append("\n");
//        for(Long threadId : threads.getAllThreadIds()) {
//            System.out.printf("threadId: %d | threadName: %s%n", threadId, threads.getThreadInfo(threadId).getThreadName());
//        }
        buffer.append("--------------------------------").append("\n");
        buffer.append("初始化堆内存 : ").append(bytesToMB(heapMemoryUsage.getInit())).append("\n");
        buffer.append("已使用堆内存 : ").append(bytesToMB(heapMemoryUsage.getUsed())).append("\n");
        buffer.append("可使用堆内存 : ").append(bytesToMB(heapMemoryUsage.getCommitted())).append("\n");
        buffer.append("最大堆内存 : ").append(bytesToMB(heapMemoryUsage.getMax())).append("\n");
        buffer.append("--------------------------------").append("\n");
        buffer.append("初始化非堆内存 : ").append(bytesToMB(nonHeapMemoryUsage.getInit())).append("\n");
        buffer.append("已使用非堆内存 : ").append(bytesToMB(nonHeapMemoryUsage.getUsed())).append("\n");
        buffer.append("可使用非堆内存 : ").append(bytesToMB(nonHeapMemoryUsage.getCommitted())).append("\n");
        buffer.append("最大非堆内存 : ").append(bytesToMB(nonHeapMemoryUsage.getMax())).append("\n");
        buffer.append("--------------------------------").append("\n");
        buffer.append("当前加载类数量 : ").append(cl.getLoadedClassCount()).append("\n");
        buffer.append("未加载类数量 : ").append(cl.getUnloadedClassCount()).append("\n");
        buffer.append("总加载类数量 : ").append(cl.getTotalLoadedClassCount()).append("\n");

        buffer.append("================================").append("\n");
        return buffer.toString();

    }
}

