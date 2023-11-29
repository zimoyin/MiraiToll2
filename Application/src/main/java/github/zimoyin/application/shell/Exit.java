package github.zimoyin.application.shell;

import github.zimoyin.cli.annotation.Shell;
import github.zimoyin.cli.command.IShell;
import github.zimoyin.mtool.dao.H2ConnectionFactory;
import github.zimoyin.mtool.util.NewThreadPoolUtils;
import net.mamoe.mirai.Bot;

@Shell("exit")
public class Exit implements IShell {

    @Override
    public void execute() {
        for (Bot bot : Bot.getInstances()) {
            bot.close();
        }
        H2ConnectionFactory.INSTANCE.close();
        NewThreadPoolUtils.getInstance().getTpe().shutdown();
        System.exit(0);
    }
}
