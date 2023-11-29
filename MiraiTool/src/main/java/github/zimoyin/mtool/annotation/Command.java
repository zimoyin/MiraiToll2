package github.zimoyin.mtool.annotation;

import net.mamoe.mirai.event.events.MessageEvent;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited

/**
 * 注册命令方法
 * 改进计划
 *  让eventType字段没有填写时候，直接扫描该注解标注的方法的参数类型(这需要对CommandLoader类的安全检查方法进行修改 （修改完毕）)
 */
public @interface Command {
    //    String[] value();//命令名称
    String value();//命令的名称

    //如果事件是MessageEvent 那么会通用很多命令
    //如果要求对命令发出这更多信息可以用其他更为具体的事件
    Class<? extends MessageEvent> eventType() default MessageEvent.class;//事件类型, 不指定默认值：因为在大部分情况下获取一个命令的事件类型是通过注解而不是命令的方法参数

    //help  如果不想被扫描到命令，请不要动他们
    String help() default "";

    String description() default "";

    String[] alias() default {};//命令的别名
}
