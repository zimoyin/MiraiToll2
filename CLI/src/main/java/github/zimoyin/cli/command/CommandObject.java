package github.zimoyin.cli.command;

import github.zimoyin.cli.annotation.Shell;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Slf4j
public class CommandObject {
    /**
     * 命令的注解
     */
    private final Shell annotationShell;
    /**
     * 命令类
     */
    private final Class<?> cls;
    /**
     * 命令参数
     */
    private final List<Parameter> parameters = new ArrayList<Parameter>();
    /**
     * 命令的执行方法
     */
    private final List<Method> mainMethods = new ArrayList<Method>();
    /**
     * 命令名称
     */
    private final String name;
    /**
     * 命令别名
     */
    private final String[] aliases;
    /**
     * 是否为子命令
     */
    private final boolean subcommand;
    /**
     * 父命令
     */
    private final CommandObject parent;
    /**
     * 命令的描述
     */
    private final String description;
    /**
     * 命令的帮助
     */
    private final String help;
    /**
     * 命令实例
     */
    @Setter
    private Object object;
    /**
     * 子命令实例
     */
    private List<CommandObject> childCommand = new ArrayList<CommandObject>();

    public CommandObject(@NonNull Class<?> cls, CommandObject command) {
        Object object1;
        try {
            object1 = cls.newInstance();
        } catch (Exception e) {
            log.error("无法实例化命令 {}", cls, e);
            object1 = null;
        }
        this.object = object1;
        this.cls = cls;
        this.annotationShell = cls.getAnnotation(Shell.class);
        this.name = annotationShell.value();
        this.aliases = annotationShell.alias();
        this.subcommand = !annotationShell.parentCommand().equals(IShell.class);
        this.parent = command;
        this.description = annotationShell.description();
        this.help = annotationShell.help();
        buildMethods();
        buildParameters();
        if (subcommand && parent == null) {
            log.error("这是个子命令，但是没有父命令的引用: {}", this);
        }
    }

    private void buildParameters() {
        Stream.concat(Arrays.stream(cls.getDeclaredFields()), Arrays.stream(cls.getFields()))
                .filter(field -> field.getAnnotation(Shell.Parameter.class) != null)
                .forEach(field -> parameters.add(new Parameter(field, object)));
    }

    private void buildMethods() {
        Arrays.stream(cls.getMethods()).filter(method -> method.getAnnotation(Shell.Main.class) != null).forEach(mainMethods::add);
        try {
            if (IShell.class.isAssignableFrom(cls)) mainMethods.add(cls.getMethod("execute"));
        } catch (Exception e) {
            log.error("无法加载该命令的运行方法: {}", cls, e);
        }
        List<Method> mainMethodsClone = new ArrayList<>(mainMethods);
        mainMethods.clear();
        mainMethods.addAll(mainMethodsClone.stream().distinct().collect(Collectors.toList()));
    }

    public void execute() {
        if (mainMethods.size() == 0) {
            log.error("该命令没有执行方法", new NullPointerException("方法为NULL"));
            return;
        }
        mainMethods.forEach(method -> {
            try {
                method.invoke(this.object);
            } catch (Exception e) {
                log.warn("命令执行方法内部运行异常，该异常没有被命令方法本身正确的记录");
                log.error("无法执行该命令的方法:{}", method, e);
            }
        });
    }

    public void execute(Object object) {
        if (mainMethods.size() == 0) {
            log.error("该命令没有执行方法", new NullPointerException("方法为NULL"));
            return;
        }
        mainMethods.forEach(method -> {
            try {
                method.invoke(object);
            } catch (Exception e) {
                log.error("无法执行该命令的方法:{}", method, e);
            }
        });
    }

    @Override
    public String toString() {
        return "CommandObject{" + "name='" + name + '\'' + ", description='" + description + '\'' + '}';
    }

    public void setChildCommand(CommandObject childCommand) {
        this.childCommand.add(childCommand);
    }

    @Getter
    @ToString
    public static class Parameter {
        private final Shell.Parameter annotationParameter;
        private final Class<?> type;
        /**
         * 参数名称
         */
        private final String name;
        /**
         * 参数别名
         */
        private final String[] alias;
        private final String description;
        private final String help;
        private final Field field;
        @Setter
        private Object value;

        public Parameter(@NonNull Field field, @NonNull Object obj) {
            this.field = field;
            this.annotationParameter = field.getAnnotation(Shell.Parameter.class);
            this.name = annotationParameter.value();
            this.type = field.getType();
            this.alias = annotationParameter.alias();
            this.description = annotationParameter.description();
            this.help = annotationParameter.help();
            try {
                field.setAccessible(true);
                value = field.get(obj);
            } catch (Exception e) {
                log.error("无法获取命令类中变量的默认值:{}", field, e);
            }
        }
    }
}
