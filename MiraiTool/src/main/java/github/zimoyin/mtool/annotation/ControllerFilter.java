package github.zimoyin.mtool.annotation;

import net.mamoe.mirai.event.Event;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
/**
 * 注册该方法为一个控制器的过滤器方法。
 * 注意：该方法与 @EventType 注解一样，需要在 @Controller 声明的控制器类下才有效
 * 注意：临时监听不受到过滤器的管理。临时监听为程序控制监听用于临时接收信息，该信息需要临时监听有条件的过滤
 */
public @interface ControllerFilter {
    /**
     * 监听的事件，只处理与过滤器注册的事件一致的事件，如果该事件的父或者子事件发生则忽略不处理
     */
    Class<? extends Event> value() default Event.class;
}
