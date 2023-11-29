package github.zimoyin.mtool.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)   //接口、类、枚举
@Documented
@Inherited
/**
 * 注册该类为一个控制器对象
 */
public @interface Controller {

}
