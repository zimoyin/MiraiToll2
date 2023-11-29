package github.zimoyin.mtool.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StackTraceInfo {
    private final StackTraceElement[] Stack;
    private StackTraceElement StackTrace;

    public StackTraceInfo() {
        Stack = Thread.currentThread().getStackTrace();
//        StackTrace = Stack[Stack.length - 1];
        StackTrace = Stack[getIndex()];
    }

    public StackTraceInfo(int index) {
        Stack = Thread.currentThread().getStackTrace();
        StackTrace = Stack[Stack.length - 1 - index];
    }

    /**
     * 过滤栈帧，找到 “net.mamoe.mirai.utils.MiraiLoggerPlatformBase” 下的一条栈帧
     *
     * @return
     */
    private int getIndex() {
        final String cls = "net.mamoe.mirai.utils.MiraiLoggerPlatformBase";
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement traceInfo : Stack) {
            sb.append("\n").append("\tat ").append(traceInfo.getClassName()).append(traceInfo.getMethodName()).append("()").append(":").append(traceInfo.getLineNumber());
        }
        log.trace("栈帧: {}", sb);
        for (int i = 0; i < Stack.length; i++) {
            if (Stack[i].getClassName().equalsIgnoreCase(cls)) {
                //从当前栈帧开始向下遍历 10 层 栈帧，直到定位到不为 net.mamoe.mirai.utils.MiraiLoggerPlatformBase 的栈帧为止
                for (int j = 0; j < Stack.length; j++) {
                    if (j >= 10) throw new IllegalArgumentException("对于日志的调用栈过于深入");
                    if (!Stack[i + j].getClassName().equalsIgnoreCase(cls)) return i + j;
                }
                throw new IllegalArgumentException("意料之外的异常，他代表找不到 cls 之外的栈帧，已经循环并未抛出异常。见到这个就请尽快处理此严重异常");
            }
        }

        NullPointerException exception = new NullPointerException("没有找到关于：" + cls + " 的栈帧记录；栈帧: {}");
        log.error("没有找到关于：" + cls + " 的栈帧记录；栈帧: {}", sb, exception);
        throw exception;
    }

    public String getClassName() {
        return StackTrace.getClassName();
    }

    public int getLineNumber() {
        return StackTrace.getLineNumber();
    }

    public String getMethodName() {
        return StackTrace.getMethodName();
    }

    public StackTraceElement[] getStack() {
        return Stack;
    }
}
