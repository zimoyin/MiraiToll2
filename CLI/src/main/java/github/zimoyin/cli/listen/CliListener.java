package github.zimoyin.cli.listen;

import github.zimoyin.cli.command.CommandManager;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 控制台监听
 */
@Data
@Slf4j
public class CliListener {
    @Setter
    @Getter
    private static String prefix = ">";//命令提示符
    private final BufferedReader br;
    private volatile boolean stop = false;
    private volatile int line = 1;
    private CommandManager manager;
    private boolean singletonCommand = false;//命令是否单例运行
    private boolean isLog = false;//是否记录日志


    public CliListener() {
        InputStreamReader is = new InputStreamReader(System.in); //new构造InputStreamReader对象
        br = new BufferedReader(is); //拿构造的方法传到BufferedReader中，此时获取到的就是整个缓存流

    }

    public CliListener(InputStream in) {
        InputStreamReader is = new InputStreamReader(in); //new构造InputStreamReader对象
        br = new BufferedReader(is); //拿构造的方法传到BufferedReader中，此时获取到的就是整个缓存流
    }

    /**
     * 重绘命令提示符
     */
    public static void repaint() {
        System.out.print(prefix + " \r");
    }

    public CliListener run() {
        run0();
        close();
        return this;
    }

    public void close() {
        try {
            br.close();
        } catch (IOException e) {
            log.error("[CLI-SYSTEM] Error while closing BufferedReader", e);
        }
    }

    private synchronized void run0() {
        manager = CommandManager.getInstance();
        boolean lineIsNull = false;
        CommandExecute commandExecute = new CommandExecute(manager, singletonCommand, isLog);
        while (!stop) {
            try {
                Thread.sleep(50);
//                if (!lineIsNull) System.out.print("\n" + prefix + " \r");
//                else System.out.print(prefix + " \r");
                repaint();
                //读取命令
                String read = br.readLine();
                if (read == null || read.length() == 0) {
                    lineIsNull = true;
                    continue;
                }
                lineIsNull = false;
                commandExecute.execute(read);
            } catch (Exception e) {
                log.error("[CLI-SYSTEM] 无法处理的异常", e);
            }
        }
    }
}
