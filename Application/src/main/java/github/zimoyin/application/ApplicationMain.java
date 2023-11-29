package github.zimoyin.application;

import github.zimoyin.application.dao.MiraiToolBotConfigurationImpl;
import github.zimoyin.application.dao.table.CreateTable;
import github.zimoyin.cli.MainCLI;
import github.zimoyin.cli.command.CommandLoader;
import github.zimoyin.cli.listen.CliListener;
import github.zimoyin.mtool.dao.BeforeAppender;
import github.zimoyin.mtool.dao.MiraiLog4j;
import github.zimoyin.mtool.run.RunMain;
import github.zimoyin.mtool.util.FindClassCache;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import org.apache.logging.log4j.Level;

@Slf4j
public final class ApplicationMain {
    public static void main(String[] args) {
        initCLI();
        RunMain.runAndLogin(new MiraiToolBotConfigurationImpl());
        createTable();
        try {
            CliListener cliListener = MainCLI.run(null, args, FindClassCache.getInstance().getFilterResultsToClass().toArray(new Class[0]));
//            listener.setPrefix("");
            System.out.println("控制台启动成功,输入 help 来查看更多命令。");
            cliListener.run();
        } catch (Exception e) {
            log.error("CLI ERROR", e);
        }
    }

    private static void initCLI() {
        //启动后，在每次日志输出完毕后就重绘命令提示符
        MiraiLog4j.addAfterLogger(unused -> {
            System.out.print("\033[38m");
            CliListener.repaint();
        });
        BeforeAppender.addBeforeLogger(event -> {
//            System.out.print("\r");
            Level level = event.getLevel();
            if (level == Level.ERROR || level == Level.FATAL) {
                System.out.print("\033[31m");
            } else if (level == Level.WARN) {
                System.out.print("\033[93m");//33
            } else if (level == Level.INFO) {
                System.out.print("\033[32m");
            } else if (level == Level.DEBUG) {
                System.out.print("\033[36m");
            } else if (level == Level.TRACE) {
                System.out.print("\033[36m");
            } else {
                System.out.print("\033[38m");
            }

        });
    }

    /**
     * 创建表
     */
    public static void createTable() {
        for (Bot bot : Bot.getInstances()) {
            new CreateTable().create(bot.getId());
        }
    }
}
