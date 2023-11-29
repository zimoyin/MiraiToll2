package github.zimoyin.cli.annotation;

import github.zimoyin.cli.command.IShell;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Shell {
    String value();//命令名称

    Class<?> parentCommand() default IShell.class;//父命令

    String[] alias() default {};//命令的别名

    String executeMethod() default "execute";//如果该命令类中没有扫描到 Main 注解的时候就去执行这里注册的执行方法

    String description() default "";// 该命令的描述

    String help() default "";//该命令的帮助

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Documented
    @Inherited
    @interface Parameter {
        String value();//参数名称

        String[] alias() default {};//命令的别名

        boolean isString() default false;//该命令的参数是否需要用双引号包裹

        String description() default "";//该参数的描述

        String help() default "";//该参数的帮助
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Documented
    @Inherited
    @interface Main {
    }
}
