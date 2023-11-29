package github.zimoyin.mtool.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)   //接口、类、枚举
@Documented
@Inherited
public @interface CommandClass {
}
