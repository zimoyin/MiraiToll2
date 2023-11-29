package github.zimoyin.cli.listen;

import github.zimoyin.cli.command.CommandManager;
import github.zimoyin.cli.command.CommandObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 命令执行值，用于解析命令
 */
@Setter
@Getter
@Slf4j
public class CommandExecute {
    private CommandManager manager;
    private boolean singletonCommand = false;//命令是否单例运行
    private boolean isLog = false;//是否记录日志

    public CommandExecute() {
    }

    public CommandExecute(CommandManager manager, boolean singletonCommand, boolean isLog) {
        this.manager = manager;
        this.singletonCommand = singletonCommand;
        this.isLog = isLog;
    }

    public void execute(String readLine) {
        manager = CommandManager.getInstance();
        //重定向输出
//        System.setErr(System.out);
//        System.setOut(System.err);
        try {
            //读取命令
            if (readLine == null || readLine.length() == 0) {
                return;
            }
            String[] split = readLine.split("\\s+");
            if (split.length == 0) return;
            if (isLog) log.debug("[CLI-SYSTEM] 解析后的命令数组:{}", Arrays.toString(split));
            //查找命令主语
            CommandObject command = manager.get(split[0]);
            if (command == null) {
                if (isLog) log.warn("[CLI-SYSTEM] 不存在该命令:{}", split[0]);
                else System.err.println("Error: 不存在的命令: " + split[0]);
                return;
            }

            //如果有子命令就查找子命令，否则返回命令主语本体
            command = findCommand(command, 0, split);
            if (command == null) {
                if (isLog) log.warn("[CLI-SYSTEM] 不存在该子命令:{}", split[0]);
                else System.err.println("不存在的子命令: " + split[0]);
                return;
            }
            if (isLog) log.debug("[CLI-SYSTEM] 查找到的命令对象:{}", command);
            //构建参数
            Object instance = getCommandInstance(command);
            setParameter(command, instance, split);
            try {
                //执行命令
                command.execute(instance);
            } catch (Exception e) {
                log.error("[CLI-COMMAND] 该命令执行时命令内部发生了异常，命令执行失败: {}:{}", e.getClass().getSimpleName(), e.getMessage());
//                    log.warn("[CLI-COMMAND] 检测到命令方法内部未捕获到异常: {}", e.getClass().getSimpleName());
//                    log.warn("[CLI-COMMAND] 检测到命令方法内部未捕获到异常: {}:{}", e.getClass().getSimpleName(), e.getMessage());
            }
        } catch (IllegalAccessException e) {
            log.error("[CLI-SYSTEM] 无法为命令类中的字段进行赋值", e);
        } catch (Exception e) {
            log.error("[CLI-SYSTEM] 无法处理的异常 {}  {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * 为命令类的实例对象设置全局属性的值
     *
     * @param command
     * @param instance
     * @param split
     * @throws IllegalAccessException
     */
    public void setParameter(CommandObject command, Object instance, String[] split) throws IllegalAccessException {
        //如果是零个参数
        if (command.getParameters().size() == 0) return;
        //获取所有参数
        List<String> lineParameters = new ArrayList<>();
        boolean isParameter = false;
        for (String sp : split) {
            if (isParameter) lineParameters.add(sp);
            if (command.getName().equalsIgnoreCase(sp)) isParameter = true;
        }
        //如果命令行没有参数
        if (lineParameters.size() == 0) return;
        //创建一个数组，当 lineParameters 数组中某一元素被访问后，该数组中同样位置的数设置为-1
        int[] counts = new int[lineParameters.size()];
        //设置值
        //如果参数有一个或零个值，并且命令行参数为1个
        if (command.getParameters().size() <= 1 && lineParameters.size() == 1) {
            if (isLog) log.debug("省略了默认参数名称");
            CommandObject.Parameter parameter = command.getParameters().get(0);
            //赋值
            Field field = parameter.getField();
            String value = lineParameters.get(0);
            field.setAccessible(true);
            Object caseValue = caseValue(parameter.getType(), value);
            if (caseValue != null) field.set(instance, caseValue);
            if (isLog) log.debug("{}: {}", parameter.getName(), value);
            return;
        }
        //有多个值
        for (CommandObject.Parameter parameter : command.getParameters()) {
            //查找参数所在位置
            int index = index(lineParameters, parameter);
            log.debug("查找参数({})在命令参数列表中所在的位置:{}", parameter.getName(), index);
            if (index < 0) continue; //该参数没有在命令行参数列表里面
            //查找是否存在参数
            String key = lineParameters.get(index);
            counts[index] = -1;
            if (isLog) log.debug("key: " + key);
            String value;
            //如果这个边缘的参数在数组中越界则没有参数
            if (index + 1 >= lineParameters.size()) {
                if (isLog) log.debug("参数({})为边界参数，如果参数为布尔类型的参数名称，就对该参数的默认值进行取反", key);
                //如果该参数是boolean类型则取反
                CommandObject.Parameter param = findParam(command.getParameters(), key);
                if (param != null && (param.getType().equals(Boolean.class) || param.getType().equals(boolean.class)))
                    value = String.valueOf(!Boolean.parseBoolean(param.getValue().toString()));
                else throw new IllegalArgumentException("该参数名称后面没有该参数的值:" + key);
            } else value = lineParameters.get(index + 1);
            //这是个参数名称
            if (isParam(command.getParameters(), value)) {
                if (isLog) log.debug("参数({})是一个参数名称", key);
                //如果该参数是boolean类型则取反
                CommandObject.Parameter param = findParam(command.getParameters(), key);
                if (param != null && (param.getType().equals(Boolean.class) || param.getType().equals(boolean.class)))
                    value = String.valueOf(!Boolean.parseBoolean(param.getValue().toString()));
                else throw new IllegalArgumentException("该参数不是一个boolean并且该参数名称后面没有该参数的值:" + key);
            }
            if (value == null) return;
            if (counts.length > index + 1) counts[index + 1] = -1;
            //赋值
            Field field = parameter.getField();
            field.setAccessible(true);
            Object caseValue = caseValue(parameter.getType(), value);
            if (caseValue != null) field.set(instance, caseValue);
            if (isLog) log.debug("value({}): {}", key, value);
        }
        //如果有未被访问到的参数就抛出异常
        //如果命令类只有一个或者零个参数则不检查
        for (int count : counts) {
            if (count >= 0 && command.getParameters().size() > 1)
                throw new IllegalArgumentException("未知的参数: " + lineParameters.get(count));
        }
    }

    /**
     * 将从命令行读取到的参数根据命令类中的定义来进行转换
     *
     * @param type
     * @param value
     * @return
     */
    private Object caseValue(Class<?> type, String value) {
        try {
            if (type.equals(int.class) || type.equals(Integer.class)) {
                return Integer.parseInt(value);
            } else if (type.equals(long.class) || type.equals(Long.class)) {
                return Long.parseLong(value);
            } else if (type.equals(double.class) || type.equals(Double.class)) {
                return Double.parseDouble(value);
            } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                return Boolean.parseBoolean(value);
            } else if (type.equals(short.class) || type.equals(Short.class)) {
                return Short.parseShort(value);
            } else if (type.equals(byte.class) || type.equals(Byte.class)) {
                return Byte.parseByte(value);
            } else if (type.equals(float.class) || type.equals(Float.class)) {
                return Float.parseFloat(value);
            } else if (type.equals(String.class)) {
                return value;
            } else {
                log.error("[CLI-SYSTEM] 无法将 String 转为 {} 类型: {}", type, value);
            }
        } catch (Exception e) {
            log.error("[CLI-SYSTEM] 无法将 String 转为 {} 类型: {}", type, value);
        }
        return null;
    }

    /**
     * 判断该参数是否在命令类的参数列表里面
     *
     * @param parameters
     * @param name
     * @return
     */
    private boolean isParam(List<CommandObject.Parameter> parameters, String name) {
        if (name == null) return false;
        return parameters.stream().anyMatch(parameter -> parameter.getName().equalsIgnoreCase(name));
    }

    /**
     * 查找参数
     *
     * @param parameters
     * @param name
     * @return
     */
    private CommandObject.Parameter findParam(List<CommandObject.Parameter> parameters, String name) {
        return parameters.stream().filter(parameter -> parameter.getName().equalsIgnoreCase(name.trim())).findFirst().orElse(null);
    }

    /**
     * 查找参数在命令参数列表中所在的位置
     *
     * @param lineParameters
     * @param parameter
     * @return
     */
    private int index(List<String> lineParameters, CommandObject.Parameter parameter) {
        //查找参数所在位置
        String name = parameter.getName();
        String[] alias = parameter.getAlias();
        int index = lineParameters.indexOf(name);
        if (index < 0) for (String alia : alias) {
            int index1 = lineParameters.indexOf(alia);
            if (index1 > -1) index = index1;
        }
        return index;
    }

    /**
     * 获取命令的执行对象
     *
     * @param command 命令封装对象
     */
    public Object getCommandInstance(CommandObject command) {
        if (singletonCommand) return command.getObject();
        try {
            return command.getCls().newInstance();
        } catch (Exception e) {
            log.error("[CLI-SYSTEM] 无法构建该命令的执行对象:{}", command, e);
        }
        return null;
    }

    /**
     * 根据命令解析返回命令对象
     */
    private CommandObject findCommand(final CommandObject command, int index, final String[] split) {
        if (command == null || index >= split.length) return null;
        String commandPax = split[index];
        String ChildCommandPax;
        if (index + 1 >= split.length) return command;
        ChildCommandPax = split[index + 1];
        //查找子命令
        CommandObject childCommand = command.getChildCommand().stream().filter(com -> com.getName().equalsIgnoreCase(ChildCommandPax)).findFirst().orElse(null);
        //检测子节点，如果子节点为null，则返回当前节点
        if (childCommand == null) return command;
        //检查当前节点，如果当前节点与当前解析的命令主语不同就返回当前命令；防止主语后面跟随参数导致解析下一级的子主语
        if (!command.getName().equalsIgnoreCase(split[index])) return command;
        index++;
        CommandObject returnCommand = findCommand(childCommand, index, split);
        //如果因为数组耗尽而返回空，就返回当前的节点对象
        if (returnCommand == null) return command;
        else return returnCommand;
    }
}
