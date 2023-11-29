package github.zimoyin.mtool.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)   //接口、类、枚举
@Documented
@Inherited
/**
 * 在此注解下将会跳过加载此类，如果类加载器强制要求加载，则会发出告警但不会加载
 */
public @interface SkipLoadClass {
}
