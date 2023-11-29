package github.zimoyin.mtool.annotation;

import java.lang.annotation.*;

/**
 * 使用该注释将会将代码放入线程池内运行，只包括： 命令，控制器，此外没有任何代码段可以被允许使用本注释
 * 注意：如果在控制器过滤器执行方法上使用会导致事件方法执行器忽略过滤器的返回值
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})   //接口、类、枚举
@Documented
@Inherited
public @interface ThreadSpace {
}
