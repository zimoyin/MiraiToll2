package github.zimoyin.mtool.control;

import github.zimoyin.mtool.annotation.EventType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.Listener;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 监听对象
 */
@Data
@Slf4j
public class ListenerObj {
    /**
     * 监听器类
     */
    private Class<?> cls;

    /**
     * 监听器对象
     */
    private Object obj;
    /**
     * 监听器方法集合
     */
    private ArrayList<ListenerMethod> ListenerMethods;

    /**
     * 这是否是个有效监听器
     */
    private boolean isListening = false;

    public ListenerObj(Class<?> cls) {
        try {
            if (!isControllerClass(cls)) {
                log.error("初始化监听器注册失败，无法对非 Controller 的类进行监听: {}", cls.getName(), new IllegalArgumentException("this 'Controller' class is not a valid value"));
                return;
            }
            this.cls = cls;
            this.obj = cls.newInstance();
        } catch (Exception e) {
            log.error(cls + " 实例创建失败,无法对其进行监听方法注册", e);
        }
        isListening = true;
    }

    private boolean isControllerClass(Class<?> cls) {
        //如果是Controller的子类说明是处理器类
        return cls.getSuperclass() == ControllerAbs.class || cls.isAnnotationPresent(github.zimoyin.mtool.annotation.Controller.class);
    }

    /**
     * 获取在该类下管理的方法监听器
     */
    public ArrayList<Listener> getListeners() {
        ArrayList<Listener> listeners = new ArrayList<Listener>();
        for (ListenerMethod method : this.getMethods()) {
            listeners.add(method.getListener());
        }
        return listeners;
    }

    /**
     * 获取所有的监听方法
     */
    public ArrayList<ListenerMethod> getMethods() {
        //是否存在缓存
        if (ListenerMethods == null || ListenerMethods.size() == 0) {
            ListenerMethods = new ArrayList<ListenerMethod>();
        } else {
            return ListenerMethods;
        }
        //遍历监听器类下所有的方法，判断是不是监听方法
        for (Method method0 : cls.getMethods()) {
            //获取方法上的注解
            if (method0.getAnnotation(EventType.class) == null) continue;
            //获取方法参数(事件)的class
            if (method0.getParameterTypes().length != 1) {
                //如果方法的参数个数不是一个那么就出问题了
                log.warn(cls + " 类下 " + method0 + " 方法的参数个数不等于一，不符合注解描述");
                continue;
            }
            //是个合格的方法，现在封装 Method
            ListenerMethod listenerMethod = new ListenerMethod(cls, obj, method0);
            //添加方法
            ListenerMethods.add(listenerMethod);
        }
        return ListenerMethods;
    }

    public void start() {
        //TODO: 开启监听处理，让该类下所有的监听器(方法)都开启逻辑处理。如果没有注册则不行
        //但是没有停止监听
        ListenerMethods.forEach(listenerMethod -> listenerMethod.setExecute(false));
    }

    public void stop() {
        //TODO: 停止监听处理，让该类下所有的监听器(方法)都停止逻辑处理。如果没有注册则不行
        //但是没有停止监听
        ListenerMethods.forEach(listenerMethod -> listenerMethod.setExecute(true));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListenerObj)) return false;

        ListenerObj that = (ListenerObj) o;

        return cls.equals(that.cls);
    }

    @Override
    public int hashCode() {
        return cls.hashCode();
    }

    /**
     * 监听器方法封装
     */
    @Data
    public static class ListenerMethod {
        /**
         * 是否运行执行，如果注册了这个监听方法，则为true，否则为 false
         */
        private boolean isExecute = true;
        /**
         * 监听执行方法所在对象实例
         */
        private Object obj;
        /**
         * 监听执行方法所在类
         */
        private Class<?> cls;
        /**
         * 监听执行方法所
         */
        private Method method;
        /**
         * 监听声明
         */
        private EventType eventType;
        /**
         * 监听类型
         */
        private Class<? extends Event> eventClas;
        /**
         * 监听实例
         */
        private Listener listener;

        public ListenerMethod(Class<?> cls, Object obj, Method method) {
            this.obj = obj;
            this.cls = cls;
            this.method = method;
            this.eventType = method.getAnnotation(EventType.class);
            //判断是否是个合法的监听方法
            if (method.getParameterTypes().length != 1 || eventType == null) {
                log.error(cls + " 类下 " + method + " 方法不是一个合格的监听方法，请保证该方法参数唯一，且为一个事件或其子类");
            }
            //如果注解的事件类型的值不再是默认就说明，注解的值被修改了，那就是false，这样注册监听的话用的是注解的事件类型而不是方法中定义的事件类型
//            this.eventClas = eventType.value();
//            if (eventClas.equals(Event.class)) this.eventClas = (Class<? extends Event>) method.getParameterTypes()[0];
            setEventClass(eventType, method);
        }


        public ListenerMethod(Method method) {
            this.method = method;
            this.cls = method.getDeclaringClass();
            this.eventType = method.getAnnotation(EventType.class);
            //判断是否是个合法的监听方法
            if (method.getParameterTypes().length != 1) {
                log.error(cls + " 类下 " + method + " 方法不是一个合格的监听方法，请保证该方法参数唯一且为一个事件或其子类");
                return;
            }
            //判断是否存在事件类型
            if (eventType == null) {
                log.error(cls + " 类下 " + method + " 方法没有指定监听事件类型，请使用 @EventType 注解指定");
                return;
            }
            //如果注解的事件类型的值不再是默认就说明，注解的值被修改了，那就是false，这样注册监听的话用的是注解的事件类型而不是方法中定义的事件类型
//            this.eventClas = eventType.value();
//            if (eventClas.equals(Event.class)) this.eventClas = (Class<? extends Event>) method.getParameterTypes()[0];
            setEventClass(eventType, method);
            //获取对象实例
            try {
                this.obj = cls.newInstance();
            } catch (Exception e) {
                log.error("Failed to instantiate class : {}", cls);
            }
        }


        private void setEventClass(EventType eventType0, Method method0) {
            Class<? extends Event> AnnotationParameterType = eventType0.value();
            Class<? extends Event> methodParameterType = (Class<? extends Event>) method0.getParameterTypes()[0];
            //如果注解的事件类型的值不再是默认就说明，注解的值被修改了，那就是false，这样注册监听的话用的是注解的事件类型而不是方法中定义的事件类型
            if (AnnotationParameterType.equals(Event.class)) this.eventClas = methodParameterType;
            else this.eventClas = AnnotationParameterType;

            if (!securityDetection(eventClas, methodParameterType))
                log.warn("方法： {} 参数类型与注解EventType({}) 描述的事件并没有继承关系", method, AnnotationParameterType);
        }

        /**
         * 判断两个类是否有继承关系
         *
         * @param eventClass       类1
         * @param methodEventClass 类2
         */
        private boolean securityDetection(Class<? extends Event> eventClass, Class<? extends Event> methodEventClass) {
            return eventClass.isAssignableFrom(methodEventClass) || methodEventClass.isAssignableFrom(eventClass);
        }

        /**
         * 执行这个方法
         *
         * @param event 事件
         */
        public void invoke(Event... event) {
            try {
                if (!isExecute()) return;
                method.invoke(obj, event);
            } catch (Exception e) {
                log.error("(监听处理)方法执行失败 : {}", method, e);
            }
        }

    }
}