package github.zimoyin.application.shell;

import github.zimoyin.cli.annotation.Shell;
import github.zimoyin.cli.command.IShell;
import github.zimoyin.mtool.run.RunMain;

@Shell(value = "login", description = "登录账户")
public class Login implements IShell {
    @Shell.Parameter(value = "-u", description = "QQ号")
    private long username;
    @Shell.Parameter(value = "-p", description = "密码")
    private String password;
    @Shell.Parameter(value = "-init", description = "是否初始化框架")
    private boolean isInit = true;

    @Override
    public void execute() {
        System.out.println(username);
        System.out.println(password);
        System.out.println(isInit);
        if (username == 0 || password == null) {
            System.err.println("账户或密码不得为空");
            return;
        }
        if (isInit && !RunMain.isRunning) {
            RunMain.runAndLogin(username, password);
        } else {
            github.zimoyin.mtool.login.Login.login(username, password);
        }
    }
}
