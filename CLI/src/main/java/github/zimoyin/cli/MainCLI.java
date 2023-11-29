package github.zimoyin.cli;

import github.zimoyin.cli.command.CommandManager;
import github.zimoyin.cli.listen.CliListener;
import github.zimoyin.cli.listen.MainArgs;
import org.apache.logging.log4j.Level;

public class MainCLI {
    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {
        CliListener run = run(null, args);
        run.setLog(true);
        run.run();
    }

    //给命令行添加颜色，需要引入 MiraiTool 包，需要在调用 run 方法运行前
//    public static void initCLI() {
//        //启动后，在每次日志输出完毕后就重绘命令提示符
//        MiraiLog4j.addAfterLogger(unused -> {
//            System.out.print("\033[38m");
//            CliListener.repaint();
//        });
//        BeforeAppender.addBeforeLogger(event -> {
////            System.out.print("\r");
//            Level level = event.getLevel();
//            if (level == Level.ERROR || level == Level.FATAL) {
//                System.out.print("\033[31m");
//            } else if (level == Level.WARN) {
//                System.out.print("\033[93m");//33
//            } else if (level == Level.INFO) {
//                System.out.print("\033[32m");
//            } else if (level == Level.DEBUG) {
//                System.out.print("\033[36m");
//            } else if (level == Level.TRACE) {
//                System.out.print("\033[36m");
//            } else {
//                System.out.print("\033[38m");
//            }
//
//        });
//    }

    /**
     * @param cls     处理程序运行时 args 参数的类
     * @param args    args 参数
     * @param classes 命令类列表，如果没有可以不填 如果有使用(需要MiraiTool模块) FindClassCache.getInstance().getFilterResultsToClass().toArray(new Class[0]) 扫描
     */
    public static CliListener run(Class<?> cls, String[] args, Class<?>... classes) throws NoSuchMethodException, IllegalAccessException {
        if (cls != null && args != null && args.length > 0) new MainArgs(cls, args);
        CommandManager.initialize(classes);
        return new CliListener(System.in);
    }
}
