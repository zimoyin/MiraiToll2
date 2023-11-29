package github.zimoyin.application.shell;

import github.zimoyin.cli.annotation.Shell;
import github.zimoyin.cli.command.IShell;

@Shell(value = "main", description = "当 Jar 被执行时传入的jvm参数，将被此类处理")
public class MainArgs implements IShell {
    @Override
    public void execute() {

    }
}
