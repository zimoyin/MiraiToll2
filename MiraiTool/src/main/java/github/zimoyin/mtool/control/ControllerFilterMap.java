package github.zimoyin.mtool.control;

import github.zimoyin.mtool.annotation.ControllerFilter;
import github.zimoyin.mtool.annotation.ThreadSpace;
import github.zimoyin.mtool.util.NewThreadPoolUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
public class ControllerFilterMap extends HashMap<Class<? extends Event>, HashSet<ControllerFilterMap.ListenerMethod>> {
    private volatile static ControllerFilterMap INSTANCE;

    private ControllerFilterMap() {
    }

    public static ControllerFilterMap getInstance() {
        if (INSTANCE == null) synchronized (ControllerFilterMap.class) {
            if (INSTANCE == null) INSTANCE = new ControllerFilterMap();
        }
        return INSTANCE;
    }


    /**
     * 添加过滤器
     *
     * @param listenerMethod 过滤器的处理方法
     */
    public void add(ListenerMethod listenerMethod) {
        Class<? extends Event> eventClas = listenerMethod.getEventClas();
        HashSet<ListenerMethod> hashSet = this.getOrDefault(eventClas, new HashSet<>());
        hashSet.add(listenerMethod);
        this.put(eventClas, hashSet);
    }

    /**
     * 执行过滤器
     *
     * @return true 为放行
     */
    public boolean invokes(Event event) {
        boolean result = true;
        for (ListenerMethod listenerMethod : getOrDefault(event.getClass(), new HashSet<>())) {
            result = result && listenerMethod.invoke(event);
        }
        return result;
    }

    @Getter
    @Setter
    @Slf4j
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
        private ControllerFilter eventType;
        /**
         * 监听类型
         */
        private Class<? extends Event> eventClas;

        public ListenerMethod(Class<?> cls, Method method) {

            this.cls = cls;
            this.method = method;
            this.eventType = method.getAnnotation(ControllerFilter.class);
            //判断是否是个合法的监听方法
            if (method.getParameterTypes().length != 1 || eventType == null) {
                log.error(cls + " 类下 " + method + " 方法不是一个合格的监听方法，请保证该方法参数唯一，且为一个事件或其子类");
            }
            if (!method.getReturnType().equals(boolean.class) && !method.getReturnType().equals(Boolean.class)) {
                log.error("禁止注册该过滤器执行方法", new IllegalArgumentException(cls + " 类下 " + method + " 方法返回值不是一个 boolean 类型。"));
                return;
            }
            setEventClass(eventType, method);
        }


        private void setEventClass(ControllerFilter eventType0, Method method0) {
            Class<? extends Event> AnnotationParameterType = eventType0.value();
            Class<? extends Event> methodParameterType = (Class<? extends Event>) method0.getParameterTypes()[0];
            //如果注解的事件类型的值不再是默认就说明，注解的值被修改了，那就是false，这样注册监听的话用的是注解的事件类型而不是方法中定义的事件类型
            if (AnnotationParameterType.equals(net.mamoe.mirai.event.Event.class)) this.eventClas = methodParameterType;
            else this.eventClas = AnnotationParameterType;

            if (!securityDetection(eventClas, methodParameterType))
                log.warn("方法： {} 参数类型与注解 ControllerFilter({}) 描述的事件并没有继承关系", method, AnnotationParameterType);
        }

        /**
         * 判断两个类是否有继承关系
         *
         * @param eventClass       类1
         * @param methodEventClass 类2
         */
        private boolean securityDetection(Class<? extends Event> eventClass, Class<? extends net.mamoe.mirai.event.Event> methodEventClass) {
            return eventClass.isAssignableFrom(methodEventClass) || methodEventClass.isAssignableFrom(eventClass);
        }

        /**
         * 执行这个方法
         *
         * @param event 事件
         */
        public boolean invoke(Event... event) {
            try {
                if (isExecute()) {
                    ThreadSpace annotation = getMethod().getAnnotation(ThreadSpace.class);
                    if (annotation == null)
                        return (boolean) method.invoke(getObj(), event);
                    else NewThreadPoolUtils.getInstance().execute(() -> {
                        try {
                            method.invoke(getObj(), event);
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("(监听过滤器处理)方法执行失败 : {}", method, e);
            }
            return true;
        }


        public Object getObj() throws InstantiationException, IllegalAccessException {
            if (obj == null) {
                for (ListenerObj listenerObj : ListenerSet.getInstance()) {
                    if (listenerObj.getCls().equals(this.getCls())) this.setObj(listenerObj.getObj());
                }
                if (obj == null) {
                    obj = cls.newInstance();
                }
            }
            return obj;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ListenerMethod that)) return false;

            return method.equals(that.method);
        }

        @Override
        public int hashCode() {
            return method.hashCode();
        }
    }
}
