package github.zimoyin.mtool.util.net.httpclient;

import lombok.Getter;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProxyHostPool extends ArrayList<ProxyHost> {
    @Getter
    private static final ProxyFilterFile filter = ProxyFilterFile.getInstance("ProxyHostPool");
    private volatile static ProxyHostPool INSTANCE;

    /**
     * 添加默认的代理服务器
     */
    private ProxyHostPool() {
        add("127.0.0.1", 8070);
        add("127.0.0.1", 7890);
        add("127.0.0.1", 7090);
        filter.addProxyHostList("openai.com");
        filter.addProxyHostList("chat.openai.com");
        filter.addProxyHostList("api.openai.com");
    }

    public static ProxyHostPool getInstance() {
        if (INSTANCE == null) synchronized (ProxyHostPool.class) {
            if (INSTANCE == null) INSTANCE = new ProxyHostPool();
        }
        return INSTANCE;
    }

    /**
     * 添加代理服务器
     *
     * @param proxyHost element whose presence in this collection is to be ensured
     * @return
     */
    @Override
    public boolean add(ProxyHost proxyHost) {
        return super.add(proxyHost);
    }

    /**
     * 添加代理服务器
     */
    public boolean add(String hostName, int port) {
        return super.add(new ProxyHost(hostName, port));
    }

    /**
     * 添加代理服务器
     */
    public boolean add(String hostName, int port, String scheme) {
        return super.add(new ProxyHost(hostName, port, scheme));
    }

    /**
     * 添加代理服务器
     */
    public boolean add(String hostName, int port, String scheme, String location) {
        return super.add(new ProxyHost(hostName, port, scheme, location));
    }

    /**
     * 映射所有的代理服务器
     *
     * @param function
     * @return
     */
    public List<Object> map(Function<ProxyHost, Object> function) {
        return this.stream().map(function).collect(Collectors.toList());
    }

    /**
     * 返回任一可用的代理服务器，如果没有则返回Null
     */
    public ProxyHost getAny() {
        for (ProxyHost proxyHost : this) {
            if (proxyHost.getPing() > 0) return proxyHost;
        }
        return null;
    }

    /**
     * 返回任一可用的代理服务器，如果没有则返回Null
     */
    public InetAddress getAnyOfInetAddress() {
        for (ProxyHost proxyHost : this) {
            if (proxyHost.getPing() >= 0) return proxyHost.getHostAddress();
        }
        return null;
    }

    /**
     * 判断此URL是否需要代理，如果需要返回代理，否则返回Null
     * 注意 如果URL需要代理，但是没有可用的代理服务器就抛出异常
     */
    public ProxyHost getAnyProxyOrThrower(String url) {
        if (filter.isProxy(url)) {
            ProxyHost any = getAny();
            if (any == null) throw new NullPointerException("没有可用的代理");
            return any;
        } else {
            return null;
        }

    }
}
