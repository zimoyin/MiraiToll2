package github.zimoyin.cli.listen;

import github.zimoyin.cli.command.CommandObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 当Main方法执行时，解析通过 vm 传入的参数
 */
@Getter
@Setter
public class MainArgs extends CommandExecute {
    private final CommandObject commandObject;
    private final Class<?> clazz;

    public MainArgs(Class<?> cls, String[] args) throws IllegalAccessException {
        this.clazz = cls;
        //命令类
        commandObject = new CommandObject(cls, null);
        //参数行
        List<String> arg = new ArrayList<String>();
        arg.add(commandObject.getName());
        arg.addAll(Arrays.asList(args));
        //构建参数
        Object instance = getCommandInstance(commandObject);
        setParameter(commandObject, instance, arg.toArray(new String[0]));
        //执行命令
        commandObject.execute(instance);
    }
}
