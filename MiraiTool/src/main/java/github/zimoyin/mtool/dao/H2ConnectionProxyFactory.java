package github.zimoyin.mtool.dao;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;

/**
 * 创建动态代理对象
 * 动态代理不需要实现接口,但是需要指定接口类型
 */
@Slf4j
public class H2ConnectionProxyFactory {

    //维护一个目标对象
    private final Connection target;

    public H2ConnectionProxyFactory(Connection target) {
        this.target = target;
    }

    //给目标对象生成代理对象
    public Connection getProxyInstance() {
        return (Connection) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class<?>[]{Connection.class},
                this::invoke
        );
    }

    private Object invoke(Object proxy, Method method, Object[] args) {
        //执行目标对象方法
        Object returnValue = null;
        try {
            returnValue = method.invoke(target, args);
        } catch (Exception e) {
            log.error("H2的动态代理类执行，执行被代理类方法时出现异常", e);
        }
        //如果是获取 Statement 则进行返回代理
        if (method.getName().equals("prepareStatement")) {
            log.debug("create prepare statement: {}", args);
            return new H2PreparedStatementProxyFactory((PreparedStatement) returnValue).getProxyInstance();
        } else if (method.getName().equals("createStatement")) {
            log.debug("create statement: {}", args);
//            log.debug("create statement: {}",args);
            return new H2StatementProxyFactory((Statement) returnValue).getProxyInstance();
        }
        return returnValue;
    }


    /**
     * 创建动态代理对象
     * 动态代理不需要实现接口,但是需要指定接口类型
     */
    @Slf4j
    public static class H2StatementProxyFactory {

        //维护一个目标对象
        private final Object target;

        public H2StatementProxyFactory(Statement target) {
            this.target = target;
        }

        //给目标对象生成代理对象
        public Statement getProxyInstance() {
            return (Statement) Proxy.newProxyInstance(
                    target.getClass().getClassLoader(),
                    target.getClass().getInterfaces(),
                    this::invoke
            );
        }

        private Object invoke(Object proxy, Method method, Object[] args) {
            //执行目标对象方法
            Object returnValue = null;
            try {
                returnValue = method.invoke(target, args);
            } catch (Exception e) {
                if (method.getName().contains("execute"))
                    log.error("SQL Ages Error: {}", args != null ? Arrays.asList(args) : "");
                else log.error("H2的动态代理类执行，执行被代理类方法时出现异常", e);
            }
            if (method.getName().contains("execute"))
                log.debug("SQL Args: {}", args != null ? Arrays.asList(args) : "");
            return returnValue;
        }
    }


    /**
     * 创建动态代理对象
     * 动态代理不需要实现接口,但是需要指定接口类型
     */
    @Slf4j
    public static class H2PreparedStatementProxyFactory {
        //维护一个目标对象
        private final PreparedStatement target;

        public H2PreparedStatementProxyFactory(PreparedStatement target) {
            this.target = target;
        }

        //给目标对象生成代理对象
        public PreparedStatement getProxyInstance() {
            return (PreparedStatement) Proxy.newProxyInstance(
                    target.getClass().getClassLoader(),
                    target.getClass().getInterfaces(),
                    this::invoke
            );
        }

        private Object invoke(Object proxy, Method method, Object[] args) {
            //执行目标对象方法
            Object returnValue = null;
            try {
                returnValue = method.invoke(target, args);
            } catch (Exception e) {
                log.error("H2的动态代理类执行，执行被代理类方法时出现异常", e);
            }
            log.debug("PreparedStatement proxy  method: {}  args:{}", method.getName(), args);
            return returnValue;
        }
    }


}

