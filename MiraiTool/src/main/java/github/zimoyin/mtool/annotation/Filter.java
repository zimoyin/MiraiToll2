package github.zimoyin.mtool.annotation;

import github.zimoyin.mtool.command.filter.AbstractFilter;
import github.zimoyin.mtool.command.filter.impl.Level;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
/**
 *  当注解作用于命令方法上，将在方法执行前执行指定的过滤器与所有全局过滤器
 *  当注解作用于类(需要继承 AbstractFilter 类)上，将声明类为全局过滤器 (只继承 AbstractFilter 的过滤器为局部过滤器)
 */
public @interface Filter {
    Level value() default Level.UNLevel;//过滤等级，注意此属性不受过滤器影响

    Class<? extends AbstractFilter>[] filterCls() default {AbstractFilter.class};//需要经过的局部过滤器列表
}
