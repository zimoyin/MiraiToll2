package github.zimoyin.mtool.command;

import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.command.filter.CommandFilter;
import github.zimoyin.mtool.control.ListenerSet;
import github.zimoyin.mtool.dao.H2ConnectionFactory;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;


/**
 * 命令对象
 */
//@Data
@Getter
@ToString

@Slf4j
public class CommandObject {
    /**
     * 针对某一具体命令的命令方法参数传参对象表，里面存储命令所需的对象。该对象对命令来说是唯一的
     */
    private final HashMap<Class<?>, Object> classObjectHashMap = new HashMap<Class<?>, Object>();
    /**
     * 是否运行执行此项命令,为true运行执行，false 不允许执行
     */
    private boolean isExecute = true;
    /**
     * key: 命令方法的名称
     */
    private String name;
    /**
     * value: 方法对象
     */
    private Method method;
    /**
     * 命令所需的参数类型
     */
    private Class<? extends Event> eventClass;
    /**
     * 命令类的 new 对象，用于反射调用方法
     */
    private Object commandObject;
    /**
     * 命令所在类的class
     */
    private Class<?> commandClass;
    /**
     * 该命令的Help
     */
    private CommandHelp help;


    /**
     * @param name          命令的名称
     * @param method        命令方法
     * @param eventClass    命令方法所在的类
     * @param commandObject 命令类的对象
     * @param commandClass  命令类的class
     *
     * 注意： 参数越少，代表被重复创建的东西越多
     */
    public CommandObject(String name,
                         Method method,
                         Class<? extends Event> eventClass,
                         Object commandObject,
                         Class<?> commandClass) {
        this.name = name;
        this.method = method;
        this.eventClass = eventClass;
        this.commandObject = commandObject;
        this.commandClass = commandClass;
    }

    /**
     * 注意： 参数越少，代表被重复创建的东西越多
     * @param name          命令的名称
     * @param method        命令方法
     * @param eventClass    命令方法所在的类
     * @param commandObject 命令类的对象
     *
     *
     */
    public CommandObject(String name,
                         Method method,
                         Class<? extends Event> eventClass,
                         Object commandObject) {
        this.name = name;
        this.method = method;
        this.eventClass = eventClass;
        this.commandObject = commandObject;
        this.commandClass = commandObject.getClass();
    }

    /**
     * 注意： 参数越少，代表被重复创建的东西越多
     * @param name          命令的名称
     * @param method        命令方法
     * @param eventClass    命令方法所在的类
     * @param commandClass  命令类的class
     */
    public CommandObject(String name, Method method, Class<? extends Event> eventClass, Class<?> commandClass) throws InstantiationException, IllegalAccessException {
        this.name = name;
        this.method = method;
        this.eventClass = eventClass;
        this.commandClass = commandClass;
        this.commandObject = commandClass.newInstance();
    }


    /**
     * 注意： 参数越少，代表被重复创建的东西越多
     * @param method        命令方法
     * @param commandClass  命令类的class
     */
    public CommandObject(Method method, Class<?> commandClass) throws InstantiationException, IllegalAccessException {
        this.method = method;
        this.commandClass = commandClass;

        Command annotation = method.getAnnotation(Command.class);
        this.eventClass = annotation.eventType();
        this.name = annotation.value();
        this.commandObject = commandClass.newInstance();
    }

    /**
     * 执行该命令方法
     *
     * @param event
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void execute(MessageEvent event, CommandData commandData) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (!isExecute) {
            log.debug("{} 命令已经被关闭无法被执行 --- {}", getName(), getMethod());
            return;
        }
        if (commandObject == null) commandObject = commandClass.newInstance();
//        Class<?>[] parameterTypes = method.getParameterTypes();
//        if (parameterTypes.length != 1) throw new IllegalArgumentException("不合法的方法参数，参数不是为恒值1个");
//        if (parameterTypes[0].isAssignableFrom(event.getClass())) method.invoke(commandObject, event);
//        else if (parameterTypes[0].isAssignableFrom(CommandData.class)) method.invoke(commandObject, new CommandData(event));
        //上面代码更新为下面代码
        //参数说明：里面参数为没有空构造参数以及特殊的实例对象
        invoke(
                //机器人相关
                event,//事件
                commandData,//事件封装
                event.getMessage(),//消息
                event.getBot(),//机器人
                event.getSubject(),//联系人
                //命令相关
                this,
                new CommandFilter(commandData),
                CommandSet.getInstance(),
                ListenerSet.getInstance(),
                //dao
                H2ConnectionFactory.INSTANCE
        );
    }

    /**
     * 根据方法参数自动注入参数并执行
     *
     * @param args 参数列表
     */
    public void invoke(Object... args) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (commandObject == null) commandObject = commandClass.newInstance();
        Class<?>[] types = method.getParameterTypes();
        Object[] objects = sortObjects(types, objectToMap(args));
        Object invoke = method.invoke(commandObject, objects);//执行方法

        //如果有返回值的话
//        if (method.getReturnType().equals(String.class) && invoke != null && invoke.toString() != null) {
        if (invoke != null) {
            //从参数列表过滤出 CommandData 对象
            Object obj = Arrays.stream(args).filter(object -> CommandData.class.isAssignableFrom(object.getClass())).findFirst().orElse(null);
            if (obj == null) return;
            CommandData data = (CommandData) obj;
            //根据返回值类型进行发送信息
//            Class<?> returnClass = invoke.getClass();
            Class<?> returnClass = method.getReturnType();
            if (MessageChain.class.isAssignableFrom(returnClass)) {
                data.sendMessage((MessageChain) invoke);
            } else if (ForwardMessage.class.isAssignableFrom(returnClass)) {
                data.sendMessage((ForwardMessage) invoke);
            } else if (SingleMessage.class.isAssignableFrom(returnClass)) {
                data.sendMessage((SingleMessage) invoke);
            } else if (Message.class.isAssignableFrom(returnClass)) {
                data.sendMessage((Message) invoke);
            } else if (String.class.isAssignableFrom(returnClass)) {
                data.sendMessage((String) invoke);
            } else {
                data.sendMessage(invoke.toString());
                log.warn("该方法的返回值没有的得到正确的处理，这将调用该返回值的 toString 方法来发送到对话窗口", new NullPointerException("无法解析该数据类型: " + invoke.getClass()));
            }

        }
    }

    /**
     * 将对象排序
     *
     * @param types       排序标准
     * @param objectToMap 对象列表
     */
    private Object[] sortObjects(Class<?>[] types, HashMap<Class<?>, Object> objectToMap) {
        Object[] objs = new Object[types.length];//排序好的参数列表
        for (int i = 0; i < types.length; i++) {
            objs[i] = objectToMap.get(types[i]);
            //如果找不到 types 里面一致的数据，就去找其子类，或父类
            if (objs[i] == null) {
                for (Class<?> cls : objectToMap.keySet()) {
                    //判断是否存在继承关系
                    if (cls.isAssignableFrom(types[i]) || types[i].isAssignableFrom(cls)) {
                        objs[i] = objectToMap.get(cls);
                        break;
                    }
                }
            }
            //如果参数列表没有提供命令方法所需要的参数则尝试new一个
            if (objs[i] == null) {
                //尝试new一个，如果失败就抛出异常
                try {
                    objs[i] = classObjectHashMap.getOrDefault(types[i], types[i].newInstance());
                    classObjectHashMap.put(types[i], objs[i]);
                } catch (Exception e) {
                    throw new IllegalArgumentException("参数列表无法找到的参数:" + types[i], new NullPointerException("无法反射创建该参数的实例对象:" + types[i]));
                }
            }
        }
        return objs;
    }

    /**
     * 参数对象列表转为 map
     *
     * @param args 参数列表
     */
    private HashMap<Class<?>, Object> objectToMap(Object... args) {
        HashMap<Class<?>, Object> obj = new HashMap<Class<?>, Object>();
        for (Object arg : args) {
            if (obj.containsKey(arg.getClass())) throw new IllegalArgumentException("参数列表存在相同的参数类型");
            obj.put(arg.getClass(), arg);
        }
        return obj;
    }

    /**
     * 禁止执行命令
     */
    public void notExecute() {
        this.isExecute = false;
    }

    /**
     * 获取命令帮助
     *
     * @return
     */
    public CommandHelp getHelp() {
        if (help == null) help = CommandHelp.create(this.method);
        return help;
    }

    @Data
    public static class CommandHelp {
        /**
         * 命令帮助
         */
        private String help;
        /**
         * 命令描述
         */
        private String description;


        public CommandHelp(String help, String description) {
            this.help = help;
            this.description = description;
        }

        public static CommandHelp create(Method commandMethod) {
            Command annotation = commandMethod.getAnnotation(Command.class);
            String help = annotation.help();
            String description = annotation.description();

            return new CommandHelp(help, description);
        }
    }
}
