package github.zimoyin.mtool.annotation;

import net.mamoe.mirai.event.Event;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
/**
 * 注册该方法为一个控制器的具体事件监听方法
 * 注意：该方法与 @ControllerFilter 注解一样，需要在 @Controller 声明的控制器类下才有效
 */
public @interface EventType {
    /**
     * 监听的事件，如果事件为某事件的子事件，则该子事件被触发时同时触发父事件。如果不想处理子事件则需要手动判断与处理
     */
    Class<? extends Event> value() default Event.class;
}
