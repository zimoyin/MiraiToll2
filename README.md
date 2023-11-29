# QQ 机器人封装框架与应用

**这是来自于一个代码功底一般且脸皮一级厚的萌新程序员的作品**
新增：程序命令（CLI 暂无文档）、~~插件加载器（未完善）~~、控制器监听注解(github.zimoyin.mtool.annotation.ControllerFilter)
# 一、框架
如果不使用此登录方法可以跳过本章
## [1. solver](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fsolver) 登录界面

此功能使用了 JAVAFX 以及其组件，如果不需要此部分可以从 pom里面删除相关引用  
1. [ImageLoginSolver.java](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fsolver%2FImageLoginSolver.java)  
   无短信验证
2. [ImageLoginSolverKt.kt](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fsolver%2FImageLoginSolverKt.kt)  
   有短信验证
* 如何应用他们  
   * 继承 BotConfigurationImpl  
```
public void initBefore(){
     //覆盖登录器
     ImageLoginSolver solver = new ImageLoginSolver();//无短信验证
     ImageLoginSolverKt solverKt = new ImageLoginSolverKt();//有短信验证
     setLoginSolver(solverKt);
}
```
   * 启动时传入你继承的对象
```
RunMain.run(...);
```
## [2. mtool](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fmtool) 框架本体
----
# 二、应用框架

你可以将框架打包，然后引入到你项目里面。或者在里面像我一样在 Application 包下开发
项目框架介绍:
* [Solver](Solver) 带GUI的登录解决器
* [MiraiTool](MiraiTool) 框架本体
* [CLI](CLI) 控制台命令框架
* [Application](Application) 我写的简单机器人

## 0. 启动框架
* 启动框架并登录 RunMain.runAndLogin
```java
//其中一个登录方法
RunMain.runAndLogin(new MiraiToolBotConfigurationImpl(){
    @Override
    public void initBefore() {
        //覆盖登录器
        ImageLoginSolver solver = new ImageLoginSolver();//无短信验证
        ImageLoginSolverKt solverKt = new ImageLoginSolverKt();//有短信验证
        setLoginSolver(solverKt);
    }
});
//其中一个登录方法
RunMain.runAndLogin(new MiraiToolBotConfigurationImpl());
//.....
```
* 启动/初始化框架 RunMain.initAll
* 只登录  Login.login
## 1. 命令

### 1.1 注册命令类与执行方法

1. 注册命令类:	使用 @CommandClass 注解标注在类名上面，以此来声明这是个命令类

```java
//注意：命令类是个单例
@CommandClass
public class CommandMusic {}
```

2. 注册命令

* 注意：命令方法的参数列表

  * 可以是任意对象，但是要注意对象有空的构造参数

  * 可以是 MessageEvent 事件或者子类

  * 可以是 CommandData 对事件的封装
  * 还可以不使用任何参数
  * 此外可以是 `MessageChain`、`Bot`、`Contact`、`CommandObj`、`CommandSet`、`ListenerSet`、`H2Connection`

* 注意：命令的返回值

  * void：常用的就是这个，因为你可以自由的使用事件对象或者CommandData 进行发送信息等
  * String：返回一个字符串到发送命令者所在的对话窗口

```java
@CommandClass
public class CommandMusic {
    @Command(
        value = "命令的主要名称",
        description = "命令描述，只有使用了这个才能被 help 命令(框架自带的命令)扫描到",
        help="命令的帮助信息，使用 help 命令对这个命令进行描述是将会用到",
        alias = {"命令别名一","命令别名二"},
        eventType= MessageEvent.class //声明此命令支持的信息类型，默认是 MessageEvent (全部支持)
    )
    @Command("命令的主要名称") //其他参数默认
   // 参数列表通常为 CommandData 就可以满足很多需要，如果需要其他的也可以写上，框架会自动注入的(实验性质)
    public void music(CommandData commandData) {}
```

3. 使用命令

在群聊里面，输入以下格式  `命令前缀 命令主语 空格 参数` 例如：`.eg hi~`
命令格式可以在 `data/config/gloval/command.json` 文件修改
```json
{
  //不需要你进行改动
  "name": "command",
  //命令前缀，如果不需要命令前缀 可以设置为 ""
  "commandPrefix": ".",
  //是否需要at机器人才能使命令生效
  "@": false,
  //命令主语与参数之间是否有空格
  "space": true
}
```
### 1.2 命令解析流程

* `CommandLoadInit`：加载所有的命令类，并加以解析。可以改对象的通过 initMethod 方法进行后续添加命令
* `CommandSet` ：由于 `CommandLoadInit` 维护的一个命令注册表
* `CommandObj`：CommandSet  的容器类型，用于封装用户的命令
* `CommandBroadcast`：命令广播(这是一个框架内部的控制器)：监听信息事件发生，之后交给 CommandExecute 执行
* `CommandExecute`：命令执行器，统一调度 CommandObj
* `CommandData`：当命令被调用后，由框架进行的命令数据封装



## 2. 命令过滤器

命令过滤器是用来截拦命令的，通常用于权限处理，或者某个系列命令执行前的初始化操作

### 2.1 使用过滤器

在命令方法上加入 @Filter 注解

该注解有两个字段: 

* value: 过滤等级，注意此属性不受过滤器影响
* filterCls: 在执行命令前需要经过的局部过滤器列表
```java
@Filter(value = Level.UNLevel, filterCls = {TestFilter.class})
```
```java
@Filter(value = Level.UNLevel, filterCls = {TestFilter.class})
@Command("test")
public void a(CommandData data) {
        .....
}
```

### 2.3 注册过滤器

1. 全局过滤器: 在所有命令之前都要执行一遍的过滤器

* 继承 `AbstractFilter` 类
* 在类的上面加入注解 `@Filter` 以此声明为全局过滤器

2. 局部过滤器：在过滤器列表中定义的局部过滤器才会生效

* 继承 `AbstractFilter` 类

## 3. 控制器

作用： 用来注册监听的

需要使用 `@Controller` 来声明这是个控制器类

需要使用 `@EventType` 来声明这是个监听器处理方法

```java
@Controller
public class TestControl {
    //注解上可以不写事件类型
    @EventType(HistoricalMessageEvent.class)
    //注意方法列表中一定要有且只有一个事件类型
    //该方法不可用改动修饰符，返回值
    public void onHistoricalMessage(HistoricalMessageEvent event){
        
    }
    
    //注册监听拦截器
    //可以拦截所有关于该事件的监听
    //禁止在拦截器上使用 @ThreadSpace 注解
    @ControllerFilter(GroupMessageEvent.class)
    public boolean a(GroupMessageEvent event){
  //        log.warn("过滤器执行....");
      //放行监听
      //false 对监听进行拦截
      return true;
    }
}
```



## 4. 配置文件

#### 1. Application Config

需要继承 `ApplicationConfig` 类

该配置文件的保存位置为 `./data/config/application/类名.properties` 

提示：配置文件的名称可以重写 `getName()` 方法来改变

**注意：构造方法一定要按照这个结构来写**
配置文件保存应该还有`saveAll()` 方法来将字段保存到内存集合中然后保存到文件中   
配置文件支持 json中的对象和数组，请注意分辨与使用。
配置文件可能无法正常使用非json的数组
```java
@Getter
@Setter
@Slf4j
public final class ChatGPTQuota extends ApplicationConfig {
    //字段: 可以使用get方法获取,但是前提是重写了set方法与进行了初始化
    //如果配置文件出现的键值对没有在类中定义可以使用 this.get("key") 来获取
    private  double TotalAmount = 18;

    public ChatGPTQuota() {
        super(true);
        initialize();
    }
    // 注意：如果不写这两个构造方法，那么你将无法通过 getTotalAmount 方法来获取值，只能通过 get("KEY") 方法来获取。修改也一样
    //该构造方法是给 ApplicationConfig 的反射方法调用的不可随意修改
    public ChatGPTQuota(boolean isInit) {
        super(isInit);
        if (isInit)initialize();
    }

    private void initialize(){
        setFieldValue(this);
    }
    
    //过时：现在不再需要再进行手动赋值了
    //现在保存需要 使用 saveAll()进行保存配置文件
    //注意重写set方法时请，务必加入 put 方法
    @Deprecated
    public void setFrequency(int TotalAmount) {
        TotalAmount = TotalAmount;
        this.put("Frequency", TotalAmount);
        //或者使用
        //initialize();
    }
}
```
注意: 配置文件的字段最好是JAVA的八大数据类型或者是JSON格式，如果是其他对象请实现接口ApplicationType<你的类的名称> 并重写 parse(String) 方法,请解析该方法中的参数，并返回你构造的实例对象


#### 2. 框架配置文件

框架的配置文件放在 `src/main/resources/config` 下，这里面是模板。

运行后会把配置文件放`data/config/gloval`，**注意：需要里面的注释被删除后才能被框架正常加载**

**下面是如何新建个配置文件，如果没有特殊需求不需要这个来实现，毕竟这部分非常屎**

* 继承 `github.zimoyin.mtool.config.global.Config`，并且你新建的类要与此类在一个包下
* 你的类必须是单例，并且你的类需要自己去 `data/config/gloval` 下读取配置文件
* 在 `src/main/resources/config` 下新建你的配置文件
* 在 `src/main/resources/config/config.json` 下注册你的配置文件

## **5. 线程池**

线程池是最屎的，不是很推荐

* 线程池类：github.zimoyin.mtool.uilt.NewThreadPoolUtils
* `@ThreadSpace`：使用此注解可以让 **命令方法、控制器方法**在线程池下运行

## 6. 数据库

```java
H2ConnectionFactory.INSTANCE.getConnection() //全局数据库，所有机器人都可以调用
H2ConnectionFactory.INSTANCE.getConnection(botID)//机器人数据库
```
* `ResultMapObject` 将查询结果转为java对象
## 7. 自定义事件

* 继承 `github.zimoyin.mtool.event.AbstractPrivateEvent`
* 广播事件 `EventKt.broadcast(event)`

* 框架拓展的事件
> [AtMessageEvent.java](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fmtool%2Fevent%2FAtMessageEvent.java)
> [FileMessageEvent.java](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fmtool%2Fevent%2FFileMessageEvent.java)
> [HistoricalMessageEvent.java](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fmtool%2Fevent%2FHistoricalMessageEvent.java)
## 8. 临时监听

临时监听通常用于接收用户上传的图片或者文件等

* 创建临时监听 `ListeningRegistration.newTempListener(Event.class,EventTask);`

```java
//注意: 事件类型要一致  MessageEvent.class, new EventTask<MessageEvent>
ListeningRegistration.newTempListener(MessageEvent.class, new EventTask<MessageEvent>() {
    //临时监听的处理逻辑
    //如果返回true就结束监听
    //默认临时监听只能存活三分钟
    @Override
    public boolean run(MessageEvent event) {
        return false;
    }

    //获取死亡时间，可以通过重写此方法以此来设定死亡时间
    @Override
    public long getTimeOfDeath();
    //获取存活时间，可以重写此方法以此自定义世间，单位毫秒
    @Override
    public long getLifeLength();
    //初始化时执行
    @Override
    public void init();
    //临时监听结束后执行的钩子函数
    @Override
    public void dead();
    //超时死亡时执行的构子函数
    @Override
    public void timeoutDead();

    //主动关闭时执行的构子函数
    @Override
    public void closeDead();
    //是否已经超时死亡。可以通过重写此方法来自定义死亡条件，或者禁止超时死亡
    @Override
    public boolean isTimeoutDead();
    //描述
    @Override
    public String getDescription();
});
```



# 三、常用操作

### 1. 引用回复

```java
MessageChain chain = new MessageChainBuilder() // 引用收到的消息并回复 "Hi!", 也可以添加图片等更多元素.
    .append(new QuoteReply(event.getMessage()))
    .append(msg)
    .build();
sendMessage(chain)
```

* `CommandData` 封装了此方法

```java
commandData.sendQuoteMessage(...);
```

### 2. 合并信息转发

```java
ForwardMessageBuilder forward = new ForwardMessageBuilder(data.getContact());
//发送者ID，发送者名称，发送的内容
forward.add(123, "发送者的ID", new PlainText("内容"));
forward.build();
```



* 同样 `CommandData` 封装此方法

```java
data.sendForwardMessage(val -> {
    val.append("你好");
    val.append("你好").append("帅");
    val.setDisplayStrategy(...);//设置显示方案
});
```



### 3. 小卡片与小程序

* [音乐卡片](https://github.com/mamoe/mirai/blob/dev/mirai-core-api/src/commonMain/kotlin/message/data/MusicShare.kt)

```java
MusicShare ファッション = new MusicShare(
    MusicKind.NeteaseCloudMusic,
    "ファッション",
    "rinahamu/Yunomi",
    "http://music.163.com/song/1338728297/?userid=324076307",
    "http://p2.music.126.net/y19E5SadGUmSR8SZxkrNtw==/109951163785855539.jpg",
    "http://music.163.com/song/media/outer/url?id=1338728297&userid=324076307"
);
sendMessage(ファッション);
```



* [JSON](https://github.com/mamoe/mirai/blob/dev/mirai-core-api/src/commonMain/kotlin/message/data/RichMessage.kt)
* [XML](https://github.com/mamoe/mirai/blob/dev/mirai-core-api/src/commonMain/kotlin/message/data/RichMessage.kt)

### 4. 图片与文件上传下载
* 图片  
见工具类 [ImageUtils.java](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fmtool%2Fuilt%2Fmessage%2FImageUtils.java)  
* 文件  
见工具类 [FileUtil.java](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fmtool%2Fuilt%2Fmessage%2FFileUtil.java)  
或者使用框架封装的文件系统
[GroupFileSystem.java](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fmtool%2Fuilt%2Fmessage%2FGroupFileSystem.java)  
[GroupFile.java](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fmtool%2Fuilt%2Fmessage%2FGroupFile.java)  
### 5. 好友操作

见事件与Friend类

### 6. 好友/群友进群申请

见事件

### 7. 群操作

见事件与Group类

### 8. 历史记录
Friend 和 Group 的 getRoamingMessages 方法
### 9. [事件列表](https://docs.mirai.mamoe.net/EventList.html#%E7%BE%A4)

### 10. [过滤信息类型](https://docs.mirai.mamoe.net/Events.html#%E8%BF%87%E6%BB%A4)

见官方。当然框架封装了一部分位于 `github.zimoyin.mtool.uilt.message.MessageData`

### 11. 信息撤回

对于自己发送的信息使用`recall()` eg:`data.sendMessage("666").recall();`

对于其他人信息或者自己以前发送的信息:

```java
//代码来自于官方网站
// Java
MessageSource.recall(messageSource);
MessageSource.recall(messageChain); // 获取其中 MessageSource 元素并操作 recall 

MessageSource.recallIn(messageSource, 3000) // 启动异步任务, 3 秒后撤回消息. 返回的 AsyncRecallResult 可以获取结果
MessageSource.recallIn(messageChain, 3000)
```

* 事件
  * `HistoricalMessageEvent` 由框架提供的事件，有详细的撤回信息记录
  * `MessageRecallEvent` 官方事件

### 12. AT与被AT

将他们放入信息链中即可

```
At at = new At(qq);//at某人
AtAll instance = AtAll.INSTANCE;//at所有
```

如何知道被AT了：请检测每一条信息，在里面过滤at信息并加以判断.或使用框架事件([AtMessageEvent.java](src%2Fmain%2Fjava%2Fgithub%2Fzimoyin%2Fmtool%2Fevent%2FAtMessageEvent.java))

### 13. 发送信息前事件
使用 MessagePreSendEvent 事件来对信息进行~~判断是否发送~~以**及修改发送的内容**

### 14. 停止所有监听器
//开启监听也是里面的方法
ListenerSet.complete()
ListenerSet.cancel()

### 15. 停止所有命令方法
CommandBroadcast.getInstance().isBroadcast()//停止所有命令,停止后不广播任何命令与过滤器的执行。慎用此方法
CommandBroadcast.getInstance().isBroadcastCommand()//停止所有命令,但是过滤器是允许被执行的，所以运行他后还可以通过过滤器里面的逻辑来恢复使用
