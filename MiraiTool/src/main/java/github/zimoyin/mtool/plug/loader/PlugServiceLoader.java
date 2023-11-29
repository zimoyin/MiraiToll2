package github.zimoyin.mtool.plug.loader;

import lombok.Getter;
import lombok.ToString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

/**
 * 扫描类加载器所在的项目下的SPI文件，与类加载器所要加载的目标Jar下的SPI文件
 * 经过测试 PlugServiceLoader 与 ServiceLoader 功能重叠。
 * 具体原因在于，ServiceLoader 会调用类加载器的 getResources 方法，该方法因为未知原因返回NUll，经过实现后已经可用
 */
public final class PlugServiceLoader<S> {
    private static final String PATH = "META-INF/services/";
    private final ClassLoader classLoader;
    private final Class<S> svc;

    public PlugServiceLoader(Class<S> svc) {
        this.svc = svc;
        this.classLoader = ClassLoader.getSystemClassLoader();
    }

    public PlugServiceLoader(Class<S> svc, ClassLoader classLoader) {
        this.classLoader = (classLoader == null) ? ClassLoader.getSystemClassLoader() : classLoader;
        this.svc = svc;
    }

    /**
     * 为给定的服务类型和类加载器创建一个 PlugServerLoad。
     *
     * @param service 接口类
     * @param loader  类加载器，可为 null
     * @return new PlugServerLoad
     */
    public static <S> PlugServiceLoader<S> load(Class<S> service, ClassLoader loader) {
        return new PlugServiceLoader<>(service, loader);
    }


    /**
     * 获取迭代器
     */
    public Iterator<Packet<S>> getIterator() throws IOException {
        return new LazyIterator(classLoader.getResources(PATH + svc.getTypeName()), classLoader);
    }

    public void forEach(BiConsumer<URL, S> consumer) {
        Iterator<Packet<S>> iterator = null;
        try {
            iterator = getIterator();
            while (iterator.hasNext()) {
                Packet<S> packet = iterator.next();
                consumer.accept(packet.url, packet.s);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 封装表
     *
     * @param <S> 泛型-接口的实现类
     */
    @Getter
    @ToString
    public static class Packet<S> {
        private final S s;
        private final URL url;

        public Packet(S s, URL url) {
            this.s = s;
            this.url = url;
        }
    }

    private class LazyIterator
            implements Iterator<Packet<S>> {
        private final Enumeration<URL> urls;
        private final ClassLoader loader;
        private final ConcurrentLinkedQueue<Packet<S>> queue = new ConcurrentLinkedQueue<>();

        public LazyIterator(Enumeration<URL> urls, ClassLoader classLoader) {
            this.loader = classLoader;
            this.urls = urls;
        }

        @Override
        public boolean hasNext() {
            URL url = nextURL();
            if (url == null) return false;
            try {
                List<String> lines = getLines(url);
                for (String line : lines) {
                    Packet<S> packet = new Packet<>(parse(line), url);
                    this.queue.add(packet);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        @Override
        public Packet<S> next() {
            return queue.remove();
        }

        private URL nextURL() {
            if (urls.hasMoreElements())
                return urls.nextElement();
            else return null;
        }

        private S parse(String cls) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
            Class<?> loadClass = loader.loadClass(cls);
            if (loadClass != null) return (S) loadClass.newInstance();
            return null;
        }

        private List<String> getLines(URL url) throws IOException {
            List<String> classesImplList = new ArrayList<>();
            URLConnection connection = url.openConnection();
            if (connection == null) throw new NullPointerException("无法连接到的地址: " + url);
            try (InputStream inputStream = connection.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                classesImplList.add(reader.readLine().trim());
            }
            return classesImplList;
        }
    }
}
