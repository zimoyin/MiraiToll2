## 启动
MainCLI.run

## 创建命令
```java
@Shell(value = "login",alias={})
public class ShellTest implements IShell {

    @Shell.Parameter("-user")
    private String user;

    @Override
    public void execute() {
        System.out.println("login: "+user);
    }
}
```

```java
@Shell(value = "login",alias={})
public class ShellTest {

    @Shell.Parameter("-user")
    private String user;

    @Shell.Main
    public void execute() {
        System.out.println("login: "+user);
    }
}
```
## 创建子命令
```java
@Shell(value = "woc",parentCommand = ShellTest.class)
public class ShellTest3 extends ShellTest2{
    @Shell.Parameter("-a")
    private boolean isLogin = false;

    @Shell.Main
    @Override
    public void execute() {
        super.execute();
    }
}
```

## 解析应用启动时传入的命令
```java
public class MainCLI {
    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {
        new MainArgs(ShellTest.class,args);
    }
}
```

```
设置的程序参数： -user zimo
```

## 注意事项
1. 当命令有一个或零个时，允许省略命令参数名称直接附带参数值即可，如：help -help login  -> help login 。
2. 当命令的参数为布尔类型时，允许省略该参数名称后面的值。程序会自动根据该参数的默认值取反，如 help -help  (-help 映射的是boolean类型)
3. Listener 类是监听器，用于监听控制台或者其他输入流
4. CommandExecute 是命令解析与执行类，实例化该类后调用 execute(...) 方法即可。该方法的参数是你从控制台接收到的输入（行）
5. MainArgs(命令类,参数) 当Main方法执行时，解析通过 vm 传入的参数
6. 如何实现不使用参数名称来实现解析参数, 如 login -u 11806 -p root  改为 login 11806 root
    * 通过只有一个参数时不用写参数名称的特性来实现 (第一条特性)
    * 你需要一个String的参数，当用户输入后你需要自己解析这个参数，比如分割字符串以此来获取账号和密码